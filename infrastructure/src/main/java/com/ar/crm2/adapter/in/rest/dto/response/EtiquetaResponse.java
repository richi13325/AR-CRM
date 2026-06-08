package com.ar.crm2.adapter.in.rest.dto.response;

import com.ar.crm2.model.entity.Etiqueta;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * REST response DTO for Etiqueta.
 * Exposes the catalog row's id, name, type, color, and creation timestamp.
 */
public record EtiquetaResponse(
    UUID id,
    String nombre,
    String tipoEtiqueta,
    String color,
    LocalDateTime creadoEn
) {
    /**
     * Maps a domain Etiqueta to this response DTO.
     *
     * <p>Tipo is exposed as the enum name (string) for stable JSON
     * serialization that does not depend on the enum ordinal.
     */
    public static EtiquetaResponse fromDomain(Etiqueta etiqueta) {
        return new EtiquetaResponse(
            etiqueta.getId().value(),
            etiqueta.getNombre(),
            etiqueta.getTipoEtiqueta().name(),
            etiqueta.getColor(),
            etiqueta.getCreadoEn()
        );
    }
}
