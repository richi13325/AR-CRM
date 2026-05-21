package com.ar.crm2.adapter.out.persistence.mapper;

import com.ar.crm2.adapter.out.persistence.entity.EmpresaEntity;
import com.ar.crm2.model.entity.Empresa;
import com.ar.crm2.model.enums.EstadoRelacion;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.model.vo.UsuarioId;

/**
 * Mapper between persistence entity and domain entity.
 */
public final class EmpresaMapper {

    private EmpresaMapper() {}

    /**
     * Maps a domain Empresa to a persistence entity.
     * Used for save operations.
     */
    public static EmpresaEntity toEntity(Empresa domain) {
        return EmpresaEntity.builder()
            .id(domain.getId().value().toString())
            .nombre(domain.getNombre())
            .sector(domain.getSector())
            .telefono(domain.getTelefono())
            .paginaWeb(domain.getPaginaWeb())
            .facebook(domain.getFacebook())
            .instagram(domain.getInstagram())
            .twitter(domain.getTwitter())
            .estadoRelacion(domain.getEstadoRelacion())
            .responsableId(domain.getResponsableId() != null ? domain.getResponsableId().value().toString() : null)
            .creadoPor(domain.getCreadoPor() != null ? domain.getCreadoPor().value().toString() : null)
            .notas(domain.getNotas())
            .creadoEn(domain.getCreadoEn())
            .actualizadoEn(domain.getActualizadoEn())
            .build();
    }

    /**
     * Maps a persistence entity to a domain Empresa.
     * Used for find/load operations.
     */
    public static Empresa toDomain(EmpresaEntity entity) {
        if (entity == null) return null;

        return Empresa.reconstitute(
            EmpresaId.from(java.util.UUID.fromString(entity.getId())),
            entity.getNombre(),
            entity.getSector(),
            entity.getTelefono(),
            entity.getPaginaWeb(),
            entity.getFacebook(),
            entity.getInstagram(),
            entity.getTwitter(),
            entity.getEstadoRelacion(),
            entity.getResponsableId() != null ? UsuarioId.from(java.util.UUID.fromString(entity.getResponsableId())) : null,
            entity.getCreadoPor() != null ? UsuarioId.from(java.util.UUID.fromString(entity.getCreadoPor())) : null,
            entity.getNotas(),
            entity.getCreadoEn(),
            entity.getActualizadoEn()
        );
    }
}