package com.ar.crm2.security;

import com.ar.crm2.whatsapp.application.bot.port.in.FindBotByTokenUseCase;
import com.ar.crm2.whatsapp.domain.entity.Bot;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Autentica al bot de n8n en las rutas estilo Chatwoot ({@code /api/v1/accounts/**})
 * vía el header {@code api_access_token}, igual al contrato de AmbarCRM/INTEGRACION-BOTS.md.
 * No usa Keycloak: estas rutas las llama un workflow de n8n, no un usuario humano.
 */
@Component
@RequiredArgsConstructor
public class BotApiTokenFilter extends OncePerRequestFilter {

    public static final String BOT_REQUEST_ATTRIBUTE = "botId";
    private static final String GUARDED_PREFIX = "/api/v1/accounts/";
    private static final String TOKEN_HEADER = "api_access_token";

    private final FindBotByTokenUseCase findBotByTokenUseCase;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (request.getRequestURI().startsWith(GUARDED_PREFIX)) {
            String token = request.getHeader(TOKEN_HEADER);
            Bot bot = token != null ? findBotByTokenUseCase.findByToken(token).orElse(null) : null;
            if (bot == null) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.getWriter().write("{\"error\":\"api_access_token invalido\"}");
                return;
            }
            request.setAttribute(BOT_REQUEST_ATTRIBUTE, bot.getId().value());
        }
        chain.doFilter(request, response);
    }
}
