package com.ar.crm2.application.agenda.service;

import com.ar.crm2.application.agenda.command.GetAgendaByIdCommand;
import com.ar.crm2.application.agenda.exception.AgendaNotFoundException;
import com.ar.crm2.application.agenda.port.in.GetAgendaByIdUseCase;
import com.ar.crm2.application.agenda.port.out.FindAgendaByIdPort;
import com.ar.crm2.model.entity.Agenda;
import com.ar.crm2.model.vo.AgendaId;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetAgendaByIdService implements GetAgendaByIdUseCase {

    private final FindAgendaByIdPort findPort;

    @Override
    public Agenda getById(GetAgendaByIdCommand command) {
        AgendaId agendaId = AgendaId.from(command.id());
        return findPort.findById(agendaId)
                .orElseThrow(() -> AgendaNotFoundException.forId(command.id()));
    }
}
