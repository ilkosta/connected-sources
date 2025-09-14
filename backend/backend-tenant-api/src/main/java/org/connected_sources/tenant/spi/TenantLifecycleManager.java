package org.connected_sources.tenant.spi;

import javax.sql.DataSource;
import java.nio.file.Path;

public interface TenantLifecycleManager {
  /** Create idempotently the tenant’s FS/DB artifacts. Safe if called twice. */
  void provisionTenant(String tenantId);

  /** Best-effort cleanup of tenant artifacts (used on FAILED). */
  void deleteTenantArtifacts(String tenantId);

  /** Resolve the tenant root directory (must exist after provision). */
  Path tenantRoot(String tenantId);

  /** Location of per-tenant SQLite DB file. */
  Path sqlitePath(String tenantId);

  /** Resolve a DataSource for the tenant SQLite DB (after provision). */
  DataSource sqlite(String tenantId);
}
