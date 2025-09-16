package org.connected_sources.notification.core;

import java.util.Map;

public record RenderedMessage(
        String correlationId,
        String tenantId,
        Channel channel,
        String subject,
        String body
) {}