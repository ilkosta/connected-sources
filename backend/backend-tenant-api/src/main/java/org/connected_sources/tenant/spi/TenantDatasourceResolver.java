package org.connected_sources.tenant.spi;

import org.connected_sources.shared.tenantdb.DataSourceDescriptor;

import javax.sql.DataSource;

/**
 * build a DataSource from the descriptor (provider-specific logic)
 */
public interface TenantDatasourceResolver {

  /**
   * Build a DataSource from a provider-agnostic descriptor.
   * tenantId is provided for optional legacy fallbacks (e.g., compute SQLite path if URL missing).
   */
  DataSource createDataSource(String tenantId, DataSourceDescriptor descriptor);
}