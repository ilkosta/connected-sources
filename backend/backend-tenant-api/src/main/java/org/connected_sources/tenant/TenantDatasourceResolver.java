package org.connected_sources.tenant;

import javax.sql.DataSource;

public interface TenantDatasourceResolver {

  /**
   * Creates and returns a DataSource instance for the given tenantId.
   *
   * @param tenantId The unique identifier for the tenant
   * @return A configured, tenant-specific DataSource
   * @throws IllegalArgumentException if tenantId is null or invalid
   * @throws RuntimeException if DataSource creation fails
   */
  DataSource createDataSource(String tenantId);
}