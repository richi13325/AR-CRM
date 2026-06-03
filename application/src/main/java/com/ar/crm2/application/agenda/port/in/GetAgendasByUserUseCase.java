package com.ar.crm2.application.agenda.port.in;

import com.ar.crm2.application.agenda.command.GetAgendasByUserCommand;
import com.ar.crm2.model.entity.Agenda;

import java.util.List;

public interface GetAgendasByUserUseCase {
    List<Agenda> getByUser(GetAgendasByUserCommand command);
}
