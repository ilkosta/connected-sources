package org.connected_sources.tenant.logger;


import org.connected_sources.tenant.fs.PathResolver;
import org.connected_sources.tenant.spi.TenantLogLocator;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Implementazione locale: delega a PathResolver e garantisce l'esistenza della dir.
 */
@Component
public class FsTenantLogLocator implements TenantLogLocator {

    private final PathResolver paths;

    public FsTenantLogLocator(PathResolver paths) {
        this.paths = paths;
    }

    @Override
    public Path logsDir(String tenantId) throws IOException {
        Path dir = paths.tenantRoot(tenantId).resolve("logs").resolve("fs");
        Files.createDirectories(dir);
        return dir;
    }
}
