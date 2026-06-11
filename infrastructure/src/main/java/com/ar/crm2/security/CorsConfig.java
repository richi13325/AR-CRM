package com.ar.crm2.security;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS configuration for CRM2 REST API.
 * Allowed origins are env-driven (CRM2_CORS_ALLOWED_ORIGINS), defaulting to the
 * local Vite dev server. Applies to /api/** so Swagger UI remains unaffected.
 *
 * <p>Exposes both {@link WebMvcConfigurer} (for Spring MVC CORS) and
 * {@link CorsConfigurationSource} (for Spring Security CORS integration).
 * Spring Security needs a {@code CorsConfigurationSource} bean to process
 * CORS at the filter level; the {@code WebMvcConfigurer} alone is insufficient
 * in Boot 4 when used with {@code .cors(cors -> {})} in {@link SecurityConfig}.
 */
@Configuration
public class CorsConfig {

    /**
     * Allowed CORS origins for the frontend. Comma-separated, env-driven via
     * {@code CRM2_CORS_ALLOWED_ORIGINS}; defaults to the local Vite dev server.
     */
    private final String[] allowedOrigins;

    public CorsConfig(@Value("${crm2.cors.allowed-origins:http://localhost:5173}") String[] allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(allowedOrigins)
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(false);
            }
        };
    }

    /**
     * {@code CorsConfigurationSource} bean for Spring Security CORS filter.
     * Mirrors the WebMvcConfigurer registry so Spring Security can evaluate
     * CORS preflight requests at the security filter level.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(allowedOrigins));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
