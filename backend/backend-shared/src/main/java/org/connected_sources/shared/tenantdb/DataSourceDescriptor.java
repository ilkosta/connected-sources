package org.connected_sources.shared.tenantdb;

import java.util.Map;

public record DataSourceDescriptor(
        DbProvider provider,
        String url,
        String username,
        String password,
        Map<String,Object> pool // optional tuning (minIdle, maxPoolSize, etc.)
) {}
