package org.connected_sources.api.web.onboarding;


import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import org.apache.logging.log4j.util.TriConsumer;
import org.connected_sources.api.dto.onboarding.*;
import org.connected_sources.core.user.User;
import org.connected_sources.core.user.UserRepository;
import org.connected_sources.core.user.async.onboarding.OnboardingProvisioner;
import org.connected_sources.core.user.onboarding.model.OnboardingRequestCmd;
import org.connected_sources.core.user.onboarding.model.OnboardingSummary;
import org.connected_sources.core.user.onboarding.model.ProvisioningSpec;
import org.connected_sources.core.user.onboarding.repo.OnboardingRepo;
import org.connected_sources.notification.core.Channel;
import org.connected_sources.notification.events.EventType;
import org.connected_sources.notification.service.ContactInformationRepo;
import org.connected_sources.notification.service.NotificationDispatcher;
import org.connected_sources.notification.template.NotificationTemplate;
import org.connected_sources.notification.template.TemplateService;
import org.connected_sources.shared.context.TenantContextHolder;
import org.connected_sources.shared.naming.TenantIdNormalizer;
import org.connected_sources.shared.onboarding.OnboardingState;
import org.connected_sources.tenant.spi.db.TenantResourcePlanner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;


/* DESIGN
 * -----------------
 * Surface for the curator-approved, two-step onboarding:
 *
 *   REQUESTED  ->  APPROVED  ->  PREPARATION  ->  ENABLED/FAILED/EXPIRED
 *
 * Goals:
 *  - Keep POST /onboarding/requests fast & idempotent (no heavy work).
 *  - Push provisioning to async worker; return immediately.
 *  - Record audit at each transition; emit notifications.
 *
 * Alternatives considered:
 *  - One-shot synchronous onboarding (rejected: latency, fragility).
 *  - Blocking until FS/SQLite ready (rejected: tail latencies, retries).
 * The chosen split improves resiliency and isolates failures.
 */
@RestController
@RequestMapping("/onboarding/requests")
@Validated
public class OnboardingController {
  private static final String SUBMITTEDAT = "submitted_at";
  private final OnboardingRepo repo;
    //  private final TenantRepo tenantRepo;
    private final NotificationDispatcher notifier;
    private final OnboardingProvisioner provisioner; // async entry
    private final TenantResourcePlanner tenantResourcePlanner;
    private final String baseUrl;
    private final @Value("${tenant.base-directory}") Path baseDir;
    private final Long onboardingRequestTtl;
    private final TemplateService templateService;
    private final ContactInformationRepo contactRepo;
    private final UserRepository userRepository;

    private static final boolean noPii = false;
    private static final boolean hasPii = true;

    public OnboardingController(OnboardingRepo repo,
//                              TenantRepo tenantRepo,
                                NotificationDispatcher notifier,
                                OnboardingProvisioner provisioner,
                                TenantResourcePlanner tenantResourcePlanner,
                                @Value("${backend.base-url}") String baseUrl,
                                @Value("${tenant.base-directory}") Path baseDir,
                                @Value("${onboarding.request.ttl}") String onboardingRequestTtlSeconds, TemplateService templateService, ContactInformationRepo contactRepo,
                                UserRepository userRepository) {
        this.repo = repo;
//    this.tenantRepo = tenantRepo;
        this.notifier = notifier;
        this.provisioner = provisioner;
        this.tenantResourcePlanner = tenantResourcePlanner;
        this.baseUrl = baseUrl;
        this.baseDir = baseDir;
        this.onboardingRequestTtl = Long.valueOf(onboardingRequestTtlSeconds);
        this.templateService = templateService;
        this.contactRepo = contactRepo;

        // Log per debug
        System.out.println("OnboardingController created with:");
        System.out.println("base_url: " + baseUrl);
        System.out.println("baseDir: " + baseDir);
        this.userRepository = userRepository;
    }


    // 1) REQUESTED
    // FUNCTION
    // POST /onboarding/requests
    // Creates or reuses an OnboardingRequest in state REQUESTED.
    // Idempotency key = (normalized producer, email, day-bucket).
    // Returns current request view, never triggers provisioning.

