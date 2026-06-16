package com.ar.crm2.adapter.out.sse;

import com.ar.crm2.whatsapp.application.mensaje.port.out.NotifyNewMensajePort;
import com.ar.crm2.whatsapp.domain.entity.Mensaje;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class SseMensajeNotifyAdapter implements NotifyNewMensajePort {

    private final SseEmitterRegistry registry;

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
}
