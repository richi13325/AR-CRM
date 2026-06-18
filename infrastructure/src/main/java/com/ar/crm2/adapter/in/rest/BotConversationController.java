package com.ar.crm2.adapter.in.rest;

import com.ar.crm2.adapter.in.rest.dto.request.BotFunnelRequest;
import com.ar.crm2.adapter.in.rest.dto.request.BotLabelsRequest;
import com.ar.crm2.adapter.in.rest.dto.request.BotMessageRequest;
import com.ar.crm2.adapter.in.rest.dto.response.ConversationStatusResponse;
import com.ar.crm2.adapter.in.rest.dto.response.MensajeWaResponse;
import com.ar.crm2.whatsapp.application.conversacion.port.in.AplicarLabelsUseCase;
import com.ar.crm2.whatsapp.application.conversacion.port.in.GetConversacionByIdUseCase;
import com.ar.crm2.whatsapp.application.funnel.port.in.MoverFunnelUseCase;
import com.ar.crm2.whatsapp.application.mensaje.command.ResponderBotCommand;
import com.ar.crm2.whatsapp.application.mensaje.port.in.ResponderBotUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * API estilo Chatwoot que llama el bot de n8n para responder, mover el funnel y hacer
 * handoff a humano. Autenticada por {@link com.ar.crm2.security.BotApiTokenFilter}
 * (header api_access_token), no por JWT — contrato igual al de AmbarCRM/INTEGRACION-BOTS.md.
 */
@RestController
@RequestMapping("/api/v1/accounts/{accountId}/conversations/{conversationId}")
@RequiredArgsConstructor
public class BotConversationController {

    private final GetConversacionByIdUseCase getByIdUseCase;
    private final ResponderBotUseCase responderUseCase;
    private final AplicarLabelsUseCase aplicarLabelsUseCase;
    private final MoverFunnelUseCase moverFunnelUseCase;

    @GetMapping
    public ResponseEntity<ConversationStatusResponse> getStatus(
            @PathVariable String accountId, @PathVariable UUID conversationId) {
        var conversacion = getByIdUseCase.getById(conversationId);
        return ResponseEntity.ok(ConversationStatusResponse.fromDomain(conversacion));
    }

    @PostMapping("/messages")
    public ResponseEntity<MensajeWaResponse> postMessage(
            @PathVariable String accountId, @PathVariable UUID conversationId,
            @Valid @RequestBody BotMessageRequest request) {
        var mensaje = responderUseCase.responder(
                new ResponderBotCommand(conversationId, request.content(), request.privado()));
        return ResponseEntity.status(HttpStatus.CREATED).body(MensajeWaResponse.fromDomain(mensaje));
    }

    @PostMapping("/labels")
    public ResponseEntity<ConversationStatusResponse> postLabels(
            @PathVariable String accountId, @PathVariable UUID conversationId,
            @RequestBody BotLabelsRequest request) {
        Set<String> labels = request.labels() != null ? Set.copyOf(request.labels()) : Set.of();
        var conversacion = aplicarLabelsUseCase.aplicar(conversationId, labels);
        return ResponseEntity.ok(ConversationStatusResponse.fromDomain(conversacion));
    }

    @PostMapping("/funnel")
    public ResponseEntity<Map<String, Boolean>> postFunnel(
            @PathVariable String accountId, @PathVariable UUID conversationId,
            @Valid @RequestBody BotFunnelRequest request) {
        boolean movido = moverFunnelUseCase.mover(conversationId, request.etapa());
        return ResponseEntity.ok(Map.of("moved", movido));
    }
}