    // WHY
    // It round the timestamp to a day-bucket for idempotency so that accidental
    // double-submissions within the same day are suppressed, while legitimate
    // follow-ups on later days are accepted.
    @PreAuthorize("hasRole('USER')") // Solo utenti autenticati
    @PostMapping // --- POST /onboarding/requests
    public ResponseEntity<OnboardingView> request(@RequestBody @Valid OnboardingRequestCreate in) {
        var tc = TenantContextHolder.get();
        final Long userId = tc.userId();
        var cmd = new OnboardingRequestCmd(
                userId.longValue(),
                in.producerName(), in.email(), in.website(), in.vatOrFiscalCode()
        );

        String idem = in.producerName() + "|" + in.email() + "|" + java.time.LocalDate.now();
        boolean firstTime = repo.tryClaimIdempotency(idem, Duration.ofSeconds(onboardingRequestTtl.longValue()));
        if (!firstTime) {
            try {
                long existingId = repo.createOrReuseRequest(cmd, tc.correlationId());
                OnboardingView view = repo.findSummary(existingId)
                        .map(this::toView)
                        .orElse(new OnboardingView(existingId, OnboardingState.REQUESTED, in.producerName(), in.email()));
                return ResponseEntity.accepted().body(view);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("errore nella codifica in jsonb della correlazione" + tc.correlationId(), e);
            }
        }

        try {
            long id = repo.createOrReuseRequest(cmd, TenantContextHolder.get().correlationId());

            var vars = new java.util.HashMap<String, Object>();

            var requestingUser = userRepository.findByUserId(userId);
            vars.put("requesterUserId", requestingUser.get().full_name());
            vars.put("producerName", in.producerName());
//      vars.put("requesterEmail",    in.email());
            vars.put("email", in.email());
            vars.put("website", in.website());
            vars.put("vatOrFiscalCode", in.vatOrFiscalCode());
            vars.put("correlationId", tc.correlationId());
            vars.put(SUBMITTEDAT, java.time.OffsetDateTime.now().toString());

            vars.put("approveUrl", baseUrl + "/onboarding/requests/" + id + "/approve");
            vars.put("rejectUrl", baseUrl + "/onboarding/requests/" + id + "/reject");

            var rendered = templateService.render(
                    NotificationTemplate.ONBOARDING_REQUESTED.name(), vars, null);

            contactRepo.curatorsPrimaryEmail().forEach(c -> {
                // notify curator (simplified AUTO channel)
                notifier.enqueue(
                        NotificationTemplate.ONBOARDING_REQUESTED,
                        c.address(),
                        Channel.EMAIL,
                        "[ onboarding ] " + in.producerName(),
                        rendered.bodyMd(),
                        Duration.ofHours(24), // TODO: from paraterms
                        EventType.ONBOARDING_REQUESTED, hasPii
                );
            });

            OnboardingView view = repo.findSummary(id)
                    .map(this::toView)
                    .orElse(new OnboardingView(id, OnboardingState.REQUESTED, in.producerName(), in.email()));
            return ResponseEntity.accepted().body(view);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("errore nella codifica in jsonb della correlazione" + tc.correlationId(), e);
        }
    }

    // ---- mapper ----
    private OnboardingView toView(OnboardingSummary s) {
        return new OnboardingView(s.id(), s.state(), s.producerName(), s.email());
    }

