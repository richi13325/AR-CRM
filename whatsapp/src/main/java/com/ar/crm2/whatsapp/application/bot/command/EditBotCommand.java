package com.ar.crm2.whatsapp.application.bot.command;

import java.util.UUID;

public record EditBotCommand(UUID id, String nombre, UUID canalId, String webhookUrl) {
}
