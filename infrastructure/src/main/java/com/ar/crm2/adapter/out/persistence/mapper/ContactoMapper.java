package com.ar.crm2.adapter.out.persistence.mapper;

import com.ar.crm2.adapter.out.persistence.entity.ContactoEntity;
import com.ar.crm2.model.entity.Contacto;
import com.ar.crm2.model.enums.EstadoRelacion;
import com.ar.crm2.model.vo.ContactoId;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.model.vo.UsuarioId;

import java.util.UUID;

/**
 * Mapper between persistence entity and domain entity.
 * Handles UUID/String conversion at the persistence boundary.
 */
public final class ContactoMapper {

    private ContactoMapper() {}

    /**
     * Maps a domain Contacto to a persistence entity.
     * Used for save operations.
     */
    public static ContactoEntity toEntity(Contacto domain) {
        return ContactoEntity.builder()
            .id(domain.getId().value().toString())
            .empresaId(domain.getEmpresaId().value().toString())
            .nombre(domain.getNombre())
            .correo(domain.getCorreo())
            .estadoRelacion(domain.getEstadoRelacion())
            .responsableId(domain.getResponsableId() != null ? domain.getResponsableId().value().toString() : null)
            .creadoPor(domain.getCreadoPor() != null ? domain.getCreadoPor().value().toString() : null)
            .telefono(domain.getTelefono())
            .cargo(domain.getCargo())
            .comoNosConocio(domain.getComoNosConocio())
            .creadoEn(domain.getCreadoEn())
            .actualizadoEn(domain.getActualizadoEn())
            .build();
    }

    /**
     * Maps a persistence entity to a domain Contacto.
     * Used for find/load operations.
     */
    public static Contacto toDomain(ContactoEntity entity) {
        if (entity == null) return null;

        return Contacto.reconstitute(
            ContactoId.from(UUID.fromString(entity.getId())),
            EmpresaId.from(UUID.fromString(entity.getEmpresaId())),
            entity.getResponsableId() != null ? UsuarioId.from(UUID.fromString(entity.getResponsableId())) : null,
            entity.getCreadoPor() != null ? UsuarioId.from(UUID.fromString(entity.getCreadoPor())) : null,
            entity.getNombre(),
            entity.getCorreo(),
            entity.getTelefono(),
            entity.getCargo(),
            entity.getComoNosConocio(),
            entity.getCreadoEn(),
            entity.getActualizadoEn(),
            entity.getEstadoRelacion()
        );
    }
}