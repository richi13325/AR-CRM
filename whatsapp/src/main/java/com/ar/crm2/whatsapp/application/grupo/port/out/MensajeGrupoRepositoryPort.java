package com.ar.crm2.whatsapp.application.grupo.port.out;

import com.ar.crm2.whatsapp.domain.entity.MensajeGrupo;
import com.ar.crm2.whatsapp.domain.vo.GrupoId;

import java.util.List;

public interface MensajeGrupoRepositoryPort {
    MensajeGrupo save(MensajeGrupo mensaje);
    boolean existsByWaMessageId(String waMessageId);
    List<MensajeGrupo> findByGrupoOrdenado(GrupoId grupoId);
}
