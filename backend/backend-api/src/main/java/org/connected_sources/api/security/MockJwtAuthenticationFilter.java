package org.connected_sources.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.connected_sources.shared.context.TenantContext;
import org.connected_sources.shared.context.TenantContextHolder;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


public class MockJwtAuthenticationFilter extends OncePerRequestFilter {

    private final MockJwtService mockJwtService;

    public MockJwtAuthenticationFilter(MockJwtService mockJwtService) {
        this.mockJwtService = mockJwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println("MockJwtAuthenticationFilter called for: " + request.getRequestURI());

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            System.out.println("Token received: " + token);

            if (mockJwtService.validateToken(token)) {
                System.out.println("Token is valid");
                try {
                    Map<String, Object> claims = mockJwtService.extractClaims(token);
                    List<String> roles = (List<String>) claims.get("roles");

                    String username = (String) claims.get("sub");
                    List<GrantedAuthority> authorities = roles.stream()
                            .map(role -> new SimpleGrantedAuthority(role))
                            .collect(Collectors.toList());

                    Authentication authentication =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    System.out.println("Authentication set for user: " + username);

                    setupTenantContextFromJwt(claims, request);
                } catch (Exception e) {
                    System.out.println("Error processing token: " + e.getMessage());
                    SecurityContextHolder.clearContext();
                }
            } else {
                System.out.println("Token is invalid");
            }
        } else {
            System.out.println("No Authorization header found");
        }

        filterChain.doFilter(request, response);
    }

    private void setupTenantContextFromJwt(Map<String, Object> claims, HttpServletRequest request) {
        String tenantId = (String) claims.getOrDefault("tenantId",
                Optional.ofNullable(request.getHeader("X-Tenant-Id")).orElse("default"));

//        Long userId = ((Number) claims.getOrDefault("userId",
//                Optional.ofNullable(request.getHeader("X-User-Id"))
//                        .map(Long::parseLong)
//                        .orElse(-1L))).longValue();
        Long userId = safeGetLong(claims, "userId",
                safeGetLongHeader(request, "X-User-Id", -1L));

        String correlationId = (String) claims.getOrDefault("correlationId",
                Optional.ofNullable(request.getHeader("X-Correlation-Id"))
                        .orElse(UUID.randomUUID().toString()));

        // Imposta il TenantContext
        TenantContext context = new TenantContext(tenantId, userId, correlationId);
        TenantContextHolder.set(context);

        System.out.println("TenantContext set: " + context); // all'antica
    }

    ///  per aggirare problemi di conversione...
    public static Long safeGetLong(Map<String, Object> claims, String key, Long defaultValue) {
        try {
            Object value = claims.get(key);
            if (value instanceof Number) return ((Number) value).longValue();
            if (value instanceof String) return Long.parseLong((String) value);
            return defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static Long safeGetLongHeader(HttpServletRequest request, String header, Long defaultValue) {
        try {
            String value = request.getHeader(header);
            return value != null ? Long.parseLong(value) : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static String safeGetString(Map<String, Object> claims, String key, String defaultValue) {
        Object value = claims.get(key);
        if (value instanceof String) return (String) value;
        if (value != null) return value.toString();
        return defaultValue;
    }
}