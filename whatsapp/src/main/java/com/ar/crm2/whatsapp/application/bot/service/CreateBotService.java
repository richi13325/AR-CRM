package com.ar.crm2.whatsapp.application.bot.service;

import com.ar.crm2.whatsapp.application.bot.command.CreateBotCommand;
import com.ar.crm2.whatsapp.application.bot.port.in.CreateBotUseCase;
import com.ar.crm2.whatsapp.application.bot.port.out.SaveBotPort;
import com.ar.crm2.whatsapp.domain.entity.Bot;
import com.ar.crm2.whatsapp.domain.vo.CanalWhatsappId;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateBotService implements CreateBotUseCase {

    private final SaveBotPort savePort;

    @Override
    public Bot create(CreateBotCommand command) {
        CanalWhatsappId canalId = command.canalId() != null ? CanalWhatsappId.from(command.canalId()) : null;
        Bot bot = Bot.create(command.nombre(), canalId, command.webhookUrl());
        return savePort.save(bot);
    }
}
