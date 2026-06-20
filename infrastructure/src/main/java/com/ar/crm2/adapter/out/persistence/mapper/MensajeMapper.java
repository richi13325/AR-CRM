package com.ar.crm2.adapter.out.persistence.mapper;

import com.ar.crm2.adapter.out.persistence.entity.MensajeEntity;
import com.ar.crm2.model.vo.UsuarioId;
import com.ar.crm2.whatsapp.domain.entity.Mensaje;
import com.ar.crm2.whatsapp.domain.vo.ConversacionId;
import com.ar.crm2.whatsapp.domain.vo.MensajeId;

import java.util.UUID;

public final class MensajeMapper {

    private MensajeMapper() {}

    public static MensajeEntity toEntity(Mensaje domain) {
        return MensajeEntity.builder()
                .id(domain.getId().value().toString())
                .conversacionId(domain.getConversacionId().value().toString())
                .waMessageId(domain.getWaMessageId())
                .tipo(domain.getTipo())
                .direccion(domain.getDireccion())
                .contenido(domain.getContenido())
                .mediaUrl(domain.getMediaUrl())
                .status(domain.getStatus())
                .enviadoPor(domain.getEnviadoPor() != null ? domain.getEnviadoPor().value().toString() : null)
                .interna(domain.isInterna())
                .creadoEn(domain.getCreadoEn())
                .build();
    }

    public static Mensaje toDomain(MensajeEntity entity) {
        return Mensaje.reconstitute(
                MensajeId.from(UUID.fromString(entity.getId())),
                ConversacionId.from(UUID.fromString(entity.getConversacionId())),
                entity.getWaMessageId(),
                entity.getTipo(),
                entity.getDireccion(),
                entity.getContenido(),
                entity.getMediaUrl(),
                entity.getStatus(),
                entity.getEnviadoPor() != null ? UsuarioId.from(UUID.fromString(entity.getEnviadoPor())) : null,
                entity.isInterna(),
                entity.getCreadoEn()
        );
    }
}
