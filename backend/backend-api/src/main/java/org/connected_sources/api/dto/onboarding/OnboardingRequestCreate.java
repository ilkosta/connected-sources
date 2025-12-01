package org.connected_sources.api.dto.onboarding;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record OnboardingRequestCreate(
        @NotBlank String producerName,
        @NotBlank @Email String email,
        String website,
        String vatOrFiscalCode,
        // requesterUserId remains implicit from auth; do not expose here
        @Positive Long requesterUserId // TODO: check if necessary
) {}
