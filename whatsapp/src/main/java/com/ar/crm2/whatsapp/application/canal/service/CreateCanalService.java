package com.ar.crm2.whatsapp.application.canal.service;

import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.whatsapp.application.canal.command.CreateCanalCommand;
import com.ar.crm2.whatsapp.application.canal.port.in.CreateCanalUseCase;
import com.ar.crm2.whatsapp.application.canal.port.out.SaveCanalPort;
import com.ar.crm2.whatsapp.domain.entity.CanalWhatsapp;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateCanalService implements CreateCanalUseCase {

    private final SaveCanalPort savePort;

    @Override
    public CanalWhatsapp create(CreateCanalCommand command) {
        CanalWhatsapp canal = CanalWhatsapp.create(
                EmpresaId.from(command.empresaId()),
                command.nombre(),
                command.instanceName(),
                command.proveedor(),
                command.apiUrl(),
                command.apiKey()
        );
        return savePort.save(canal);
    }
}
