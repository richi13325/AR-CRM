package com.ar.crm2.whatsapp.application.conversacion.service;

import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.whatsapp.application.conversacion.port.in.GetAllConversacionesUseCase;
import com.ar.crm2.whatsapp.application.conversacion.port.out.FindAllConversacionesByEmpresaPort;
import com.ar.crm2.whatsapp.domain.entity.Conversacion;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class GetAllConversacionesService implements GetAllConversacionesUseCase {

    private final FindAllConversacionesByEmpresaPort findPort;

    @Override
    public List<Conversacion> getAll(UUID empresaId) {
        return findPort.findAllByEmpresaId(EmpresaId.from(empresaId));
    }
}
