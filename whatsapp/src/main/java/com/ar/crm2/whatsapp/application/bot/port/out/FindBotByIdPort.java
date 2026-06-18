package com.ar.crm2.whatsapp.application.bot.port.out;

import com.ar.crm2.whatsapp.domain.entity.Bot;
import com.ar.crm2.whatsapp.domain.vo.BotId;

import java.util.Optional;

public interface FindBotByIdPort {
    Optional<Bot> findById(BotId id);
}
