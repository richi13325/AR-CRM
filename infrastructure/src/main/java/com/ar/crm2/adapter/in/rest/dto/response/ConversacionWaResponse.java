package com.ar.crm2.adapter.in.rest.dto.response;

import com.ar.crm2.whatsapp.domain.entity.Conversacion;
import com.ar.crm2.whatsapp.domain.enums.EstadoConversacion;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record ConversacionWaResponse(
        UUID id,
        UUID canalId,
        UUID contactoId,
        String numeroTelefono,
        String nombreContacto,
        EstadoConversacion estado,
        UUID asignadoA,
        int noLeidos,
        LocalDateTime ultimoMensajeAt,
        String ultimoMensajeTexto,
        Set<String> labels,
        boolean botActivo,
        LocalDateTime creadoEn,
        LocalDateTime actualizadoEn
) {
    public static ConversacionWaResponse fromDomain(Conversacion c) {
        return new ConversacionWaResponse(
                c.getId().value(),
                c.getCanalId().value(),
                c.getContactoId() != null ? c.getContactoId().value() : null,
                c.getNumeroTelefono(),
                c.getNombreContacto(),
                c.getEstado(),
                c.getAsignadoA() != null ? c.getAsignadoA().value() : null,
                c.getNoLeidos(),
                c.getUltimoMensajeAt(),
                c.getUltimoMensajeTexto(),
                c.getLabels(),
                c.isBotActivo(),
                c.getCreadoEn(),
                c.getActualizadoEn()
        );
    }
}
