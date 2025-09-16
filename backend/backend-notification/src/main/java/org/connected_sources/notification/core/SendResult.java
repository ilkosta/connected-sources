package org.connected_sources.notification.core;

public record SendResult(boolean success, String providerMessageId, String errorCode, boolean permanent) {}
