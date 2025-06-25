package org.connected_sources.tenant.fs;

import org.connected_sources.tenant.TenantDatasourceResolver;

import javax.sql.DataSource;
import java.io.File;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Objects;
import org.sqlite.SQLiteDataSource;
/**
 * Implementazione di TenantDatasourceResolver che costruisce dinamicamente
 * il path a un file SQLite per ciascun tenant in base a un base directory.
 *
 */
public class FsTenantDatasourceResolver implements TenantDatasourceResolver
{

  private final Path baseDirectory;

  public FsTenantDatasourceResolver(Path baseDirectory) {
    this.baseDirectory = Objects.requireNonNull(baseDirectory);
  }

  public DataSource createDataSource(String tenantId) throws RuntimeException  {
    if (tenantId == null || tenantId.isBlank()) {
      throw new IllegalArgumentException("Tenant ID must not be null or blank");
    }

    try {
      Path dbPath = baseDirectory.resolve(tenantId).resolve("datasource.sqlite");
      File tenantDbFile = dbPath.toFile();

      // Ensure parent directory exists
      File parentDir = tenantDbFile.getParentFile();
      if (!parentDir.exists() && !parentDir.mkdirs()) {
        throw new IllegalStateException("Failed to create directories for tenant: " + tenantId);
      }

      String jdbcUrl = "jdbc:sqlite:" + tenantDbFile.getAbsolutePath();
      SQLiteDataSource dataSource = new SQLiteDataSource();
      dataSource.setUrl(jdbcUrl);

      // Validate connection
      try (var conn = dataSource.getConnection()) {
        // optionally log or check db version here
        //TODO
      }

      return dataSource;
    } catch (SQLException e) {
      throw new RuntimeException("Failed to create DataSource for tenant: " + tenantId, e);
    }
  }
}
