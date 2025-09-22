package org.connected_sources.api.dto.onboarding;

import org.connected_sources.shared.onboarding.OnboardingState;

public record OnboardingView(
        Long id,
        OnboardingState state,
        String producerName,
        String email
) implements OnboardingViewInterface {

  public OnboardingView(Long id, OnboardingState state, String producerName, String email) {
    if (state.equals(OnboardingState.APPROVED)) {
      throw new IllegalArgumentException("Use the specific implementation ApprovedOnboardingView");
    }

    this.id = id;
    this.state = state;
    this.producerName = producerName;
    this.email = email;
  }

  public static OnboardingView of(long id, String s) throws IllegalArgumentException {
    return new OnboardingView(id, OnboardingState.fromString(s), "", "");
  }
}

