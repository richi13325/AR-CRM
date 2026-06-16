package com.ar.crm2.whatsapp.application.canal.port.in;

import com.ar.crm2.whatsapp.application.canal.command.CreateCanalCommand;
import com.ar.crm2.whatsapp.domain.entity.CanalWhatsapp;

public interface CreateCanalUseCase {
    CanalWhatsapp create(CreateCanalCommand command);
}
