package com.ar.crm2.whatsapp.application.canal.port.in;

import com.ar.crm2.whatsapp.domain.entity.CanalWhatsapp;

import java.util.List;
import java.util.UUID;

public interface GetAllCanalesUseCase {
    List<CanalWhatsapp> getAll(UUID empresaId);
}
