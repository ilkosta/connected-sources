package org.connected_sources.api.web.onboarding;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import org.connected_sources.api.dto.OnboardingRequest;
//import org.connected_sources.core.user.OnboardingService;
import org.connected_sources.core.user.onboarding.model.OnboardingRequestCmd;
import org.connected_sources.core.user.onboarding.model.OnboardingSummary;
import org.connected_sources.core.user.onboarding.repo.OnboardingRepo;
import org.connected_sources.shared.context.TenantContextHolder;
import org.connected_sources.shared.onboarding.OnboardingState;
import org.springframework.http.ResponseEntity;
import org.connected_sources.api.dto.onboarding.OnboardingView;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/onboard")
public class OnboardingController {

//  private final OnboardingService onboardingService;
  private final OnboardingRepo repo;

  public OnboardingController(OnboardingRepo repo) {
    this.repo = repo;
  }

  @PostMapping
  public ResponseEntity<OnboardingView> onboard(@Valid @RequestBody org.connected_sources.api.dto.onboarding.OnboardingRequestCreate in) throws JsonProcessingException {
    var tc = TenantContextHolder.get();
    final Long userId = tc.userId();
    var cmd = new OnboardingRequestCmd(
            userId.longValue(),
            in.producerName(), in.email(), in.website(), in.vatOrFiscalCode()
    );
    long existingId = repo.createOrReuseRequest(cmd, tc.correlationId());
    org.connected_sources.api.dto.onboarding.OnboardingView view = repo.findSummary(existingId)
            .map(this::toView)
            .orElse(new org.connected_sources.api.dto.onboarding.OnboardingView(existingId, OnboardingState.REQUESTED, in.producerName(), in.email()));
    return ResponseEntity.accepted().body(view);
  }

  // ---- mapper ----
  private OnboardingView toView(OnboardingSummary s) {
    return new org.connected_sources.api.dto.onboarding.OnboardingView(s.id(), s.state(), s.producerName(), s.email());
  }
}