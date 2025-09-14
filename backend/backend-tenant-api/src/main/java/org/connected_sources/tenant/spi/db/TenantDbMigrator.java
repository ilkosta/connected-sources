package org.connected_sources.tenant.spi.db;
import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

/**
 * SPI for per-tenant DB migrations (e.g., SQLite, future backends).
 * Implementations must be idempotent: run only pending migrations.
 */
public interface TenantDbMigrator {

  /**
   * Apply (or no-op) all pending migrations for the given tenant datasource.
   * Implementations may use vendor-specific tools (e.g., Flyway).
   */
  void migrate(DataSource dataSource,
               String tenantId);

  /**
   * Optional: check readiness (e.g., required tables exist).
   * Default no-op for implementations that don't need it.
   */
  default void validate(DataSource tenantDataSource, String tenantId) throws Exception { }
}
