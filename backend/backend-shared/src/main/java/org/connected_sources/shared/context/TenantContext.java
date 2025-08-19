package org.connected_sources.shared.context;

public record TenantContext(String tenantId, Long userId, String correlationId) {}