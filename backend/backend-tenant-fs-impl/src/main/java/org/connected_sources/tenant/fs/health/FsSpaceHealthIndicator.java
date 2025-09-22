package org.connected_sources.tenant.fs.health;

import org.connected_sources.tenant.fs.FsTenantLifecycleManager;
import org.connected_sources.tenant.spi.health.TenantStorageHealthIndicator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.file.FileStore;
import java.nio.file.Files;

// in backend-tenant-fs-impl
@Component
@ConditionalOnProperty(name = "tenant.storage.type", havingValue = "fs")
public class FsSpaceHealthIndicator implements TenantStorageHealthIndicator {

    private final FsTenantLifecycleManager lifecycleManager;
    @Value("${health.fs.min_available:1000000000}") long minSpaceAvailable;

    public FsSpaceHealthIndicator(FsTenantLifecycleManager lifecycleManager) {
        this.lifecycleManager = lifecycleManager;
    }

    @Override
    public String getStorageType() {
        return "filesystem";
    }

    @Override
    public boolean supports(String storageType) {
        return "fs".equals(storageType);
    }

    @Override
    public Health health() {

        try {
            FileStore store = Files.getFileStore(lifecycleManager.tenantRoot("default"));
            long free = store.getUsableSpace();
            return free > minSpaceAvailable ? Health.up().withDetail("free", free).build()
                    : Health.down().withDetail("free", free).build();
        } catch (Exception e) { return Health.down(e).build(); }
    }
}