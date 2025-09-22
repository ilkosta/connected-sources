package org.connected_sources.tenant.fs;

import org.connected_sources.shared.context.TenantContextHolder;
import org.connected_sources.tenant.spi.TenantDatasourceRegistry;
import org.connected_sources.tenant.spi.TenantDatasourceResolver;
import org.connected_sources.tenant.spi.TenantLifecycleManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/* DESIGN
 * -----------------
 * FS layout per tenant:
 *   {base}/{tenantId}/
 *     db/datasource.sqlite
 *     logs/fs/YYYY-MM-DD.jsonl (rotated)
 *     content/
 *
 * Rationale:
 *  - Deterministic paths for ops & cleanup
 *  - Writable hierarchy checked by health probes before enabling tenant
 */
// GUIDE
// Always create parent dirs first, then the SQLite file. If dir creation
// fails, abort early.
// If DB creation fails everything is left as is to facilitate diagnostics
@Component
public class FsTenantLifecycleManager implements TenantLifecycleManager {

    private final TenantDatasourceResolver datasourceResolver;
    private final TenantDatasourceRegistry registry;

    private final Path root;
    private final PathResolver pathResolver;
//    private final Path root = Path.of(root_param);
//    private final ConcurrentHashMap<String, DataSource> sqliteByTenant = new ConcurrentHashMap<>();

    public FsTenantLifecycleManager(TenantDatasourceResolver datasourceResolver,
                                    TenantDatasourceRegistry registry,
                                   @Value("${tenant.base-directory}") String rootDir) {
        this.datasourceResolver = Objects.requireNonNull(datasourceResolver);
        this.registry = Objects.requireNonNull(registry);
        this.root = Path.of(rootDir);
        this.pathResolver = new PathResolver(this.root);
    }


    // idempotent
    @Override
    public void provisionTenant(String tenantId) throws RuntimeException {
        assert(root != null);
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("tenantId must not be null or blank");
        }

        try {
          Files.createDirectories(sqlitePath(tenantId).getParent());
          Files.createDirectories(fsLogDir(tenantId));
          Files.createDirectories(tenantRoot(tenantId).resolve("content"));
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        DataSource ds = registry.getDataSource(tenantId);
//        try {

//          DataSource ds = datasourceResolver.createDataSource(tenantId,null );
//          registry.registerDataSource(tenantId,ds);
//        }
//        catch (RuntimeException e) {
//v
//        }
    }

  @Override
  public void deleteTenantArtifacts(String tenantId) {
    // do nothing at the moment...
    // does not change the status to allow the analysis of the incident
    // and the continuation of the recovery operations
  }

  public DataSource sqlite(String tenantId) {
      return registry.getDataSource(tenantId);
  }

  public DataSource sqliteCurrentTenant() {
    String tenantId = TenantContextHolder.get().tenantId();
    return sqlite(tenantId);
  }


  public Path tenantRoot(String tenantId) {
      return pathResolver.tenantRoot(tenantId);
    }
  public Path sqlitePath(String tenantId) {
      return pathResolver.sqlitePath(tenantId);
    }
  public Path fsLogDir(String tenantId) {
      return pathResolver.logs(tenantId);
    }
}
