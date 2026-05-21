package com.ar.crm2.adapter.in.rest.dto.response;

import com.ar.crm2.model.entity.Contacto;
import com.ar.crm2.model.enums.EstadoRelacion;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * REST response DTO for Contacto.
 * Exposes all fields needed for front-end list/create views.
 */
public record ContactoResponse(
    UUID id,
    UUID empresaId,
    String nombre,
    String correo,
    EstadoRelacion estadoRelacion,
    UUID responsableId,
    UUID creadoPor,
    String telefono,
    String cargo,
    String comoNosConocio,
    LocalDateTime creadoEn,
    LocalDateTime actualizadoEn
) {
    /**
     * Maps a domain Contacto to this response DTO.
     */
    public static ContactoResponse fromDomain(Contacto contacto) {
        return new ContactoResponse(
            contacto.getId().value(),
            contacto.getEmpresaId().value(),
            contacto.getNombre(),
            contacto.getCorreo(),
            contacto.getEstadoRelacion(),
            contacto.getResponsableId() != null ? contacto.getResponsableId().value() : null,
            contacto.getCreadoPor() != null ? contacto.getCreadoPor().value() : null,
            contacto.getTelefono(),
            contacto.getCargo(),
            contacto.getComoNosConocio(),
            contacto.getCreadoEn(),
            contacto.getActualizadoEn()
        );
    }
}