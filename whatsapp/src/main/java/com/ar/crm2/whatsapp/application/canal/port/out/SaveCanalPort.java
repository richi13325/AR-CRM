package com.ar.crm2.whatsapp.application.canal.port.out;

import com.ar.crm2.whatsapp.domain.entity.CanalWhatsapp;

public interface SaveCanalPort {
    CanalWhatsapp save(CanalWhatsapp canal);
}
