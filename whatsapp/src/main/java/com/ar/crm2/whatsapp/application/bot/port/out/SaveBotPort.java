package com.ar.crm2.whatsapp.application.bot.port.out;

import com.ar.crm2.whatsapp.domain.entity.Bot;

public interface SaveBotPort {
    Bot save(Bot bot);
}
