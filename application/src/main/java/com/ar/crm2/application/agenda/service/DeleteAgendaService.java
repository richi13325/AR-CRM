package com.ar.crm2.application.agenda.service;

import com.ar.crm2.application.agenda.command.DeleteAgendaCommand;
import com.ar.crm2.application.agenda.exception.AgendaNotFoundException;
import com.ar.crm2.application.agenda.port.in.DeleteAgendaUseCase;
import com.ar.crm2.application.agenda.port.out.DeleteAgendaByIdPort;
import com.ar.crm2.application.agenda.port.out.FindAgendaByIdPort;
import com.ar.crm2.model.vo.AgendaId;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeleteAgendaService implements DeleteAgendaUseCase {

    private final FindAgendaByIdPort findPort;
    private final DeleteAgendaByIdPort deletePort;

    @Override
    public void delete(DeleteAgendaCommand command) {
        AgendaId agendaId = AgendaId.from(command.id());

        findPort.findById(agendaId)
                .orElseThrow(() -> AgendaNotFoundException.forId(command.id()));

        deletePort.deleteById(agendaId);
    }
}
