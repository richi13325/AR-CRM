package com.ar.crm2.application.agenda.port.in;

import com.ar.crm2.application.agenda.command.GetAgendaByIdCommand;
import com.ar.crm2.model.entity.Agenda;

public interface GetAgendaByIdUseCase {
    Agenda getById(GetAgendaByIdCommand command);
}
