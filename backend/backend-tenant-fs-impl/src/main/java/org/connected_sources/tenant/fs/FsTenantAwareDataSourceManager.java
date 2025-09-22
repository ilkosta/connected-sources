package org.connected_sources.tenant.fs;

import org.connected_sources.shared.context.TenantContextHolder;
import org.connected_sources.tenant.spi.TenantAwareDataSourceManager;
import org.connected_sources.tenant.spi.TenantDatasourceRegistry;
import org.connected_sources.tenant.spi.TenantLifecycleManager;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class FsTenantAwareDataSourceManager implements TenantAwareDataSourceManager {

  private final TenantContextHolder tenantContextHolder;
  private final TenantDatasourceRegistry tenantDatasourceRegistry;
  private final TenantLifecycleManager tenantLifecycleManager;

  public FsTenantAwareDataSourceManager(
          TenantContextHolder tenantContextHolder,
          TenantDatasourceRegistry tenantDatasourceRegistry,
          TenantLifecycleManager tenantLifecycleManager
                                     ) {
    this.tenantContextHolder = tenantContextHolder;
    this.tenantDatasourceRegistry = tenantDatasourceRegistry;
    this.tenantLifecycleManager = tenantLifecycleManager;
  }

  @Override
  public DataSource resolveDataSource() {
    String tenantId = tenantContextHolder.get().tenantId();
//                                         .orElseThrow(() -> new IllegalStateException("No tenant set"));
    DataSource cached = tenantDatasourceRegistry.getDataSource(tenantId);
    if (cached != null) return cached;
    else {
      tenantLifecycleManager.provisionTenant(tenantId);
      return tenantDatasourceRegistry.getDataSource(tenantId);
    }
  }

  @Override
  public TenantDatasourceRegistry tenantDatasourceRegistry() {
    return tenantDatasourceRegistry;
  }

  @Override
  public TenantLifecycleManager tenantLifecycleManager() {
    return tenantLifecycleManager;
  }

}
