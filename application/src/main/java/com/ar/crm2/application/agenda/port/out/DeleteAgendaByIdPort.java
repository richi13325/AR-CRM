package com.ar.crm2.application.agenda.port.out;

import com.ar.crm2.model.vo.AgendaId;

public interface DeleteAgendaByIdPort {
    void deleteById(AgendaId id);
}
