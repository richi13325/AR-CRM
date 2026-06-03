package com.ar.crm2.application.agenda.port.in;

import com.ar.crm2.application.agenda.command.CreateAgendaCommand;
import com.ar.crm2.model.entity.Agenda;

public interface CreateAgendaUseCase {
    Agenda create(CreateAgendaCommand command);
}
