package com.ar.crm2.whatsapp.application.grupo.port.out;

import com.ar.crm2.whatsapp.domain.entity.Grupo;
import com.ar.crm2.whatsapp.domain.vo.GrupoId;

import java.util.List;
import java.util.Optional;

public interface GrupoRepositoryPort {
    Grupo save(Grupo grupo);
    Optional<Grupo> findByJid(String jid);
    Optional<Grupo> findById(GrupoId id);
    List<Grupo> findAll();
}
