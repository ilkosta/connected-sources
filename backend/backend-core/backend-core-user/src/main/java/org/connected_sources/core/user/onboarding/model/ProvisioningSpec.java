package org.connected_sources.core.user.onboarding.model;

import java.util.List;

public record ProvisioningSpec(
        String producerAdminEmail,
        List<Long> initialUsers
) {}