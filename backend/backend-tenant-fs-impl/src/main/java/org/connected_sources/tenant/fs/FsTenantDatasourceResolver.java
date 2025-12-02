package org.connected_sources.tenant.fs;

import org.connected_sources.shared.tenantdb.DataSourceDescriptor;
import org.connected_sources.tenant.spi.TenantDatasourceResolver;

import javax.sql.DataSource;
import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

import org.connected_sources.tenant.spi.db.TenantDbMigrator;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;
import org.sqlite.SQLiteDataSource;
/**
 * Implementazione di TenantDatasourceResolver che costruisce dinamicamente
 * il path a un file SQLite per ciascun tenant in base a un base directory.
 *
 */
@Component
public class FsTenantDatasourceResolver implements TenantDatasourceResolver
{

  private final PathResolver pathResolver;
  private final Path baseDirectory;
  private final TenantDbMigrator migrator;

  public FsTenantDatasourceResolver(@Value("${tenant.base-directory}") Path baseDirectory,
                                    TenantDbMigrator migrator) {
    this.baseDirectory = Objects.requireNonNull(baseDirectory);
    this.migrator = migrator;
    this.pathResolver = new PathResolver(baseDirectory);
  }

  @Override
  public DataSource createDataSource(String tenantId, @NotNull DataSourceDescriptor descriptor) throws RuntimeException  {
    String url = descriptor.url();
    if( url == null || url.isBlank()) {
      if (tenantId == null || tenantId.isBlank()) {
        throw new IllegalArgumentException("Tenant ID must not be null or blank");
      }

      try {
        final SQLiteDataSource dataSource = getSqLiteDataSource(tenantId);
        migrator.migrate(dataSource, tenantId);

//      // Validate connection
//      try (var conn = dataSource.getConnection()) {
//        //TODO log or check db version here
//      }

        return dataSource;
      } catch (/*SQLException e*/ Exception e) {
        throw new RuntimeException("Failed to create DataSource for tenant: " + tenantId, e);
      }
    }

    var ds = new DriverManagerDataSource();

    switch (descriptor.provider()) {
      case SQLITE -> ds.setDriverClassName("org.sqlite.JDBC");
      default -> throw new IllegalArgumentException("Unsupported provider " + descriptor.provider());
    }

    ds.setUrl(url);
    return ds;
  }

  private @NotNull SQLiteDataSource getSqLiteDataSource(String tenantId) {
    PathResolver pathResolver = new PathResolver(this.baseDirectory);
    Path dbPath = pathResolver.sqlitePath(tenantId);
    File tenantDbFile = dbPath.toFile();


    // Ensure parent directory exists
    File parentDir = tenantDbFile.getParentFile();
    if (!parentDir.exists() && !parentDir.mkdirs()) {
      throw new IllegalStateException("Failed to create directories for tenant: " + tenantId);
    }

    String jdbcUrl = "jdbc:sqlite:" + tenantDbFile.getAbsolutePath();
    SQLiteDataSource dataSource = new SQLiteDataSource();
    dataSource.setUrl(jdbcUrl);
    return dataSource;
  }
}
