package com.ar.crm2.whatsapp.application.ia.service;

import com.ar.crm2.whatsapp.application.ia.port.in.SugerirRespuestaUseCase;
import com.ar.crm2.whatsapp.application.ia.port.out.GenerarSugerenciaPort;
import com.ar.crm2.whatsapp.application.mensaje.port.out.FindMensajesByConversacionIdPort;
import com.ar.crm2.whatsapp.domain.entity.Mensaje;
import com.ar.crm2.whatsapp.domain.enums.DireccionMensaje;
import com.ar.crm2.whatsapp.domain.vo.ConversacionId;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class SugerirRespuestaService implements SugerirRespuestaUseCase {

    private static final int MAX_MENSAJES = 12;

    private final FindMensajesByConversacionIdPort findMensajesPort;
    private final GenerarSugerenciaPort generarPort;

    @Override
    public String sugerir(UUID conversacionId) {
        List<Mensaje> mensajes = findMensajesPort.findByConversacionId(ConversacionId.from(conversacionId));
        if (mensajes.isEmpty()) {
            throw new IllegalStateException("La conversación no tiene mensajes para sugerir una respuesta");
        }

        // Tomamos los últimos N en orden cronológico y los formateamos como diálogo.
        int desde = Math.max(0, mensajes.size() - MAX_MENSAJES);
        StringBuilder sb = new StringBuilder();
        for (Mensaje m : mensajes.subList(desde, mensajes.size())) {
            String quien = m.getDireccion() == DireccionMensaje.ENTRANTE ? "Cliente" : "Agente";
            String texto = m.getContenido() != null && !m.getContenido().isBlank()
                    ? m.getContenido() : "[adjunto]";
            sb.append(quien).append(": ").append(texto).append("\n");
        }
        return generarPort.generar(sb.toString().trim());
    }
}
