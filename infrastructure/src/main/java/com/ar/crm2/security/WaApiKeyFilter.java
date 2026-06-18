package com.ar.crm2.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class WaApiKeyFilter extends OncePerRequestFilter {

    private static final String WEBHOOK_PATH = "/api/wa/webhook";
    private static final String CRON_AUTO_RESOLVER_PATH = "/api/cron/auto-resolver";
    private static final String API_KEY_HEADER = "x-api-key";

    private final WaProperties waProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (uri.startsWith(WEBHOOK_PATH) || uri.equals(CRON_AUTO_RESOLVER_PATH)) {
            String apiKey = request.getHeader(API_KEY_HEADER);
            if (apiKey == null || !apiKey.equals(waProperties.webhookApiKey())) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.getWriter().write("{\"error\":\"API key invalida\"}");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
