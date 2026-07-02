package com.ar.crm2.adapter.out.whatsapp;

import com.ar.crm2.adapter.out.persistence.MensajeRepositoryAdapter;
import com.ar.crm2.application.ai.port.out.projection.WhatsappMensajeResumen;
import com.ar.crm2.application.ai.port.out.WhatsappMensajeLecturaPort;
import com.ar.crm2.whatsapp.domain.entity.Mensaje;
import com.ar.crm2.whatsapp.domain.vo.ConversacionId;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Read-side bridge adapter that exposes WhatsApp messages through the
 * application-owned {@link WhatsappMensajeLecturaPort}.
 *
 * <p>This adapter is the <em>only</em> place where the
 * {@code application} layer is allowed to read WhatsApp messages.
 * The mapping from {@code whatsapp.domain.entity.Mensaje} to the
 * application projection {@link WhatsappMensajeResumen} happens here
 * so the rest of the application is decoupled from the
 * {@code whatsapp} module (see design.md §Module Boundaries).
 *
 * <p><b>Safety boundary:</b> this is a read-only adapter. It never
 * writes to WhatsApp persistence and never invokes any AI mutation
 * use case. The chat-analyzer service loads the transcript here, the
 * model/tool layer reads it, and execution happens through a separate
 * staged-proposal path.
 */
@RequiredArgsConstructor
public class WhatsappMensajeLecturaAdapter implements WhatsappMensajeLecturaPort {

    private final MensajeRepositoryAdapter mensajeRepositoryAdapter;

    @Override
    public List<WhatsappMensajeResumen> findByConversacionId(UUID waConversacionId) {
        List<Mensaje> mensajes = mensajeRepositoryAdapter
            .findByConversacionId(ConversacionId.from(waConversacionId));

        return mensajes.stream()
            .map(m -> new WhatsappMensajeResumen(
                m.getId().value(),
                m.getConversacionId().value(),
                m.getDireccion().name(),
                m.getTipo().name(),
                m.getContenido(),
                m.getMediaUrl(),
                m.getCreadoEn()
            ))
            .toList();
    }
}
