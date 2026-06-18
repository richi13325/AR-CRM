package com.ar.crm2.whatsapp.application.canal.service;

import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.whatsapp.application.canal.port.in.GetAllCanalesUseCase;
import com.ar.crm2.whatsapp.application.canal.port.out.FindAllCanalesByEmpresaPort;
import com.ar.crm2.whatsapp.application.canal.port.out.FindAllCanalesPort;
import com.ar.crm2.whatsapp.domain.entity.CanalWhatsapp;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class GetAllCanalesService implements GetAllCanalesUseCase {

    private final FindAllCanalesByEmpresaPort findPort;
    private final FindAllCanalesPort findAllPort;

    @Override
    public List<CanalWhatsapp> getAll(UUID empresaId) {
        return findPort.findAllByEmpresaId(EmpresaId.from(empresaId));
    }

    @Override
    public List<CanalWhatsapp> getAll() {
        return findAllPort.findAll();
    }
}
