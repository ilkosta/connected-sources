package org.connected_sources.tenantdb;

import org.connected_sources.tenant.spi.db.TenantDbMigrator;
import org.connected_sources.tenantdb.TenantMigrationProperties;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.util.List;

/* TEACHER
 * ------------------
 * Flyway order matters: baseline -> schema -> seeds.
 * Seeds provide roles/categories/templates used by the app right after enable.
 * SQLite specifics:
 *  - DDL auto-commit: group related statements in a tx via Flyway callback.
 *  - Vacuum is skipped on first run to reduce latency.
 */
@Component
public class SqliteTenantDbMigrator implements TenantDbMigrator {

  private final TenantMigrationProperties properties;

  public SqliteTenantDbMigrator(TenantMigrationProperties properties) {
    this.properties = properties;
  }

  @Override
  public void migrate(DataSource tenantDataSource, String tenantId) {
    if (properties.isEnabled()) {
      // for sqlite tenantId isn't useful...
      Flyway.configure()
              .dataSource(tenantDataSource)
              .locations(properties.getLocations())                  // e.g. classpath:/tenant-sqlite/migration
              .table(properties.getHistoryTable())        // per-tenant history
              .baselineOnMigrate(properties.isBaselineOnMigrate())
              .load()
              .migrate();
    }
  }
}
