package com.ar.crm2.application.agenda.service;

import com.ar.crm2.application.agenda.command.GetAgendasByUserCommand;
import com.ar.crm2.application.agenda.port.in.GetAgendasByUserUseCase;
import com.ar.crm2.application.agenda.port.out.FindAgendasByUserIdPort;
import com.ar.crm2.model.entity.Agenda;
import com.ar.crm2.model.vo.UsuarioId;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class GetAgendasByUserService implements GetAgendasByUserUseCase {

    private final FindAgendasByUserIdPort findByUserPort;

    @Override
    public List<Agenda> getByUser(GetAgendasByUserCommand command) {
        return findByUserPort.findByCreadoPor(UsuarioId.from(command.usuarioId()));
    }
}
