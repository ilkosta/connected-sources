package org.connected_sources.tenant.spi.db;

import org.connected_sources.shared.tenantdb.DataSourceDescriptor;

import java.util.Objects;

public interface TenantResourcePlanner {
  TenantResourcesPlan plan(String tenantId);

  record TenantResourcesPlan(
          String tenantId,
          String baseDir,                 // absolute base dir for the tenant
          String sqlitePath,              // absolute path to sqlite file (if SQLITE)
          DataSourceDescriptor dsDesc     // provider-agnostic DS descriptor
  ) {
    public TenantResourcesPlan {
      Objects.requireNonNull(tenantId, "tenantId");
      Objects.requireNonNull(baseDir, "baseDir");
      Objects.requireNonNull(sqlitePath, "sqlitePath");
      Objects.requireNonNull(dsDesc, "dsDesc");
    }
  }
}
