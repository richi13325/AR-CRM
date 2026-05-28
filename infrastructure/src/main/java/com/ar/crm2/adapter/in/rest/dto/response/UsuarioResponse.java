package com.ar.crm2.adapter.in.rest.dto.response;

import com.ar.crm2.model.entity.Usuario;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * REST response DTO for Usuario data.
 */
public record UsuarioResponse(
    UUID id,
    String nombre,
    String correo,
    UUID rolId,
    LocalDateTime creadoEn,
    boolean activo,
    String keycloakId
) {

    /**
     * Factory method from domain entity.
     */
    public static UsuarioResponse fromDomain(Usuario usuario) {
        return new UsuarioResponse(
            usuario.getId().value(),
            usuario.getNombre(),
            usuario.getCorreo(),
            usuario.getRolId().value(),
            usuario.getCreadoEn(),
            usuario.isActivo(),
            usuario.getKeycloakId()
        );
    }
}