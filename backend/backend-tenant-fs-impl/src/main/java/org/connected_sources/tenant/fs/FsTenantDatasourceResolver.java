package org.connected_sources.tenant.fs;

import org.connected_sources.tenant.TenantDatasourceResolver;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Implementazione di TenantDatasourceResolver che costruisce dinamicamente
 * il path a un file SQLite per ciascun tenant in base a un base directory.
 */
public class FsTenantDatasourceResolver implements TenantDatasourceResolver {

  private final String baseDirectory;

  public FsTenantDatasourceResolver(String baseDirectory) {
    this.baseDirectory = baseDirectory;
  }

  @Override
  public String resolvePathForTenant(String tenantId) {
    Path path = Paths.get(baseDirectory, tenantId, "datasource.sqlite");
    return path.toString();
  }
}
