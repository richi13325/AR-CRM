package com.ar.crm2.whatsapp.application.bot.service;

import com.ar.crm2.whatsapp.application.bot.port.in.FindBotByTokenUseCase;
import com.ar.crm2.whatsapp.application.bot.port.out.FindBotByTokenPort;
import com.ar.crm2.whatsapp.domain.entity.Bot;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class FindBotByTokenService implements FindBotByTokenUseCase {

    private final FindBotByTokenPort findPort;

    @Override
    public Optional<Bot> findByToken(String apiAccessToken) {
        if (apiAccessToken == null || apiAccessToken.isBlank()) return Optional.empty();
        return findPort.findByApiAccessToken(apiAccessToken).filter(Bot::isActivo);
    }
}
