package com.ar.crm2.whatsapp.application.bot.port.out;

import com.ar.crm2.whatsapp.domain.entity.Bot;

import java.util.List;

public interface FindAllBotsPort {
    List<Bot> findAll();
}
