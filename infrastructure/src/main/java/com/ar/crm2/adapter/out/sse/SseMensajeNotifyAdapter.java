package com.ar.crm2.adapter.out.sse;

import com.ar.crm2.whatsapp.application.conversacion.port.out.NotifyEscribiendoPort;
import com.ar.crm2.whatsapp.application.grupo.port.out.NotifyMensajeGrupoPort;
import com.ar.crm2.whatsapp.application.mensaje.port.out.NotifyEstadoMensajePort;
import com.ar.crm2.whatsapp.application.mensaje.port.out.NotifyNewMensajePort;
import com.ar.crm2.whatsapp.domain.entity.Mensaje;
import com.ar.crm2.whatsapp.domain.entity.MensajeGrupo;
import com.ar.crm2.whatsapp.domain.vo.ConversacionId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class SseMensajeNotifyAdapter implements NotifyNewMensajePort, NotifyMensajeGrupoPort, NotifyEstadoMensajePort, NotifyEscribiendoPort {

    private final SseEmitterRegistry registry;

    @Override
    public void notificarEscribiendo(ConversacionId conversacionId) {
        broadcast(SseEmitter.event()
                .name("escribiendo")
                .data(Map.of("conversacionId", conversacionId.value().toString())));
    }

    @Override
    public void notifyEstado(Mensaje mensaje) {
        broadcast(SseEmitter.event()
                .name("estado_mensaje")
                .data(Map.of(
                        "mensajeId", mensaje.getId().value().toString(),
                        "conversacionId", mensaje.getConversacionId().value().toString(),
                        "status", mensaje.getStatus().name())));
    }

    private void broadcast(SseEmitter.SseEventBuilder event) {
        Map<String, SseEmitter> emitters = registry.getAll();
        emitters.forEach((userId, emitter) -> {
            try {
                emitter.send(event);
            } catch (Exception e) {
                log.debug("SSE emitter {} ya no disponible: {}", userId, e.getMessage());
            }
        });
    }

    @Override
    public void notify(Mensaje mensaje) {
        Map<String, SseEmitter> emitters = registry.getAll();
        if (emitters.isEmpty()) return;

        SseEmitter.SseEventBuilder event = SseEmitter.event()
                .name("nuevo_mensaje")
                .data(Map.of(
                        "mensajeId", mensaje.getId().value().toString(),
                        "conversacionId", mensaje.getConversacionId().value().toString(),
                        "tipo", mensaje.getTipo().name(),
                        "direccion", mensaje.getDireccion().name(),
                        "contenido", mensaje.getContenido() != null ? mensaje.getContenido() : "",
                        "creadoEn", mensaje.getCreadoEn().toString()
                ));

        emitters.forEach((userId, emitter) -> {
            try {
                emitter.send(event);
            } catch (Exception e) {
                log.debug("SSE emitter {} ya no disponible: {}", userId, e.getMessage());
            }
        });
    }

    @Override
    public void notifyGrupo(MensajeGrupo mensaje) {
        Map<String, SseEmitter> emitters = registry.getAll();
        if (emitters.isEmpty()) return;

        SseEmitter.SseEventBuilder event = SseEmitter.event()
                .name("nuevo_mensaje_grupo")
                .data(Map.of(
                        "mensajeId", mensaje.getId().value().toString(),
                        "grupoId", mensaje.getGrupoId().value().toString(),
                        "tipo", mensaje.getTipo().name(),
                        "direccion", mensaje.getDireccion().name(),
                        "contenido", mensaje.getContenido() != null ? mensaje.getContenido() : "",
                        "remitente", mensaje.getRemitente() != null ? mensaje.getRemitente() : "",
                        "creadoEn", mensaje.getCreadoEn().toString()
                ));

        emitters.forEach((userId, emitter) -> {
            try {
                emitter.send(event);
            } catch (Exception e) {
                log.debug("SSE emitter {} ya no disponible: {}", userId, e.getMessage());
            }
        });
    }
}
