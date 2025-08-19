package org.connected_sources.shared.onboarding;

public enum OnboardingState {
  REQUESTED,
  APPROVED,
  PREPARATION,
  ENABLED,
  FAILED,
  EXPIRED,
  REJECTED;

  public static OnboardingState fromString(String value) throws IllegalArgumentException {
    if (value == null) throw new IllegalArgumentException("stato non definito (null)");
    return OnboardingState.valueOf(value.trim().toUpperCase());
  }
}