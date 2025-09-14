package org.connected_sources.tenant.spi;

import javax.sql.DataSource;

public interface TenantAwareDataSourceManager {
    DataSource resolveDataSource();
    public TenantDatasourceRegistry tenantDatasourceRegistry();
    public TenantLifecycleManager tenantLifecycleManager();
    default DataSource resolveDataSourceForTenant(String tenantId) {
        final TenantDatasourceRegistry tenantDatasourceRegistry = tenantDatasourceRegistry();
        DataSource existing = tenantDatasourceRegistry.getDataSource(tenantId);
        if (existing != null) {
            return existing;
        }

        tenantLifecycleManager().provisionTenant(tenantId);
        DataSource provisioned = tenantDatasourceRegistry.getDataSource(tenantId);
        if (provisioned == null) {
            throw new IllegalStateException("DataSource unavailable after provisioning for tenant: " + tenantId);
        }

        return provisioned;
    }
}
