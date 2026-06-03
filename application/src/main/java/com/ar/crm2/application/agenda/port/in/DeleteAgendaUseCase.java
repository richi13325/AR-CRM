package com.ar.crm2.application.agenda.port.in;

import com.ar.crm2.application.agenda.command.DeleteAgendaCommand;

public interface DeleteAgendaUseCase {
    void delete(DeleteAgendaCommand command);
}
