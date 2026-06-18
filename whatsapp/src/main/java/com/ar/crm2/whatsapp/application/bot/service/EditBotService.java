package com.ar.crm2.whatsapp.application.bot.service;

import com.ar.crm2.whatsapp.application.bot.command.EditBotCommand;
import com.ar.crm2.whatsapp.application.bot.port.in.EditBotUseCase;
import com.ar.crm2.whatsapp.application.bot.port.out.FindBotByIdPort;
import com.ar.crm2.whatsapp.application.bot.port.out.SaveBotPort;
import com.ar.crm2.whatsapp.domain.entity.Bot;
import com.ar.crm2.whatsapp.domain.vo.BotId;
import com.ar.crm2.whatsapp.domain.vo.CanalWhatsappId;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EditBotService implements EditBotUseCase {

    private final FindBotByIdPort findPort;
    private final SaveBotPort savePort;

    @Override
    public Bot edit(EditBotCommand command) {
        Bot bot = findPort.findById(BotId.from(command.id()))
                .orElseThrow(() -> new IllegalArgumentException("Bot no encontrado: " + command.id()));
        CanalWhatsappId canalId = command.canalId() != null ? CanalWhatsappId.from(command.canalId()) : null;
        return savePort.save(bot.editar(command.nombre(), canalId, command.webhookUrl()));
    }
}
