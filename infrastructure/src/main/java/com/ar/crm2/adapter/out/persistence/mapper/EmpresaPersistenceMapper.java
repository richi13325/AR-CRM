package com.ar.crm2.adapter.out.persistence.mapper;

import com.ar.crm2.adapter.out.persistence.entity.EmpresaEntity;
import com.ar.crm2.model.entity.Empresa;
import com.ar.crm2.model.enums.EstadoRelacion;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.model.vo.UsuarioId;

/**
 * Mapper between persistence entity and domain entity.
 */
public final class EmpresaPersistenceMapper {

    private EmpresaPersistenceMapper() {}

    /**
     * Maps a domain Empresa to a persistence entity.
     * Used for save operations.
     */
    public static EmpresaEntity toEntity(Empresa domain) {
        return EmpresaEntity.builder()
            .id(domain.getId().value())
            .nombre(domain.getNombre())
            .sector(domain.getSector())
            .telefono(domain.getTelefono())
            .paginaWeb(domain.getPaginaWeb())
            .facebook(domain.getFacebook())
            .instagram(domain.getInstagram())
            .twitter(domain.getTwitter())
            .estadoRelacion(domain.getEstadoRelacion())
            .responsableId(domain.getResponsableId() != null ? domain.getResponsableId().value() : null)
            .creadoPor(domain.getCreadoPor() != null ? domain.getCreadoPor().value() : null)
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
            EmpresaId.from(entity.getId()),
            entity.getNombre(),
            entity.getSector(),
            entity.getTelefono(),
            entity.getPaginaWeb(),
            entity.getFacebook(),
            entity.getInstagram(),
            entity.getTwitter(),
            entity.getEstadoRelacion(),
            entity.getResponsableId() != null ? UsuarioId.from(entity.getResponsableId()) : null,
            entity.getCreadoPor() != null ? UsuarioId.from(entity.getCreadoPor()) : null,
            entity.getNotas(),
            entity.getCreadoEn(),
            entity.getActualizadoEn()
        );
    }
}