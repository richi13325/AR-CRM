package com.ar.crm2.whatsapp.application.bot.port.out;

import com.ar.crm2.whatsapp.domain.entity.Bot;
import com.ar.crm2.whatsapp.domain.vo.CanalWhatsappId;

import java.util.Optional;

/** Resuelve el bot activo que debe atender mensajes de un canal (específico o "todos"). */
public interface FindBotActivoParaCanalPort {
    Optional<Bot> findActivoParaCanal(CanalWhatsappId canalId);
}
