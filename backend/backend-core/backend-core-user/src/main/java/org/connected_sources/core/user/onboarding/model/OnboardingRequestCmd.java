package org.connected_sources.core.user.onboarding.model;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record OnboardingRequestCmd(
        @Positive long requesterUserId,
        @NotBlank String producerName,
        @NotBlank @Email String email,
        String website,
        String vatOrFiscalCode
) {}
