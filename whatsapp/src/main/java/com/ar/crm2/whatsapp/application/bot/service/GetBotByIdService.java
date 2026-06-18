package com.ar.crm2.whatsapp.application.bot.service;

import com.ar.crm2.whatsapp.application.bot.port.in.GetBotByIdUseCase;
import com.ar.crm2.whatsapp.application.bot.port.out.FindBotByIdPort;
import com.ar.crm2.whatsapp.domain.entity.Bot;
import com.ar.crm2.whatsapp.domain.vo.BotId;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class GetBotByIdService implements GetBotByIdUseCase {

    private final FindBotByIdPort findPort;

    @Override
    public Bot getById(UUID id) {
        return findPort.findById(BotId.from(id))
                .orElseThrow(() -> new IllegalArgumentException("Bot no encontrado: " + id));
    }
}
