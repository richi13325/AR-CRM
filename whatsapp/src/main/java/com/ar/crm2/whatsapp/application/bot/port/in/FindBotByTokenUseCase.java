package com.ar.crm2.whatsapp.application.bot.port.in;

import com.ar.crm2.whatsapp.domain.entity.Bot;

import java.util.Optional;

/** Usado por el filtro de seguridad que autentica al bot vía header api_access_token. */
public interface FindBotByTokenUseCase {
    Optional<Bot> findByToken(String apiAccessToken);
}
