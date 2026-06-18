package com.ar.crm2.whatsapp.application.bot.port.in;

import com.ar.crm2.whatsapp.domain.entity.Bot;

import java.util.List;

public interface GetAllBotsUseCase {
    List<Bot> getAll();
}
