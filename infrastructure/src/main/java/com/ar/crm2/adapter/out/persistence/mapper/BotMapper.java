package com.ar.crm2.adapter.out.persistence.mapper;

import com.ar.crm2.adapter.out.persistence.entity.BotEntity;
import com.ar.crm2.whatsapp.domain.entity.Bot;
import com.ar.crm2.whatsapp.domain.vo.BotId;
import com.ar.crm2.whatsapp.domain.vo.CanalWhatsappId;

import java.util.UUID;

public final class BotMapper {

    private BotMapper() {}

    public static BotEntity toEntity(Bot domain) {
        return BotEntity.builder()
                .id(domain.getId().value().toString())
                .nombre(domain.getNombre())
                .canalId(domain.getCanalId() != null ? domain.getCanalId().value().toString() : null)
                .webhookUrl(domain.getWebhookUrl())
                .apiAccessToken(domain.getApiAccessToken())
                .activo(domain.isActivo())
                .creadoEn(domain.getCreadoEn())
                .actualizadoEn(domain.getActualizadoEn())
                .build();
    }

    public static Bot toDomain(BotEntity entity) {
        return Bot.reconstitute(
                BotId.from(UUID.fromString(entity.getId())),
                entity.getNombre(),
                entity.getCanalId() != null ? CanalWhatsappId.from(UUID.fromString(entity.getCanalId())) : null,
                entity.getWebhookUrl(),
                entity.getApiAccessToken(),
                entity.isActivo(),
                entity.getCreadoEn(),
                entity.getActualizadoEn()
        );
    }
}
