package org.connected_sources.api.dto.onboarding;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * Payload for POST /onboarding/requests/{id}/register-producer
 */
public record RegistrationPayload(
        @NotBlank @Email String producerAdminEmail,

        /**
         * Optional human hint (e.g. "Acme Corp").
         * Will be normalized into tenantId; uniqueness enforced.
         */
        String tenantIdHint,

        /**
         * List of userIds to be created as initial members of the tenant.
         * The requester can be included here explicitly as ADMIN.
         */
        List<Long> initialUsers
) {}
