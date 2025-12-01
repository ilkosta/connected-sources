package org.connected_sources.notification.core;

import java.util.Map;

public record RenderedMessage(
        String correlationId,
        String tenantId,
        Long userId,
        Channel channel,
        String subject,
        String bodyMd,
        String recipient,
        Map<String,Object> providerHints
) {}