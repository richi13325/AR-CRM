package com.ar.crm2.whatsapp.application.bot.service;

import com.ar.crm2.whatsapp.application.bot.port.in.GetAllBotsUseCase;
import com.ar.crm2.whatsapp.application.bot.port.out.FindAllBotsPort;
import com.ar.crm2.whatsapp.domain.entity.Bot;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class GetAllBotsService implements GetAllBotsUseCase {

    private final FindAllBotsPort findAllPort;

    @Override
    public List<Bot> getAll() {
        return findAllPort.findAll();
    }
}
