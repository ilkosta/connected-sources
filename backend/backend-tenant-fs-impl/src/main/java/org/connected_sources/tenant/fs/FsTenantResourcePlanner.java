package org.connected_sources.tenant.fs;

import org.connected_sources.shared.tenantdb.DataSourceDescriptor;
import org.connected_sources.shared.tenantdb.DbProvider;
import org.connected_sources.tenant.spi.db.TenantResourcePlanner;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FsTenantResourcePlanner implements TenantResourcePlanner {

  private final PathResolver paths;

  public FsTenantResourcePlanner(PathResolver paths) {
    this.paths = paths;
  }

  @Override
  public TenantResourcesPlan plan(String tenantId) {
    var baseDir = paths.tenantRoot(tenantId).toAbsolutePath().toString();
    var sqlite   = paths.sqlitePath(tenantId).toAbsolutePath().toString();

    var ds = new DataSourceDescriptor(
            DbProvider.SQLITE,
            "jdbc:sqlite:" + sqlite,
            null, null,
            Map.of("maxPoolSize", 5, "minIdle", 1)
    );

    return new TenantResourcesPlan(tenantId, baseDir, sqlite, ds);
  }
}
