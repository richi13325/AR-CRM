package com.ar.crm2.whatsapp.application.canal.port.in;

import com.ar.crm2.whatsapp.application.canal.command.EditCanalCommand;
import com.ar.crm2.whatsapp.domain.entity.CanalWhatsapp;

public interface EditCanalUseCase {
    CanalWhatsapp edit(EditCanalCommand command);
}
