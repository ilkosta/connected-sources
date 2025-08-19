package org.connected_sources.api.dto.onboarding;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record OnboardingRequestCreate(
        @NotBlank String producerName,
        @NotBlank @Email String email,
        String website,
        String vatOrFiscalCode,
        // requesterUserId remains implicit from auth; do not expose here
        Long requesterUserId // TODO: check if necessary
) {}
