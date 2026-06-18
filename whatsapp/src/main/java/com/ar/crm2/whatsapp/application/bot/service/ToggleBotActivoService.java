package com.ar.crm2.whatsapp.application.bot.service;

import com.ar.crm2.whatsapp.application.bot.port.in.ToggleBotActivoUseCase;
import com.ar.crm2.whatsapp.application.bot.port.out.FindBotByIdPort;
import com.ar.crm2.whatsapp.application.bot.port.out.SaveBotPort;
import com.ar.crm2.whatsapp.domain.entity.Bot;
import com.ar.crm2.whatsapp.domain.vo.BotId;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class ToggleBotActivoService implements ToggleBotActivoUseCase {

    private final FindBotByIdPort findPort;
    private final SaveBotPort savePort;

    @Override
    public Bot activar(UUID id) {
        return savePort.save(find(id).activar());
    }

    @Override
    public Bot desactivar(UUID id) {
        return savePort.save(find(id).desactivar());
    }

    private Bot find(UUID id) {
        return findPort.findById(BotId.from(id))
                .orElseThrow(() -> new IllegalArgumentException("Bot no encontrado: " + id));
    }
}
