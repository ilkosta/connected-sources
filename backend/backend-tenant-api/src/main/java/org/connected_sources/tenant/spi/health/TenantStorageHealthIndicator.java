package org.connected_sources.tenant.spi.health;

import org.springframework.boot.actuate.health.HealthIndicator;

public interface TenantStorageHealthIndicator extends HealthIndicator {
    String getStorageType();
    boolean supports(String storageType);
}
