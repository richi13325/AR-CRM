package com.ar.crm2.whatsapp.application.bot.port.in;

import com.ar.crm2.whatsapp.domain.entity.Bot;

import java.util.UUID;

public interface ToggleBotActivoUseCase {
    Bot activar(UUID id);

    Bot desactivar(UUID id);
}