    // 2) APPROVE / REJECT
    @PreAuthorize("hasRole('CURATOR')")
    @PostMapping("/{id}/approve")
    public ResponseEntity<ApprovedOnboardingView> approve(@PathVariable long id) {
        // generate secure token & link (store it in details)
        String token = java.util.UUID.randomUUID().toString();

        // email producer with registration link
        final String link = baseUrl + "/onboarding/" + id + "/register-producer?token=" + token;

        try {
            repo.transitionState(
                    id, OnboardingState.APPROVED,
                    /*curator*/ TenantContextHolder.get().userId(),
                    Map.of("token", token, "link", link));
        } catch (IllegalStateException | IllegalArgumentException _ ) {
            return ResponseEntity.badRequest().build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        Function<OnboardingSummary, OnboardingSummary> sendApprovation = (OnboardingSummary s) -> {
            var requestingUser = userRepository.findByUserId(s.requester());
            if (requestingUser.isEmpty()) return s;

            User requester = requestingUser.get();
            var vars = new java.util.HashMap<String, Object>();
            vars.put("requesterUserId", requester.username());
            vars.put("producerName", s.producerName());
            vars.put("requesterEmail", requester.email());
            vars.put("email", s.email());
            vars.put("website", s.website());
            vars.put("vatOrFiscalCode", s.vatOrFiscalCode());
            vars.put("correlationId", s.correlationId());
            vars.put("link", link);
            vars.put(SUBMITTEDAT, s.created_at().toLocalDateTime().format(
                    DateTimeFormatter
                            .ofPattern("dd/MM/yyyy HH:mm:ss")
                            .withLocale(Locale.ITALIAN)));

            String requesterMsg = "È stata inoltrata a " + s.email() + " il modulo di registrazione a Connected Sources.";

            var rendered = templateService.render(
                    NotificationTemplate.ONBOARDING_APPROVED.name(),
                    vars, null);



            this.notifier.enqueue(
                    NotificationTemplate.ONBOARDING_APPROVED,
                    requester.email(), Channel.EMAIL,
                    "[ registration request approved ] " + s.producerName(),
                    requesterMsg, Duration.ofHours(24),
                    EventType.ONBOARDING_ACCEPTED, noPii);

            this.notifier.enqueue(
                    NotificationTemplate.ONBOARDING_APPROVED,
                    s.email(), Channel.EMAIL,
                    "Connected Sources registration",
                    rendered.bodyMd(), Duration.ofHours(24),
                    EventType.ONBOARDING_ACCEPTED, hasPii);

            return s;
        };


        return repo.findSummary(id)
                .map(sendApprovation)
                .map(s -> new ApprovedOnboardingView(s.id(), s.state(), s.producerName(), s.email(), token))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('CURATOR')")
    @PostMapping("/{id}/reject")
    public ResponseEntity<OnboardingView> reject(@RequestBody @Valid OnboardingRequestRejection payload) {
        var ctx = TenantContextHolder.get();

        HashMap<String,Object> rejectionData = new HashMap<>();
        rejectionData.put("reason", payload.reason());
        rejectionData.put("hints", payload.hints() );
        try {
            repo.transitionState(payload.id(),
                    OnboardingState.REJECTED,
                    ctx.userId(),
                    rejectionData);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        UnaryOperator<OnboardingSummary> sendRejection = s -> {
            var requestingUser = userRepository.findByUserId(s.requester());
            if (requestingUser.isEmpty()) return s;

            User requester = requestingUser.get();
            rejectionData.put(SUBMITTEDAT, s.created_at().toLocalDateTime().format(
                    DateTimeFormatter
                            .ofPattern("dd/MM/yyyy HH:mm:ss")
                            .withLocale(Locale.ITALIAN)));

            var rendered = templateService.render(
                    NotificationTemplate.ONBOARDING_APPROVED.name(),
                    rejectionData, null);

            this.notifier.enqueue(
                    NotificationTemplate.ONBOARDING_REJECTED,
                    requester.email(), Channel.EMAIL,
                    "[ registration request rejected ] ",
                    rendered.bodyMd(), Duration.ofHours(24),
                    EventType.ONBOARDING_REJECTED, noPii);

            return s;
        };

        return repo.findSummary(payload.id())
                .map(sendRejection)
                .map(this::toView)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 3) REGISTRATION -> create Tenant(PREPARATION) + async provisioning
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{id}/register-producer")
    public ResponseEntity<OnboardingView> register(
            @PathVariable long id, @RequestBody RegistrationPayload in,
            @RequestParam("token") String token) {

        // normalize tenantId from the req. hint
        String raw = in.tenantIdHint() != null ? in.tenantIdHint() : ("t-" + id + "-" + System.currentTimeMillis());
        String tenantId = TenantIdNormalizer.normalize(raw);

        // Delegate infra decisions to tenant-api
        var plan = tenantResourcePlanner.plan(tenantId);

        try {
            // Persist tenant data
            repo.createTenant(
                    plan.tenantId(),
                    /* producerName */ plan.tenantId(),   // set proper producer name as needed
                    plan.baseDir(),
                    plan.dsDesc()                         // << store DS descriptor (not raw path)
            );

            repo.transitionState(id, OnboardingState.PREPARATION,
                    null, // no actor for the self-registration from email
                    Map.of("tenantId", tenantId, "adminEmail", in.producerAdminEmail()));

            var provSpec = new ProvisioningSpec(in.producerAdminEmail(), in.initialUsers());
            provisioner.enqueueProvisioning(id, plan.tenantId(), plan.baseDir(), provSpec);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("error processing tenant datasource.", e);
        }

        // Build response from DB
        return repo.findSummary(id)
                .map(this::toView)
                .map(v -> ResponseEntity.accepted().body(v))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 4 - STATUS
    @PreAuthorize("hasRole('USER')") // Solo utenti autenticati
    @GetMapping("/{id}/status") // --- GET /onboarding/requests/{id}/status
    public ResponseEntity<OnboardingView> status(@PathVariable long id) {

        return repo.findSummary(id)
                .map(this::toView)
                .map(v -> ResponseEntity.ok().body(v))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


}
