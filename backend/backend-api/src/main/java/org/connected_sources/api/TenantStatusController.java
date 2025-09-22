package org.connected_sources.api;

import org.connected_sources.shared.context.TenantContextHolder;
import org.connected_sources.tenant.spi.TenantDatasourceRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/tenant")
@Profile("test")
public class TenantStatusController {

    private final TenantContextHolder tenantContextHolder;
    private final TenantDatasourceRegistry datasourceRegistry;

    public TenantStatusController(
            TenantContextHolder tenantContextHolder,
            @Qualifier("fsTenantDatasourceRegistry") TenantDatasourceRegistry datasourceRegistry
    ) {
        this.tenantContextHolder = tenantContextHolder;
        this.datasourceRegistry = datasourceRegistry;
    }

    @GetMapping("/status")
    public ResponseEntity<?> getTenantStatus() {

        if (tenantContextHolder.get().tenantId().isEmpty()) {
            return ResponseEntity.status(400).body("Tenant not set");
        }
        else {
            String tenantId = tenantContextHolder.get().tenantId();
            DataSource ds = datasourceRegistry.getDataSource(tenantId);
            if (ds == null) {
                return ResponseEntity.status(404).body("No datasource found for tenant: " + tenantId);
            }

            try (Connection conn = ds.getConnection()) {
                DatabaseMetaData meta = conn.getMetaData();
                Map<String, Object> info = new HashMap<>();
                info.put("tenantId", tenantId);
                info.put("dbProduct", meta.getDatabaseProductName());
                info.put("url", meta.getURL());
                info.put("user", meta.getUserName());
                return ResponseEntity.ok(info);
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body("Error retrieving DB metadata: " + e.getMessage());
            }
        }
    }

    @PostMapping("/ping")
    public ResponseEntity<String> ping() {
      try{
        final String tenantId = tenantContextHolder.get().tenantId();
        if(tenantId.isEmpty()) {
          return ResponseEntity.status(404).body("Tenant empty");
        }
        else {
          return ResponseEntity.status(200).body("pong " + tenantId);
        }
      }
      catch(Exception e) {
        return ResponseEntity.status(404).body("Tenant not set");
      }
    }

    @GetMapping("/current")
    public ResponseEntity<String> currentTenant() {
      try{
        final String tenantId = tenantContextHolder.get().tenantId();
        if(tenantId.isEmpty()) {
          return ResponseEntity.status(404).body("Tenant empty");
        }
        else {
          return ResponseEntity.status(200).body(tenantId);
        }
      }
      catch(Exception e) {
        return ResponseEntity.status(404).body("Tenant not set");
      }
    }
}
