package org.connected_sources.tenant.fs;

import org.connected_sources.tenant.TenantDatasourceRegistry;
import org.connected_sources.shared.TenantLifecycleManager;

import javax.sql.DataSource;
import java.util.Objects;

public class FsTenantLifecycleManager implements TenantLifecycleManager {

    private final FsTenantDatasourceResolver datasourceResolver;
    private final TenantDatasourceRegistry registry;

    public FsTenantLifecycleManager(FsTenantDatasourceResolver datasourceResolver,
                                    TenantDatasourceRegistry registry) {
        this.datasourceResolver = Objects.requireNonNull(datasourceResolver);
        this.registry = Objects.requireNonNull(registry);
    }

    public void provisionTenant(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("tenantId must not be null or blank");
        }

        DataSource dataSource = datasourceResolver.createDataSource(tenantId);
        registry.registerDataSource(tenantId, dataSource);
    }
}
