package com.ar.crm2.application.agenda.port.out;

import com.ar.crm2.model.entity.Agenda;

public interface SaveAgendaPort {
    Agenda save(Agenda agenda);
}
