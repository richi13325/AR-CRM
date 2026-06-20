package com.ar.crm2.adapter.in.rest;

import com.ar.crm2.adapter.in.rest.dto.request.AplicarLabelsWaRequest;
import com.ar.crm2.adapter.in.rest.dto.request.RenombrarConversacionRequest;
import com.ar.crm2.adapter.in.rest.dto.request.SendMensajeWaRequest;
import com.ar.crm2.adapter.in.rest.dto.response.ConversacionWaResponse;
import com.ar.crm2.adapter.in.rest.dto.response.MensajeWaResponse;
import com.ar.crm2.application.security.ActorContext;
import com.ar.crm2.security.ActorContextRequestAttributeFilter;
import com.ar.crm2.whatsapp.application.conversacion.command.AsignarAgenteCommand;
import com.ar.crm2.whatsapp.application.conversacion.port.in.AplicarLabelsUseCase;
import com.ar.crm2.whatsapp.application.conversacion.port.in.AsignarAgenteUseCase;
import com.ar.crm2.whatsapp.application.conversacion.port.in.CerrarConversacionUseCase;
import com.ar.crm2.whatsapp.application.conversacion.port.in.GetAllConversacionesUseCase;
import com.ar.crm2.whatsapp.application.conversacion.port.in.GetConversacionByIdUseCase;
import com.ar.crm2.whatsapp.application.conversacion.port.in.RenombrarConversacionUseCase;
import com.ar.crm2.whatsapp.application.conversacion.port.in.GetCsatResumenUseCase;
import com.ar.crm2.whatsapp.application.ia.port.in.SugerirRespuestaUseCase;
import com.ar.crm2.whatsapp.application.mensaje.command.SendMensajeCommand;
import com.ar.crm2.whatsapp.application.mensaje.port.in.GetMensajesByConversacionUseCase;
import com.ar.crm2.whatsapp.application.mensaje.port.in.SendMensajeUseCase;
import com.ar.crm2.whatsapp.domain.entity.Conversacion;
import com.ar.crm2.whatsapp.domain.entity.Mensaje;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class WhatsappConversacionController {

    private final GetAllConversacionesUseCase getAllUseCase;
    private final GetConversacionByIdUseCase getByIdUseCase;
    private final AsignarAgenteUseCase asignarUseCase;
    private final CerrarConversacionUseCase cerrarUseCase;
    private final GetMensajesByConversacionUseCase getMensajesUseCase;
    private final SendMensajeUseCase sendMensajeUseCase;
    private final com.ar.crm2.whatsapp.application.conversacion.port.in.MarcarConversacionLeidaUseCase marcarLeidaUseCase;
    private final com.ar.crm2.whatsapp.application.conversacion.port.in.ReabrirConversacionUseCase reabrirUseCase;
    private final AplicarLabelsUseCase aplicarLabelsUseCase;
    private final RenombrarConversacionUseCase renombrarUseCase;
    private final SugerirRespuestaUseCase sugerirUseCase;
    private final GetCsatResumenUseCase csatResumenUseCase;

    /** Resumen global de CSAT (promedio y total de calificaciones) para el dashboard. */
    @GetMapping("/api/wa/conversaciones/csat-resumen")
    public ResponseEntity<com.ar.crm2.whatsapp.application.conversacion.port.out.CsatResumenPort.CsatResumen> csatResumen() {
        return ResponseEntity.ok(csatResumenUseCase.get());
    }

    @GetMapping("/api/wa/conversaciones/get-all")
    public ResponseEntity<List<ConversacionWaResponse>> getAll(@RequestParam UUID empresaId) {
        List<ConversacionWaResponse> result = getAllUseCase.getAll(empresaId).stream()
                .map(ConversacionWaResponse::fromDomain)
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/wa/conversaciones/get-by-id")
    public ResponseEntity<ConversacionWaResponse> getById(@RequestParam UUID id) {
        Conversacion c = getByIdUseCase.getById(id);
        return ResponseEntity.ok(ConversacionWaResponse.fromDomain(c));
    }

    @GetMapping("/api/wa/conversaciones/{id}/mensajes")
    public ResponseEntity<List<MensajeWaResponse>> getMensajes(@PathVariable UUID id) {
        List<MensajeWaResponse> mensajes = getMensajesUseCase.getByConversacion(id).stream()
                .map(MensajeWaResponse::fromDomain)
                .toList();
        return ResponseEntity.ok(mensajes);
    }

    @PostMapping("/api/wa/mensajes/send")
    public ResponseEntity<MensajeWaResponse> send(
            HttpServletRequest httpRequest,
            @RequestParam UUID conversacionId,
            @Valid @RequestBody SendMensajeWaRequest request
    ) {
        ActorContext actor = (ActorContext) httpRequest.getAttribute(
                ActorContextRequestAttributeFilter.ACTOR_CONTEXT_ATTRIBUTE);
        SendMensajeCommand command = new SendMensajeCommand(
                conversacionId,
                actor.usuarioId().orElseThrow(),
                request.tipo(),
                request.contenido(),
                request.mediaUrl(),
                request.interna()
        );
        Mensaje mensaje = sendMensajeUseCase.send(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(MensajeWaResponse.fromDomain(mensaje));
    }

    @PutMapping("/api/wa/conversaciones/asignar")
    public ResponseEntity<ConversacionWaResponse> asignar(
            @RequestParam UUID id,
            @RequestParam UUID agenteId
    ) {
        AsignarAgenteCommand command = new AsignarAgenteCommand(id, agenteId);
        Conversacion c = asignarUseCase.asignar(command);
        return ResponseEntity.ok(ConversacionWaResponse.fromDomain(c));
    }

    @PutMapping("/api/wa/conversaciones/cerrar")
    public ResponseEntity<ConversacionWaResponse> cerrar(@RequestParam UUID id) {
        Conversacion c = cerrarUseCase.cerrar(id);
        return ResponseEntity.ok(ConversacionWaResponse.fromDomain(c));
    }

    @PutMapping("/api/wa/conversaciones/marcar-leido")
    public ResponseEntity<ConversacionWaResponse> marcarLeido(@RequestParam UUID id) {
        Conversacion c = marcarLeidaUseCase.marcarLeida(id);
        return ResponseEntity.ok(ConversacionWaResponse.fromDomain(c));
    }

    // Renombra manualmente al contacto de la conversación (y el Contacto del CRM vinculado).
    /** Sugiere una respuesta con IA a partir del historial reciente de la conversación. */
    @PostMapping("/api/wa/conversaciones/{id}/sugerir")
    public ResponseEntity<java.util.Map<String, String>> sugerir(@PathVariable UUID id) {
        return ResponseEntity.ok(java.util.Map.of("sugerencia", sugerirUseCase.sugerir(id)));
    }

    @PutMapping("/api/wa/conversaciones/nombre")
    public ResponseEntity<ConversacionWaResponse> renombrar(
            @RequestParam UUID id, @Valid @RequestBody RenombrarConversacionRequest request) {
        Conversacion c = renombrarUseCase.renombrar(id, request.nombre());
        return ResponseEntity.ok(ConversacionWaResponse.fromDomain(c));
    }

    @PutMapping("/api/wa/conversaciones/reabrir")
    public ResponseEntity<ConversacionWaResponse> reabrir(@RequestParam UUID id) {
        Conversacion c = reabrirUseCase.reabrir(id);
        return ResponseEntity.ok(ConversacionWaResponse.fromDomain(c));
    }

    /**
     * Toggle de bot/handoff desde el panel humano (JWT). Mismo contrato de labels que
     * usa el bot de n8n (POST .../labels con api_access_token) — mandar ["escalado_humano"]
     * apaga el bot; mandar [] lo reactiva. Ver BotConversationController.
     */
    @PutMapping("/api/wa/conversaciones/labels")
    public ResponseEntity<ConversacionWaResponse> aplicarLabels(
            @RequestParam UUID id, @RequestBody AplicarLabelsWaRequest request) {
        Set<String> labels = request.labels() != null ? Set.copyOf(request.labels()) : Set.of();
        Conversacion c = aplicarLabelsUseCase.aplicar(id, labels);
        return ResponseEntity.ok(ConversacionWaResponse.fromDomain(c));
    }
}
