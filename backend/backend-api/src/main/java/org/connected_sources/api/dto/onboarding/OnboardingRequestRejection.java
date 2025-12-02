package org.connected_sources.api.dto.onboarding;

import jakarta.validation.constraints.NotBlank;

public record OnboardingRequestRejection (
    Long id,
    @NotBlank String reason,
    String hints
){}
