package com.ar.crm2.application.superusuario.service;

import com.ar.crm2.application.superusuario.command.DeleteSuperUsuarioCommand;
import com.ar.crm2.application.superusuario.exception.SuperUsuarioNotFoundException;
import com.ar.crm2.application.superusuario.port.in.DeleteSuperUsuarioUseCase;
import com.ar.crm2.application.superusuario.port.out.DeleteSuperUsuarioByIdPort;
import com.ar.crm2.application.superusuario.port.out.FindSuperUsuarioByIdPort;
import com.ar.crm2.model.vo.SuperUsuarioId;
import lombok.RequiredArgsConstructor;

/**
 * Application service implementing DeleteSuperUsuarioUseCase.
 * Validates existence before deleting.
 * No Spring annotations — constructor injection via Lombok.
 */
@RequiredArgsConstructor
public class DeleteSuperUsuarioService implements DeleteSuperUsuarioUseCase {

    private final FindSuperUsuarioByIdPort findPort;
    private final DeleteSuperUsuarioByIdPort deletePort;

    @Override
    public void delete(DeleteSuperUsuarioCommand command) {
        SuperUsuarioId superUsuarioId = SuperUsuarioId.from(command.id());

        // Verify superUsuario exists
        findPort.findById(superUsuarioId)
                .orElseThrow(() -> SuperUsuarioNotFoundException.forId(command.id()));

        deletePort.deleteById(superUsuarioId);
    }
}