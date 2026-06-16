package com.ar.crm2.whatsapp.application.canal.service;

import com.ar.crm2.whatsapp.application.canal.port.in.GetCanalByIdUseCase;
import com.ar.crm2.whatsapp.application.canal.port.out.FindCanalByIdPort;
import com.ar.crm2.whatsapp.domain.entity.CanalWhatsapp;
import com.ar.crm2.whatsapp.domain.vo.CanalWhatsappId;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class GetCanalByIdService implements GetCanalByIdUseCase {

    private final FindCanalByIdPort findPort;

    @Override
    public CanalWhatsapp getById(UUID canalId) {
        return findPort.findById(CanalWhatsappId.from(canalId))
                .orElseThrow(() -> new IllegalArgumentException("Canal no encontrado: " + canalId));
    }
}
