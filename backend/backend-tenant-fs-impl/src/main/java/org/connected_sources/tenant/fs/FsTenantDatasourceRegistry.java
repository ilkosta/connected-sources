package org.connected_sources.tenant.fs;

import org.connected_sources.shared.tenantdb.DataSourceDescriptor;
import org.connected_sources.tenant.spi.TenantDatasourceRegistry;
import org.connected_sources.tenant.spi.db.TenantDescriptorStore;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Objects;

/**
 * responsabilità:
 *  Registry is a cache that maps tenant identifiers to their DataSource instances.
 *  on cache miss read the descriptor from Postgres
 * Responsabilità:
 * - Memorizzazione nella cache di istanze di DataSource specifiche per tenant (una per tenant)
 *  - on cache miss read the descriptor from Postgres
 * - Registrazione di nuove origini dati *
 */
@Component
public class FsTenantDatasourceRegistry implements TenantDatasourceRegistry {

  private final Map<String, DataSource> cache = new ConcurrentHashMap<>();
  private final FsTenantDatasourceResolver datasourceResolver;
  private final TenantDescriptorStore store;
//  private final Map<DbProvider, TenantDatasourceResolver> resolvers = new EnumMap<>(DbProvider.class);

  public FsTenantDatasourceRegistry(TenantDescriptorStore store, FsTenantDatasourceResolver datasourceResolver) {
    this.store = store;
    this.datasourceResolver = Objects.requireNonNull(datasourceResolver);
  }

  @Override
  public DataSource getDataSource(String tenantId) {
    if (tenantId == null || tenantId.isBlank()) {
      throw new IllegalArgumentException("Tenant ID must not be null or blank");
    }
    var cached = cache.get(tenantId);
    if (cached != null)
      return cached;

    DataSourceDescriptor d = store.readDescriptor(tenantId);
    var ds = datasourceResolver.createDataSource(tenantId, d);
    return cache.computeIfAbsent(tenantId, __ -> ds);
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

  public void evict(String tenantId) { cache.remove(tenantId); }
}
