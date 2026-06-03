package com.ar.crm2.application.agenda.port.in;

import com.ar.crm2.application.agenda.command.EditAgendaCommand;
import com.ar.crm2.model.entity.Agenda;

public interface EditAgendaUseCase {
    Agenda edit(EditAgendaCommand command);
}
