package com.ar.crm2.adapter.out.persistence.mapper;

import com.ar.crm2.adapter.out.persistence.entity.ConversacionEntity;
import com.ar.crm2.model.vo.ContactoId;
import com.ar.crm2.model.vo.UsuarioId;
import com.ar.crm2.whatsapp.domain.entity.Conversacion;
import com.ar.crm2.whatsapp.domain.vo.CanalWhatsappId;
import com.ar.crm2.whatsapp.domain.vo.ConversacionId;

import java.util.UUID;

public final class ConversacionMapper {

    private ConversacionMapper() {}

    public static ConversacionEntity toEntity(Conversacion domain) {
        return ConversacionEntity.builder()
                .id(domain.getId().value().toString())
                .canalId(domain.getCanalId().value().toString())
                .contactoId(domain.getContactoId() != null ? domain.getContactoId().value().toString() : null)
                .numeroTelefono(domain.getNumeroTelefono())
                .nombreContacto(domain.getNombreContacto())
                .estado(domain.getEstado())
                .asignadoA(domain.getAsignadoA() != null ? domain.getAsignadoA().value().toString() : null)
                .creadoEn(domain.getCreadoEn())
                .actualizadoEn(domain.getActualizadoEn())
                .build();
    }

    public static Conversacion toDomain(ConversacionEntity entity) {
        return Conversacion.reconstitute(
                ConversacionId.from(UUID.fromString(entity.getId())),
                CanalWhatsappId.from(UUID.fromString(entity.getCanalId())),
                entity.getContactoId() != null ? ContactoId.from(UUID.fromString(entity.getContactoId())) : null,
                entity.getNumeroTelefono(),
                entity.getNombreContacto(),
                entity.getEstado(),
                entity.getAsignadoA() != null ? UsuarioId.from(UUID.fromString(entity.getAsignadoA())) : null,
                entity.getCreadoEn(),
                entity.getActualizadoEn()
        );
    }
}
