package com.ar.crm2.whatsapp.application.conversacion.port.out;

import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.whatsapp.domain.entity.Conversacion;

import java.util.List;

public interface FindAllConversacionesByEmpresaPort {
    List<Conversacion> findAllByEmpresaId(EmpresaId empresaId);
}
