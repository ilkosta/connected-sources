package org.connected_sources.tenant;

import javax.sql.DataSource;

/**
 * Registry that maps tenant identifiers to their DataSource instances.
 * Expected to be thread-safe and updated dynamically.
 */
public interface TenantDatasourceRegistry {

  /**
   * Gets the DataSource associated with the given tenant ID.
   *
   * @param tenantId the tenant identifier
   * @return the associated DataSource, or null if none is registered
   */
  DataSource getDataSource(String tenantId);

  /**
   * Registers a DataSource for the specified tenant ID.
   *
   * @param tenantId the tenant identifier
   * @param dataSource the DataSource instance
   */
  void registerDataSource(String tenantId, DataSource dataSource);

  /**
   * Checks whether the registry already contains a DataSource for the tenant.
   *
   * @param tenantId the tenant identifier
   * @return true if registered, false otherwise
   */
  boolean containsTenant(String tenantId);
}
