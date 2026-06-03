package com.ar.crm2.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
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
    @Order(0)
    public SecurityFilterChain apiSecurityFilterChain(
            HttpSecurity http,
            ActorContextRequestAttributeFilter actorContextRequestAttributeFilter
    ) throws Exception {
        http
            // Route authorization
            .authorizeHttpRequests(authorize -> authorize
                // Public OpenAPI / actuator health
                .requestMatchers(HttpMethod.GET, "/swagger-ui/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/v3/api-docs/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                // Forgot password - public endpoint, no email enumeration
                .requestMatchers(HttpMethod.POST, "/api/usuarios/forgot-password").permitAll()
                // Preflight CORS
                .requestMatchers(HttpMethod.OPTIONS).permitAll()
                // SuperUsuario bootstrap: requires authenticated + SUPER_USUARIO technical role.
                // This is a technical guard only — CRM2 business authorization is separate.
                .requestMatchers(HttpMethod.POST, "/api/superusuarios/create")
                    .hasRole("SUPER_USUARIO")
                // All other API endpoints require authentication (no role restriction here)
                .requestMatchers("/api/**").authenticated()
                // Deny everything else
                .anyRequest().denyAll()
            )
            // Stateless session — no HttpSession, no server-side session store
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // Disable CSRF — not needed for stateless Bearer-token APIs
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")
                .disable()
            )
// CORS is handled by CorsConfig WebMvcConfigurer
            .cors(cors -> {})
            // JWT Resource Server — validates Bearer tokens via configured issuer-uri
            // KeycloakJwtAuthoritiesConverter maps Keycloak roles (realm + resource_access)
            // to Spring ROLE_* authorities so hasRole() works against Keycloak identity.
            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt ->
                    jwt.jwtAuthenticationConverter(keycloakJwtAuthenticationConverter())
                )
            )
            .addFilterAfter(actorContextRequestAttributeFilter, BearerTokenAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    JwtAuthenticationConverter keycloakJwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakJwtAuthoritiesConverter());
        return converter;
    }
}
