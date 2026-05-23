package com.ar.crm2.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration for CRM2 REST API.
 *
 * Security boundary in infrastructure layer — keeps Spring Security types
 * out of application/domain layers per Clean/Hexagonal architecture.
 *
 * Route policy:
 * - Public: /swagger-ui/**, /v3/api-docs/**, /actuator/health, OPTIONS (CORS preflight)
 * - Authenticated: /api/** (JWT Bearer token required)
 * - Deny all else
 *
 * Session: stateless (no server-side sessions; JWT carries identity).
 * CSRF: disabled for REST API (stateless + Bearer token authentication).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Stateless session — no HttpSession, no server-side session store
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // Disable CSRF — not needed for stateless Bearer-token APIs
            .csrf(csrf -> csrf.disable())
            // CORS is handled by CorsConfig WebMvcConfigurer; permit all OPTIONS for preflight
            .cors(cors -> {})
            // Route authorization
            .authorizeHttpRequests(authorize -> authorize
                // Public OpenAPI / actuator health
                .requestMatchers(HttpMethod.GET, "/swagger-ui/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/v3/api-docs/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                // Preflight CORS
                .requestMatchers(HttpMethod.OPTIONS).permitAll()
                // All API endpoints require authentication
                .requestMatchers("/api/**").authenticated()
                // Deny everything else
                .anyRequest().denyAll()
            )
            // JWT Resource Server — validates Bearer tokens via configured issuer-uri
            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt -> {})
            );

        return http.build();
    }
}