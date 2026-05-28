package com.ar.crm2.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Converts a Keycloak-issued JWT into a collection of Spring {@link GrantedAuthority}
 * instances by extracting roles from the standard Keycloak token structure.
 *
 * <p>This converter is responsible for the TECHNICAL route guard only — it maps
 * Keycloak roles (realm + client/resource) to Spring {@code ROLE_*} authorities so that
 * Spring Security's {@code hasRole()} expressions work against Keycloak identity.
 *
 * <p>Two Keycloak role sources are mapped:
 * <ul>
 *   <li>{@code realm_access.roles} — realm-level roles assigned in Keycloak</li>
 *   <li>{@code resource_access.{client-id}.roles} — client-level roles assigned in Keycloak</li>
 * </ul>
 *
 * <p>Each role string {@code "FOO"} becomes {@code "ROLE_FOO"} (Spring convention).
 * Duplicate roles across sources are deduplicated.
 *
 * <p>CRM2 business authorization is handled separately via {@link ActorContext}
 * (application layer) — this converter only feeds Spring Security route guards.
 *
 * <p>NOTE: Keycloak roles are identity-level, not CRM2 business roles. They are
 * trusted here ONLY for technical routing decisions, not for business authorization.
 */
public class KeycloakJwtAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final Logger log = LoggerFactory.getLogger(KeycloakJwtAuthoritiesConverter.class);

    private static final String CLAIM_REALM_ACCESS = "realm_access";
    private static final String CLAIM_RESOURCE_ACCESS = "resource_access";
    private static final String CLAIM_ROLES = "roles";
    private static final String ROLE_PREFIX = "ROLE_";

    /**
     * Converts a Keycloak JWT into Spring GrantedAuthorities by extracting
     * roles from both realm_access and resource_access claims.
     *
     * @param jwt the validated Keycloak JWT (never null when called by Spring Security)
     * @return unmodifiable collection of SimpleGrantedAuthority("ROLE_{role}") instances
     */
    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Set<String> roles = new HashSet<>();

        // Extract realm-level roles
        extractRealmRoles(jwt, roles);

        // Extract client/resource-level roles
        extractResourceRoles(jwt, roles);

        List<GrantedAuthority> result = roles.stream()
                .map(role -> new SimpleGrantedAuthority(ROLE_PREFIX + role))
                .collect(Collectors.toUnmodifiableList());

        if (log.isDebugEnabled()) {
            log.debug("JWT granted authorities resolved: {}",
                result.stream().map(GrantedAuthority::getAuthority).toList());
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private void extractRealmRoles(Jwt jwt, Set<String> roles) {
        Map<String, Object> realmAccess = jwt.getClaim(CLAIM_REALM_ACCESS);
        if (realmAccess == null) {
            if (log.isDebugEnabled()) {
                log.debug("JWT realm_access claim absent — no realm roles");
            }
            return;
        }
        Object rolesObj = realmAccess.get(CLAIM_ROLES);
        if (rolesObj instanceof List<?> rawRoles) {
            List<String> extracted = new ArrayList<>();
            for (Object r : rawRoles) {
                if (r instanceof String role && !role.isBlank()) {
                    roles.add(role);
                    extracted.add(role);
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("JWT realm_access.roles extracted: {}", extracted);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("JWT realm_access present but roles key missing or non-list — realm_access keys: {}",
                    realmAccess.keySet());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void extractResourceRoles(Jwt jwt, Set<String> roles) {
        // The client-id for this resource server is configured in application.yml
        // spring.security.oauth2.resourceserver.jwt.audiences: crm2-api
        // We extract resource_access.crm2-api.roles if present (client role assignment)
        Map<String, Object> resourceAccess = jwt.getClaim(CLAIM_RESOURCE_ACCESS);
        if (resourceAccess == null) {
            if (log.isDebugEnabled()) {
                log.debug("JWT resource_access claim absent — no client roles");
            }
            return;
        }
        // Iterate all resource entries — collect roles from any known CRM2 client entry
        List<String> extractedClientRoles = new ArrayList<>();
        for (Map.Entry<String, Object> entry : resourceAccess.entrySet()) {
            if (entry.getValue() instanceof Map<?, ?> resource) {
                Object clientRoles = resource.get(CLAIM_ROLES);
                if (clientRoles instanceof List<?> rawRoles) {
                    for (Object r : rawRoles) {
                        if (r instanceof String role && !role.isBlank()) {
                            roles.add(role);
                            extractedClientRoles.add(role);
                        }
                    }
                }
            }
        }
        if (log.isDebugEnabled()) {
            if (extractedClientRoles.isEmpty()) {
                log.debug("JWT resource_access present but no client roles extracted — resource keys: {}",
                    resourceAccess.keySet());
            } else {
                log.debug("JWT resource_access client roles extracted: {}", extractedClientRoles);
            }
        }
    }
}