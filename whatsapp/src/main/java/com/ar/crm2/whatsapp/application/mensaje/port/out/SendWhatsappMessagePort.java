package com.ar.crm2.whatsapp.application.mensaje.port.out;

import com.ar.crm2.whatsapp.domain.entity.CanalWhatsapp;
import com.ar.crm2.whatsapp.domain.enums.TipoMensaje;

public interface SendWhatsappMessagePort {
    String send(CanalWhatsapp canal, String numeroDestino, TipoMensaje tipo, String contenido, String mediaUrl);
}
