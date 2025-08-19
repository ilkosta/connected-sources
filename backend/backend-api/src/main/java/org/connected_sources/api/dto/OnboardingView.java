package org.connected_sources.api.dto.onboarding;

import org.connected_sources.shared.onboarding.OnboardingState;

public record OnboardingView(
        Long id,
        OnboardingState state,
        String producerName,
        String email
) {}

