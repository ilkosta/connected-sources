package org.connected_sources.api.security;

import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Profile("dev | test") // Attivo solo in dev/test
public class MockJwtService implements JwtService {

//    private static final String MOCK_SECRET = "dev-mock-secret :)";
    private static final long EXPIRATION_TIME = 86400000; // 24 ore

    @Override
    public String generateToken(UserDetails userDetails) {
        return "it will be what it will be...";
    }

    @Override
    public boolean validateToken(String token) {
        //WARN: mocked validation
//        return token != null && token.startsWith("mock_");
        return true;
    }

    @Override
    public Map<String, Object> extractClaims(String token) {
        if (!validateToken(token)) {
            throw new RuntimeException("Invalid mock token");
        }

        try {
            String decoded = new String(Base64.getDecoder().decode(token));
            String[] parts = decoded.split("\\|");

            // Parsing semplice della mappa (per demo)
            Map<String, Object> claims = new HashMap<>();
            claims.put("sub", parts[0]);
            claims.put("userId", parts[1]);
            claims.put("roles", Arrays.asList(parts[2].split(",")));
            claims.put("exp", System.currentTimeMillis() + EXPIRATION_TIME); // forzato per test

            return claims;

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse mock token", e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> extractRoles(String token) {
        Map<String, Object> claims = extractClaims(token);
        return (List<String>) claims.get("roles");
    }
}