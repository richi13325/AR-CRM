package com.ar.crm2.whatsapp.application.bot.port.out;

import com.ar.crm2.whatsapp.domain.vo.BotId;

public interface DeleteBotByIdPort {
    void deleteById(BotId id);
}
