package org.connected_sources.api.security;

import org.springframework.context.annotation.Profile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("dev | test")
public class DebugController {

    @GetMapping("/debug/auth")
    public String debugAuth(@AuthenticationPrincipal String username) {
        return "Authenticated as: " + username;
    }
}