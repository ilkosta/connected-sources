package org.connected_sources.tenant.fs;

import java.nio.file.Path;

public class PathResolver {
  private final Path baseDir;
  public PathResolver(Path baseDir) { this.baseDir = baseDir; }

  public Path tenantRoot(String tenantId) {
    return baseDir.resolve(tenantId);
  }
  public Path sqlitePath(String tenantId) {
    return tenantRoot(tenantId).resolve("datasource.sqlite");
  }
  public Path logs(String tenantId) {
    return tenantRoot(tenantId).resolve("logs/");
  }
}

