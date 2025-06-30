package org.connected_sources.api.e2e;

import org.connected_sources.tenant.TenantContextHolder;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@Profile("test")
public class PingController {

  private final TenantContextHolder tenantContextHolder;

  public PingController(TenantContextHolder tenantContextHolder) {
    this.tenantContextHolder = tenantContextHolder;
  }

  @PostMapping("/ping")
  public ResponseEntity<String> ping() {
    String tenantId = tenantContextHolder.getTenantId();
    if (tenantId == null) {
      return ResponseEntity.badRequest().body("Tenant not set");
    }
    return ResponseEntity.ok("pong " + tenantId);
  }
}

