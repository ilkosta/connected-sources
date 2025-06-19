package org.connected_sources.tenant;


import org.connected_sources.shared.TenantLifecycleManager;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FsTenantLifecycleManager implements TenantLifecycleManager {

    private final Path baseDirectory;

    public FsTenantLifecycleManager(String baseDir) {
        this.baseDirectory = Paths.get(baseDir);
    }

    @Override
    public void createTenant(String tenantId) {
        try {
            Path tenantPath = baseDirectory.resolve(tenantId);
            Files.createDirectories(tenantPath);

            // Create placeholder SQLite datasource
            Path sqliteFile = tenantPath.resolve("datasource.sqlite");
            if (!Files.exists(sqliteFile)) {
                Files.createFile(sqliteFile); // In a real setup, you might initialize schema here
            }

            // Optionally initialize logging, git repo, etc.
        } catch (Exception e) {
            throw new RuntimeException("Failed to create tenant directory for " + tenantId, e);
        }
    }
}