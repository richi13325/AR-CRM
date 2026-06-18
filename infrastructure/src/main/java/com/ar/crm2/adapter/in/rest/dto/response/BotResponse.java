package com.ar.crm2.adapter.in.rest.dto.response;

import com.ar.crm2.whatsapp.domain.entity.Bot;

import java.time.LocalDateTime;
import java.util.UUID;

public record BotResponse(
        UUID id,
        String nombre,
        UUID canalId,
        String webhookUrl,
        String apiAccessToken,
        boolean activo,
        LocalDateTime creadoEn,
        LocalDateTime actualizadoEn
) {
    public static BotResponse fromDomain(Bot bot) {
        return new BotResponse(
                bot.getId().value(),
                bot.getNombre(),
                bot.getCanalId() != null ? bot.getCanalId().value() : null,
                bot.getWebhookUrl(),
                bot.getApiAccessToken(),
                bot.isActivo(),
                bot.getCreadoEn(),
                bot.getActualizadoEn()
        );
    }
}
