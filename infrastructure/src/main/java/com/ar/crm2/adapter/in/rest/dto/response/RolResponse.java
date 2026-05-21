package com.ar.crm2.adapter.in.rest.dto.response;

import com.ar.crm2.model.entity.Rol;

import java.util.UUID;

/**
 * REST response DTO for Rol.
 * Exposes all fields needed for front-end list/create/edit views.
 */
public record RolResponse(
    UUID id,
    String nombre,
    String descripcion,
    boolean activo
) {
    /**
     * Maps a domain Rol to this response DTO.
     */
    public static RolResponse fromDomain(Rol rol) {
        return new RolResponse(
            rol.getId().value(),
            rol.getNombre(),
            rol.getDescripcion(),
            rol.isActivo()
        );
    }
}