package com.ar.crm2.whatsapp.application.mensaje.service;

import com.ar.crm2.whatsapp.application.mensaje.port.out.FindMensajeByWaMessageIdPort;
import com.ar.crm2.whatsapp.application.mensaje.port.out.NotifyEstadoMensajePort;
import com.ar.crm2.whatsapp.application.mensaje.port.out.SaveMensajePort;
import com.ar.crm2.whatsapp.domain.entity.Mensaje;
import com.ar.crm2.whatsapp.domain.enums.StatusMensaje;
import lombok.RequiredArgsConstructor;

/**
 * Aplica los acuses (MESSAGES_UPDATE de Evolution) sobre el estado de un mensaje
 * saliente y empuja el cambio por SSE para actualizar los checks en vivo.
 */
@RequiredArgsConstructor
public class ActualizarStatusMensajeService {

    private final FindMensajeByWaMessageIdPort findPort;
    private final SaveMensajePort savePort;
    private final NotifyEstadoMensajePort notifyPort;

    public void actualizar(String waMessageId, String statusEvolution) {
        StatusMensaje nuevo = mapStatus(statusEvolution);
        if (waMessageId == null || nuevo == null) return;

        findPort.findByWaMessageId(waMessageId).ifPresent(mensaje -> {
            if (mensaje.getStatus() == nuevo) return;
            // No degradar: si ya está LEIDO, un acuse de ENTREGADO tardío no lo baja.
            if (rango(mensaje.getStatus()) > rango(nuevo)) return;
            Mensaje actualizado = savePort.save(mensaje.actualizarStatus(nuevo));
            notifyPort.notifyEstado(actualizado);
        });
    }

    private StatusMensaje mapStatus(String s) {
        if (s == null) return null;
        return switch (s.toUpperCase()) {
            case "SENT", "SERVER_ACK" -> StatusMensaje.ENVIADO;
            case "DELIVERED", "DELIVERY_ACK" -> StatusMensaje.ENTREGADO;
            case "READ", "READ_ACK", "PLAYED" -> StatusMensaje.LEIDO;
            case "ERROR" -> StatusMensaje.FALLIDO;
            default -> null;
        };
    }

    private int rango(StatusMensaje s) {
        return switch (s) {
            case ENVIADO -> 1;
            case ENTREGADO -> 2;
            case LEIDO -> 3;
            case FALLIDO -> 0;
        };
    }
}
