package com.ar.crm2.whatsapp.application.canal.port.out;

import com.ar.crm2.whatsapp.domain.entity.CanalWhatsapp;
import java.util.List;

public interface FindAllCanalesPort {
    List<CanalWhatsapp> findAll();
}
