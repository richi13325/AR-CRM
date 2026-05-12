package com.ar.crm2.adapter.in.rest.dto.response;

import com.ar.crm2.model.entity.Empresa;
import com.ar.crm2.model.enums.EstadoRelacion;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * REST response DTO for Empresa.
 * Exposes all fields needed for front-end list/create views.
 */
public record EmpresaResponse(
    UUID id,
    String nombre,
    String sector,
    String telefono,
    String paginaWeb,
    String facebook,
    String instagram,
    String twitter,
    EstadoRelacion estadoRelacion,
    UUID responsableId,
    UUID creadoPor,
    String notas,
    LocalDateTime creadoEn,
    LocalDateTime actualizadoEn
) {
    /**
     * Maps a domain Empresa to this response DTO.
     */
    public static EmpresaResponse fromDomain(Empresa empresa) {
        return new EmpresaResponse(
            empresa.getId().value(),
            empresa.getNombre(),
            empresa.getSector(),
            empresa.getTelefono(),
            empresa.getPaginaWeb(),
            empresa.getFacebook(),
            empresa.getInstagram(),
            empresa.getTwitter(),
            empresa.getEstadoRelacion(),
            empresa.getResponsableId() != null ? empresa.getResponsableId().value() : null,
            empresa.getCreadoPor() != null ? empresa.getCreadoPor().value() : null,
            empresa.getNotas(),
            empresa.getCreadoEn(),
            empresa.getActualizadoEn()
        );
    }
}