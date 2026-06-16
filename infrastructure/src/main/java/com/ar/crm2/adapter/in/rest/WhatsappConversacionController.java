package com.ar.crm2.adapter.in.rest;

import com.ar.crm2.adapter.in.rest.dto.request.SendMensajeWaRequest;
import com.ar.crm2.adapter.in.rest.dto.response.ConversacionWaResponse;
import com.ar.crm2.adapter.in.rest.dto.response.MensajeWaResponse;
import com.ar.crm2.application.security.ActorContext;
import com.ar.crm2.security.ActorContextRequestAttributeFilter;
import com.ar.crm2.whatsapp.application.conversacion.command.AsignarAgenteCommand;
import com.ar.crm2.whatsapp.application.conversacion.port.in.AsignarAgenteUseCase;
import com.ar.crm2.whatsapp.application.conversacion.port.in.CerrarConversacionUseCase;
import com.ar.crm2.whatsapp.application.conversacion.port.in.GetAllConversacionesUseCase;
import com.ar.crm2.whatsapp.application.conversacion.port.in.GetConversacionByIdUseCase;
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
                actor.usuarioId().value(),
                request.tipo(),
                request.contenido(),
                request.mediaUrl()
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
}
