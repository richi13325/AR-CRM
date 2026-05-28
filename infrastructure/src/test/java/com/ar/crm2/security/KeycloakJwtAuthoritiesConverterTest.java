package com.ar.crm2.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link KeycloakJwtAuthoritiesConverter}.
 *
 * Verifies that the converter correctly extracts roles from:
 * - {@code realm_access.roles} → ROLE_{role}
 * - {@code resource_access.{client}.roles} → ROLE_{role}
 * - Both sources combined, deduplicated
 *
 * <p>This converter is the bridge between Keycloak's role model and Spring Security's
 * {@code hasRole()} expressions. It is the fix for the 403 on /api/superusuarios/create
 * when an admin token contains SUPER_USUARIO.
 */
class KeycloakJwtAuthoritiesConverterTest {

    private KeycloakJwtAuthoritiesConverter converter;

    @BeforeEach
    void setUp() {
        converter = new KeycloakJwtAuthoritiesConverter();
    }

    // ------------------------------------------------------------------
    // Realm roles only
    // ------------------------------------------------------------------

    @Test
    @DisplayName("convert — realm_access.roles contains SUPER_USUARIO → ROLE_SUPER_USUARIO")
    void convert_realmAccessHasSuperUsuario_returnsRoleSuperUsuario() {
        Jwt jwt = mockJwt(
            Map.of(
                "sub", "admin-subject",
                "realm_access", Map.of("roles", List.of("SUPER_USUARIO", "USER")),
                "aud", List.of("crm2-api")
            )
        );

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertTrue(authorities.contains(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_SUPER_USUARIO")),
            "Authorities must contain ROLE_SUPER_USUARIO");
        assertTrue(authorities.contains(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")),
            "Authorities must contain ROLE_USER");
    }

    @Test
    @DisplayName("convert — realm_access.roles is empty → no authorities")
    void convert_realmAccessEmpty_noAuthorities() {
        Jwt jwt = mockJwt(
            Map.of(
                "sub", "subject",
                "realm_access", Map.of("roles", List.of()),
                "aud", List.of("crm2-api")
            )
        );

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertTrue(authorities.isEmpty());
    }

    @Test
    @DisplayName("convert — realm_access absent → no authorities")
    void convert_realmAccessAbsent_noAuthorities() {
        Jwt jwt = mockJwt(
            Map.of(
                "sub", "subject",
                "aud", List.of("crm2-api")
            )
        );

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertTrue(authorities.isEmpty());
    }

    // ------------------------------------------------------------------
    // Client/resource roles only
    // ------------------------------------------------------------------

    @Test
    @DisplayName("convert — resource_access.crm2-api.roles contains SUPER_USUARIO → ROLE_SUPER_USUARIO")
    void convert_resourceAccessHasSuperUsuario_returnsRoleSuperUsuario() {
        Jwt jwt = mockJwt(
            Map.of(
                "sub", "admin-subject",
                "realm_access", Map.of("roles", List.of()),
                "resource_access", Map.of(
                    "crm2-api", Map.of("roles", List.of("SUPER_USUARIO"))
                ),
                "aud", List.of("crm2-api")
            )
        );

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertTrue(authorities.contains(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_SUPER_USUARIO")),
            "Authorities must contain ROLE_SUPER_USUARIO from resource_access");
    }

    @Test
    @DisplayName("convert — roles from multiple resources → deduplicated")
    void convert_multipleResourceRoles_deduplicated() {
        Jwt jwt = mockJwt(
            Map.of(
                "sub", "subject",
                "realm_access", Map.of("roles", List.of("USER")),
                "resource_access", Map.of(
                    "crm2-api", Map.of("roles", List.of("SUPER_USUARIO")),
                    "other-client", Map.of("roles", List.of("SUPPORT"))
                ),
                "aud", List.of("crm2-api")
            )
        );

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertEquals(3, authorities.size());
        assertTrue(authorities.contains(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")));
        assertTrue(authorities.contains(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_SUPER_USUARIO")));
        assertTrue(authorities.contains(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_SUPPORT")));
    }

    // ------------------------------------------------------------------
    // Both sources combined
    // ------------------------------------------------------------------

    @Test
    @DisplayName("convert — realm + resource roles combined → all extracted")
    void convert_realmAndResourceRolesCombined_bothExtracted() {
        Jwt jwt = mockJwt(
            Map.of(
                "sub", "admin-subject",
                "realm_access", Map.of("roles", List.of("ADMIN", "USER")),
                "resource_access", Map.of(
                    "crm2-api", Map.of("roles", List.of("SUPER_USUARIO"))
                ),
                "aud", List.of("crm2-api")
            )
        );

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertTrue(authorities.contains(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN")));
        assertTrue(authorities.contains(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")));
        assertTrue(authorities.contains(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_SUPER_USUARIO")));
    }

    @Test
    @DisplayName("convert — same role in realm and resource → deduplicated (single ROLE_*)")
    void convert_duplicateRoleAcrossSources_deduplicated() {
        Jwt jwt = mockJwt(
            Map.of(
                "sub", "subject",
                "realm_access", Map.of("roles", List.of("USER")),
                "resource_access", Map.of(
                    "crm2-api", Map.of("roles", List.of("USER"))
                ),
                "aud", List.of("crm2-api")
            )
        );

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        long userAuthorities = authorities.stream()
            .filter(a -> a.getAuthority().equals("ROLE_USER"))
            .count();
        assertEquals(1, userAuthorities, "ROLE_USER must appear only once");
    }

    // ------------------------------------------------------------------
    // Edge cases: non-String roles, malformed data
    // ------------------------------------------------------------------

    @Test
    @DisplayName("convert — roles contain non-String → silently skipped")
    void convert_nonStringRoles_skipped() {
        Jwt jwt = mockJwt(
            Map.of(
                "sub", "subject",
                "realm_access", Map.of("roles", List.of("ADMIN", 123, true)),
                "aud", List.of("crm2-api")
            )
        );

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("convert — blank role string → silently skipped")
    void convert_blankRoleString_skipped() {
        Jwt jwt = mockJwt(
            Map.of(
                "sub", "subject",
                "realm_access", Map.of("roles", List.of("ADMIN", "", "   ")),
                "aud", List.of("crm2-api")
            )
        );

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("convert — resource_access is null → no crash, realm roles still extracted")
    void convert_resourceAccessNull_noCrash() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "subject");
        claims.put("realm_access", Map.of("roles", List.of("USER")));
        claims.put("resource_access", null);
        claims.put("aud", List.of("crm2-api"));
        Jwt jwt = mockJwt(claims);

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    @DisplayName("convert — returned collection is unmodifiable")
    void convert_returnsUnmodifiableCollection() {
        Jwt jwt = mockJwt(
            Map.of(
                "sub", "subject",
                "realm_access", Map.of("roles", List.of("USER")),
                "aud", List.of("crm2-api")
            )
        );

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertThrows(UnsupportedOperationException.class,
            () -> authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_HACKER")));
    }

    // ------------------------------------------------------------------
    // Helper
    // ------------------------------------------------------------------

    private Jwt mockJwt(Map<String, Object> claims) {
        return new org.springframework.security.oauth2.jwt.Jwt(
            "mock-token-value",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            Map.of("alg", "RS256"),
            claims
        ) {};
    }
}