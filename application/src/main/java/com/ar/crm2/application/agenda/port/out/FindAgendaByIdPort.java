package com.ar.crm2.application.agenda.port.out;

import com.ar.crm2.model.entity.Agenda;
import com.ar.crm2.model.vo.AgendaId;

import java.util.Optional;

public interface FindAgendaByIdPort {
    Optional<Agenda> findById(AgendaId id);
}
