package com.ar.crm2.whatsapp.application.canal.port.in;

import com.ar.crm2.whatsapp.domain.entity.CanalWhatsapp;

import java.util.UUID;

public interface GetCanalByIdUseCase {
    CanalWhatsapp getById(UUID canalId);
}
