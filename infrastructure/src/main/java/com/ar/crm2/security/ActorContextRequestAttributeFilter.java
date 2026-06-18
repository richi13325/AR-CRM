package com.ar.crm2.security;

import com.ar.crm2.application.security.ActorContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Populates the authenticated {@link ActorContext} into the current request as a
 * request attribute ({@code actorContext}), making it available to downstream
 * controllers without leaking Spring Security types into the application layer.
 *
 * <p>This filter runs after the JWT validation filter in the Spring Security
 * filter chain, so {@link SecurityContextHolder} already contains the validated
 * authentication when this filter executes.
 *
 * <p>Pattern: SecurityContextHolder → ActorContext → HttpServletRequest attribute
 *
 * <p>Request attribute name: {@code actorContext}
 */
@Component
public class ActorContextRequestAttributeFilter extends OncePerRequestFilter {

    public static final String ACTOR_CONTEXT_ATTRIBUTE = "actorContext";

    private final KeycloakJwtActorContextMapper actorContextMapper;

    public ActorContextRequestAttributeFilter(KeycloakJwtActorContextMapper actorContextMapper) {
        this.actorContextMapper = actorContextMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof Jwt) {
            ActorContext actorContext = actorContextMapper.map(authentication);
            request.setAttribute(ACTOR_CONTEXT_ATTRIBUTE, actorContext);
        } else {
            // noauth profile: inyectar ActorContext de desarrollo para que los
            // controllers no reciban null (solo aplica cuando no hay JWT válido)
            ActorContext devContext = new ActorContext(
                "dev-user-id",
                "dev",
                "dev@local.test",
                Optional.of(UUID.fromString("00000000-0000-0000-0000-000000000001")),
                Optional.empty(),
                Set.of("admin")
            );
            request.setAttribute(ACTOR_CONTEXT_ATTRIBUTE, devContext);
        }

        filterChain.doFilter(request, response);
    }
}