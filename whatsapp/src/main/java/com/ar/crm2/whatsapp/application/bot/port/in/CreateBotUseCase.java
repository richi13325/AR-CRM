package com.ar.crm2.whatsapp.application.bot.port.in;

import com.ar.crm2.whatsapp.application.bot.command.CreateBotCommand;
import com.ar.crm2.whatsapp.domain.entity.Bot;

public interface CreateBotUseCase {
    Bot create(CreateBotCommand command);
}
