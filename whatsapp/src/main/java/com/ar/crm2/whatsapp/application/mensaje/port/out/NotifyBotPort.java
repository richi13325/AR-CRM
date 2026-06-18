package com.ar.crm2.whatsapp.application.mensaje.port.out;

import com.ar.crm2.whatsapp.domain.entity.CanalWhatsapp;
import com.ar.crm2.whatsapp.domain.entity.Conversacion;
import com.ar.crm2.whatsapp.domain.entity.Mensaje;

/** Notifica al bot de n8n (webhook saliente) cuando llega un mensaje entrante. Best-effort. */
public interface NotifyBotPort {
    void notificarMensajeEntrante(Conversacion conversacion, Mensaje mensaje, CanalWhatsapp canal);
}
