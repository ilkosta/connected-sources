package org.connected_sources.api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@Profile("dev | test")
public class DevSecurityConfig {

    // for autowiring
    @Bean
    public MockJwtAuthenticationFilter mockJwtAuthenticationFilter(MockJwtService mockJwtService) {
        return new MockJwtAuthenticationFilter(mockJwtService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, MockJwtAuthenticationFilter mockJwtAuthenticationFilter) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/ping").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        // in prod
                        //.requestMatchers("/actuator/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(mockJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}