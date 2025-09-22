package org.connected_sources.api;

import org.connected_sources.shared.context.TenantContextHolder;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
@Profile("test") // only exposed in test environment
public class TenantDebugController {

    private final TenantContextHolder tenantContextHolder;

    public TenantDebugController(TenantContextHolder tch) {
        this.tenantContextHolder = tch;
    }

    @GetMapping("/tenant")
    public ResponseEntity<Map<String, String>> getTenantContext() {
        Map<String, String> response = HashMap.newHashMap(1);
        final String tenantId = tenantContextHolder.get().tenantId();
        if(!tenantId.isBlank()) {
            response.put("tenantId", tenantId);
            return ResponseEntity.ok(response);
        }
        else {
            response.put("Bad Request", "Tenant not set");
            return ResponseEntity.status(400).body(response);
        }
    }
}
