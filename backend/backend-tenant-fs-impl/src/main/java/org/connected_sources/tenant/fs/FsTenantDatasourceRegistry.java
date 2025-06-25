package org.connected_sources.tenant.fs;

import org.connected_sources.tenant.TenantDatasourceRegistry;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Objects;

/**
 * responsabilità:
 *
 * Responsabilità:
 * - Memorizzazione nella cache di istanze di DataSource specifiche per tenant (una per tenant)
 * - Interazione con un FsTenantDatasourceResolver collegabile per la creazione di istanze di DataSource su richiesta
 * - Registrazione di nuove origini dati
 */
public class FsTenantDatasourceRegistry implements TenantDatasourceRegistry {

  private final Map<String, DataSource> cache = new ConcurrentHashMap<>();
  private final FsTenantDatasourceResolver datasourceResolver;

  public FsTenantDatasourceRegistry(FsTenantDatasourceResolver datasourceResolver) {
    this.datasourceResolver = Objects.requireNonNull(datasourceResolver);
  }

  @Override
  public DataSource getDataSource(String tenantId) {
    if (tenantId == null || tenantId.isBlank()) {
      throw new IllegalArgumentException("Tenant ID must not be null or blank");
    }

    return cache.computeIfAbsent(tenantId, datasourceResolver::createDataSource);
  }



  @Override
  public void registerDataSource(String tenantId, DataSource ds) {
    if (tenantId == null || ds == null) {
      throw new IllegalArgumentException("Tenant ID and DataSource must not be null");
    }

    cache.put(tenantId, ds);
  }

  /**
   * Checks whether the registry already contains a DataSource for the tenant.
   *
   * @param tenantId the tenant identifier
   * @return true if registered, false otherwise
   */
  @Override
  public boolean containsTenant(String tenantId) {
    return cache.containsKey(tenantId);
  }
}
