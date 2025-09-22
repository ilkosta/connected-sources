package org.connected_sources.api.health;

//import java.nio.file.*;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.actuate.health.*;
//import org.springframework.stereotype.Component;
//import org.connected_sources.tenant.fs.FsTenantLifecycleManager;
//

//@Component
//public class FsSpaceHealthIndicator implements HealthIndicator {
//
//  @Value("${health.fs.min_available:1000000000}") long minSpaceAvailable;
//
//  private final FsTenantLifecycleManager fs;
//  public FsSpaceHealthIndicator(FsTenantLifecycleManager fs) { this.fs = fs; }
//  @Override public Health health() {
//    try {
//      FileStore store = Files.getFileStore(fs.tenantRoot("default"));
//      long free = store.getUsableSpace();
//      return free > minSpaceAvailable ? Health.up().withDetail("free", free).build()
//              : Health.down().withDetail("free", free).build();
//    } catch (Exception e) { return Health.down(e).build(); }
//  }
//}