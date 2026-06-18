package com.ar.crm2.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Perfil 'noauth': deshabilita Keycloak y permite todas las peticiones.
 * Solo para desarrollo/pruebas locales sin servidor de auth.
 *
 * Uso: mvnw spring-boot:run -pl boot -Dspring-boot.run.profiles=noauth
 */
@Configuration
@EnableWebSecurity
@Profile("noauth")
public class SecurityNoAuthConfig {

    @Bean
    @Order(0)
    public SecurityFilterChain noAuthFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .csrf(csrf -> csrf.disable())
            .cors(cors -> {})
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        return http.build();
    }
}
