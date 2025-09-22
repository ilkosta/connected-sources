package org.connected_sources.api.diag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
public class DiagnosticsController {
  @GetMapping("/ping") public String ping() { return "pong"; }
  @GetMapping("/status") public ResponseEntity<?> status() { return ResponseEntity.ok().build(); }
}