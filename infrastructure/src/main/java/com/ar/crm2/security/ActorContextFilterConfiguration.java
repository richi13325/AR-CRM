package com.ar.crm2.security;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.util.ReflectionUtils;

/**
 * Registers the {@link ActorContextRequestAttributeFilter} into the servlet filter chain
 * with a fixed order so it runs AFTER Spring Security's JWT authentication filter.
 *
 * <p>Spring Security's OAuth2 Resource Server uses {@link BearerTokenAuthenticationFilter}
 * as the JWT validation entry point. Our filter must run after that to have the
 * {@link org.springframework.security.core.context.SecurityContextHolder} populated.
 *
 * <p>Uses {@link FilterRegistrationBean} with an explicit order rather than
 * {@link jakarta.servlet.Filter#init(jakarta.servlet.ServletConfig)} to avoid
 * coupling infrastructure components to the Servlet API at startup.
 */
@Configuration
public class ActorContextFilterConfiguration {

    @Bean
    public FilterRegistrationBean<ActorContextRequestAttributeFilter> actorContextFilterRegistration(
            ActorContextRequestAttributeFilter filter
    ) {
        FilterRegistrationBean<ActorContextRequestAttributeFilter> registration =
                new FilterRegistrationBean<>(filter);
        // Run immediately after BearerTokenAuthenticationFilter (order ~110)
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 110);
        return registration;
    }
}