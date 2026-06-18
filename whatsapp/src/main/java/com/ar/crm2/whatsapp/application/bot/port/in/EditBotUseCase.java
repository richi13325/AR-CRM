package com.ar.crm2.whatsapp.application.bot.port.in;

import com.ar.crm2.whatsapp.application.bot.command.EditBotCommand;
import com.ar.crm2.whatsapp.domain.entity.Bot;

public interface EditBotUseCase {
    Bot edit(EditBotCommand command);
}
