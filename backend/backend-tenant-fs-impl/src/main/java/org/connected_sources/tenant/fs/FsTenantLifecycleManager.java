package org.connected_sources.tenant.fs;

import org.connected_sources.tenant.TenantDatasourceRegistry;
import org.connected_sources.shared.TenantLifecycleManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class FsTenantLifecycleManager implements TenantLifecycleManager {

    private final FsTenantDatasourceResolver datasourceResolver;
    private final TenantDatasourceRegistry registry;

    public FsTenantLifecycleManager(FsTenantDatasourceResolver datasourceResolver,
                                    TenantDatasourceRegistry registry) {
        this.datasourceResolver = Objects.requireNonNull(datasourceResolver);
        this.registry = Objects.requireNonNull(registry);
    }

    public void provisionTenant(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("tenantId must not be null or blank");
        }

        Path dbFile = Path.of("/base-directory", tenantId, "datasource.sqlite");
        try {
            Files.createDirectories(dbFile.getParent());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.sqlite.JDBC");
        ds.setUrl("jdbc:sqlite:" + dbFile.toAbsolutePath());

        registry.registerDataSource(tenantId, ds);
    }
}
