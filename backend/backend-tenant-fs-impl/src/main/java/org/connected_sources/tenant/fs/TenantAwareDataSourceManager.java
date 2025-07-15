package org.connected_sources.tenant.fs;

import org.connected_sources.tenant.TenantContextHolder;
import org.connected_sources.tenant.TenantDatasourceRegistry;
import org.connected_sources.shared.TenantLifecycleManager;

import javax.sql.DataSource;

public class TenantAwareDataSourceManager {

  private final TenantContextHolder tenantContextHolder;
  private final TenantDatasourceRegistry tenantDatasourceRegistry;
  private final TenantLifecycleManager tenantLifecycleManager;

  public TenantAwareDataSourceManager(
          TenantContextHolder tenantContextHolder,
          TenantDatasourceRegistry tenantDatasourceRegistry,
          TenantLifecycleManager tenantLifecycleManager
                                     ) {
    this.tenantContextHolder = tenantContextHolder;
    this.tenantDatasourceRegistry = tenantDatasourceRegistry;
    this.tenantLifecycleManager = tenantLifecycleManager;
  }

  public DataSource resolveDataSource() {
    String tenantId = tenantContextHolder.getTenantId()
                                         .orElseThrow(() -> new IllegalStateException("No tenant set"));
    DataSource cached = tenantDatasourceRegistry.getDataSource(tenantId);
    if (cached != null) return cached;

    tenantLifecycleManager.provisionTenant(tenantId);
    return tenantDatasourceRegistry.getDataSource(tenantId);
  }

  private DataSource resolveDataSourceForTenant(String tenantId) {
    DataSource existing = tenantDatasourceRegistry.getDataSource(tenantId);
    if (existing != null) {
      return existing;
    }

    tenantLifecycleManager.provisionTenant(tenantId);
    DataSource provisioned = tenantDatasourceRegistry.getDataSource(tenantId);
    if (provisioned == null) {
      throw new IllegalStateException("DataSource unavailable after provisioning for tenant: " + tenantId);
    }

    return provisioned;
  }
}
