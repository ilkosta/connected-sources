package org.connected_sources.core.user.async;

public record OpsExecutorSettings(int corePoolSize, int maxPoolSize, int queueCapacity, String threadNamePrefix) {}
