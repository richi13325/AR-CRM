package com.ar.crm2.whatsapp.application.canal.service;

import com.ar.crm2.whatsapp.application.canal.command.EditCanalCommand;
import com.ar.crm2.whatsapp.application.canal.port.in.EditCanalUseCase;
import com.ar.crm2.whatsapp.application.canal.port.out.FindCanalByIdPort;
import com.ar.crm2.whatsapp.application.canal.port.out.SaveCanalPort;
import com.ar.crm2.whatsapp.domain.entity.CanalWhatsapp;
import com.ar.crm2.whatsapp.domain.vo.CanalWhatsappId;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EditCanalService implements EditCanalUseCase {

    private final FindCanalByIdPort findPort;
    private final SaveCanalPort savePort;

    @Override
    public CanalWhatsapp edit(EditCanalCommand command) {
        CanalWhatsapp canal = findPort.findById(CanalWhatsappId.from(command.canalId()))
                .orElseThrow(() -> new IllegalArgumentException("Canal no encontrado: " + command.canalId()));

        CanalWhatsapp editado = canal.editar(command.nombre(), command.apiUrl(), command.apiKey());
        return savePort.save(editado);
    }
}
