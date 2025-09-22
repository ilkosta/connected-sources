package org.connected_sources.api.dto.onboarding;

import org.connected_sources.shared.onboarding.OnboardingState;

public record ApprovedOnboardingView(Long id, OnboardingState state, String producerName, String email, String token)
        implements OnboardingViewInterface {

  public ApprovedOnboardingView(Long id, OnboardingState state, String producerName, String email, String token) {
    if (!state.equals(OnboardingState.APPROVED)) {
      throw new IllegalArgumentException("Stato deve essere APPROVED");
    }

    this.id = id;
    this.state = state;
    this.producerName = producerName;
    this.email = email;
    this.token = token;
  }
}
