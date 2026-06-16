package com.ar.crm2.whatsapp.application.canal.port.out;

import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.whatsapp.domain.entity.CanalWhatsapp;

import java.util.List;

public interface FindAllCanalesByEmpresaPort {
    List<CanalWhatsapp> findAllByEmpresaId(EmpresaId empresaId);
}
