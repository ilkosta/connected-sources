package org.connected_sources.api.security;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Map;

public interface JwtService {
    String generateToken(UserDetails userDetails);

    boolean validateToken(String token);

    Map<String, Object> extractClaims(String token);

//    @SuppressWarnings("unchecked")
    List<String> extractRoles(String token);
}
