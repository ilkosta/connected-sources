package org.connected_sources.core.user.onboarding.model;


public record OnboardingRequestCmd(
        long requesterUserId,
        String producerName,
        String email,
        String website,
        String vatOrFiscalCode
) {}
