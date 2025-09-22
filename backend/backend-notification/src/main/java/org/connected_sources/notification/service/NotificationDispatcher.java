package org.connected_sources.notification.service;

import java.time.*;
import java.util.*;

import org.connected_sources.notification.events.EventType;
import org.connected_sources.notification.incident.DelegatingCategoryResolver;
import org.connected_sources.notification.incident.RedmineTicketLocator;
import org.connected_sources.notification.template.NotificationTemplate;
import org.connected_sources.shared.logging.TenantLogger;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.transaction.annotation.Transactional;
import org.connected_sources.notification.core.*;
import org.connected_sources.shared.context.TenantContextHolder;

import static org.connected_sources.shared.logging.TenantLogger.Level.*;

/* DESIGN
 * -----------------
 * Dispatch flow
 *  1) Render template
 *  2) Send via configured channel (no fallback)
 *  3) Audit attempt + provider meta
 *  4) On permanent error: locate-or-create Redmine ticket, attach ticketId
 *  5) Schedule retry for both transient & permanent (policy-driven)
 *
 * Why we retry permanent errors: operational policy prefers auto-heal after
 * misconfiguration/provider hiccups. Ticket ensures human visibility.
 */
// WHY
// We never fall back from Telegram->Email (and vice versa) to preserve
// tenant/user channel preferences and avoid duplicate deliveries.
@Service
public class NotificationDispatcher {

  private final DelegatingCategoryResolver categoryResolver;
  private final RedmineTicketLocator ticketLocator;
  private final int projectId;  // inject from properties
  private final int trackerId;  // inject from properties

  private final ChannelAdapterFactory factory;
  private final NotificationRepo repo;
  private final TaskScheduler scheduler;
  private final TenantLogger log;


  @Value("${notifications.retry.maxAttempts:5}") int maxAttempts;
  @Value("${notifications.retry.backoff.initial:PT2S}") Duration initialBackoff;
  @Value("${notifications.retry.backoff.multiplier:2.0}") double backoffMult;
  @Value("${notifications.retry.backoff.max:PT5M}") Duration maxBackoff;


  public NotificationDispatcher(
          DelegatingCategoryResolver categoryResolver,
          RedmineTicketLocator ticketLocator,
          @Value("${redmine.projectId}") int projectId,
          @Value("${redmine.incident.trackerId}") int trackerId,
          ChannelAdapterFactory factory,
          NotificationRepo repo,
          TaskScheduler scheduler,
          TenantLogger log,
          @Value("${notifications.retry.maxAttempts:5}") int maxAttempts,
          @Value("${notifications.retry.backoff.initial:PT2S}") Duration initialBackoff,
          @Value("${notifications.retry.backoff.multiplier:2.0}") double backoffMult,
          @Value("${notifications.retry.backoff.max:PT5M}") Duration maxBackoff
                               ) {
    this.categoryResolver = categoryResolver;
    this.ticketLocator = ticketLocator;
    this.projectId = projectId;
    this.trackerId = trackerId;
    this.factory = factory;
    this.repo = repo;
    this.scheduler = scheduler;
    this.log = log;
    this.maxAttempts = maxAttempts;
    this.initialBackoff = initialBackoff;
    this.backoffMult = backoffMult;
    this.maxBackoff = maxBackoff;
  }


  /** Key = (templateId, recipientKey, correlationId) */
  // no custom rollback -> RuntimeException and Error
  @Transactional
  protected DispatchOutcome enqueueInternal(String templateId, String recipientKey, Channel channel,
                                            String subject, String bodyMd, Duration ttl, String eventType,
                                            boolean hasPii) {
    String corr = TenantContextHolder.get().correlationId();
    String key = templateId+"|"+recipientKey+"|"+corr;
    if (repo.existsIdem(key)) {
      return DispatchOutcome.duplicate();
    }
    repo.putIdem(key, ttl);


    String auditId = repo.createAudit(eventType, templateId, channel, subject, hasPii, bodyMd);
    scheduleSend(auditId, channel, subject, bodyMd, 1, eventType, ttl, hasPii, initialBackoff);
    return DispatchOutcome.accepted(auditId);
  }

  public DispatchOutcome enqueue(
          NotificationTemplate template,
          String recipientKey,
          Channel channel,
          String subject,
          String bodyMd,
          Duration ttl,
          EventType eventType,
          boolean hasPii) {
    return enqueueInternal(template.id(), recipientKey, channel, subject, bodyMd, ttl, eventType.name(), hasPii);
  }

  private void scheduleSend(String auditId, Channel channel, String subject, String bodyMd,
                            int attempt, String eventType, Duration ttl, boolean hasPii, Duration delay) {
    scheduler.schedule(() -> doSend(auditId, channel, subject, bodyMd, attempt, eventType, ttl, hasPii),
                       Instant.now().plus(delay));
  }


  private void doSend(String auditId, Channel channel, String subject, String bodyMd,
                      int attempt, String eventType, Duration ttl, boolean hasPii) {
    RenderedMessage msg = new RenderedMessage(TenantContextHolder.get().correlationId(),
                                              TenantContextHolder.get().tenantId(), TenantContextHolder.get().userId(), channel, subject, bodyMd, Map.of());


    ChannelAdapter adapter = factory.resolve(channel);
    Boolean faulted = false;
    try {
      SendResult r = adapter.send(msg);
      repo.touchAttempt(auditId, attempt, maxAttempts);

      if (!faulted && r.success()) {
        repo.markSent(auditId, r.providerMessageId());
        if( eventType != EventType.ONBOARDING_REQUESTED.name() && eventType != EventType.ONBOARDING_ACCEPTED.name()) {
            log.log(TenantLogger.Category.AUDIT, INFO, "notification_sent", Map.of("auditId", auditId));
        }
        return;
      }



      boolean permanent = r.permanent();
      if (permanent) {
        String category = categoryResolver.resolve(r.errorCode()).orElse("Uncategorized");
        String ticketId = ticketLocator.findOpenTicket(projectId, trackerId, category)
                                       .orElseGet(() -> ticketLocator.createTicket(
                                               projectId, trackerId, category,
                                               "Notification delivery failed: " + channel + " (" + category + ")",
                                               "Template/event: " + eventType + "\nError: " + r.errorCode(),
                                               TenantContextHolder.get().correlationId()
                                                                                  ));
        repo.attachTicket(auditId, ticketId);  // persist on audit row
      }

      if (attempt >= maxAttempts || (ttl != null && ttl != Duration.ZERO && Instant.now().isAfter(repo.auditCreatedAt(auditId).plus(ttl)))) {
        repo.markFailed(auditId, r.errorCode(), permanent);
        return;
      }
    }
    catch (Exception e) {
      log.log(TenantLogger.Category.AUDIT, ERROR, "notification_delivery_error: " + e.getMessage() , Map.of("auditId", auditId));
    }
    Duration next = nextBackoff(attempt);
    scheduleSend(auditId, channel, subject, bodyMd, attempt+1, eventType, ttl, hasPii, next);
  }


  private Duration nextBackoff(int attempt) {
    double mult = Math.min(Math.pow(backoffMult, attempt-1), maxBackoff.dividedBy(initialBackoff));
    long millis = Math.round(initialBackoff.toMillis() * mult);
    return Duration.ofMillis(Math.min(millis, maxBackoff.toMillis()));
  }
}