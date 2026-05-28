package com.ar.crm2.security;

import com.ar.crm2.application.security.ActorContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

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

        if (authentication != null && authentication.isAuthenticated()) {
            ActorContext actorContext = actorContextMapper.map(authentication);
            request.setAttribute(ACTOR_CONTEXT_ATTRIBUTE, actorContext);
        }

        filterChain.doFilter(request, response);
    }
}