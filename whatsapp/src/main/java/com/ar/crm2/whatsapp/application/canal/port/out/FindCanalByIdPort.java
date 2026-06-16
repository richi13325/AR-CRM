package com.ar.crm2.whatsapp.application.canal.port.out;

import com.ar.crm2.whatsapp.domain.entity.CanalWhatsapp;
import com.ar.crm2.whatsapp.domain.vo.CanalWhatsappId;

import java.util.Optional;

public interface FindCanalByIdPort {
    Optional<CanalWhatsapp> findById(CanalWhatsappId id);
}
