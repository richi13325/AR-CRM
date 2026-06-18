package com.ar.crm2.whatsapp.application.bot.service;

import com.ar.crm2.whatsapp.application.bot.port.in.DeleteBotUseCase;
import com.ar.crm2.whatsapp.application.bot.port.out.DeleteBotByIdPort;
import com.ar.crm2.whatsapp.domain.vo.BotId;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class DeleteBotService implements DeleteBotUseCase {

    private final DeleteBotByIdPort deletePort;

    @Override
    public void delete(UUID id) {
        deletePort.deleteById(BotId.from(id));
    }
}
