package com.ar.crm2.application.rol.service;

import com.ar.crm2.application.rol.command.DeleteRolCommand;
import com.ar.crm2.application.rol.exception.RolHasAssociatedUsuariosException;
import com.ar.crm2.application.rol.exception.RolNotFoundException;
import com.ar.crm2.application.rol.port.in.DeleteRolUseCase;
import com.ar.crm2.application.rol.port.out.DeleteRolByIdPort;
import com.ar.crm2.application.rol.port.out.ExistsUsuariosByRolIdPort;
import com.ar.crm2.application.rol.port.out.FindRolByIdPort;
import com.ar.crm2.model.vo.RolId;
import lombok.RequiredArgsConstructor;

/**
 * Application service implementing DeleteRolUseCase.
 * Validates existence and usuario associations before hard-deleting.
 */
@RequiredArgsConstructor
public class DeleteRolService implements DeleteRolUseCase {

    private final FindRolByIdPort findPort;
    private final ExistsUsuariosByRolIdPort existsUsuariosPort;
    private final DeleteRolByIdPort deletePort;

    @Override
    public void delete(DeleteRolCommand command) {
        RolId rolId = RolId.from(command.id());

        // Verify rol exists
        findPort.findById(rolId)
                .orElseThrow(() -> RolNotFoundException.forId(command.id()));

        // Check for associated usuarios
        if (existsUsuariosPort.existsUsuariosByRolId(rolId)) {
            throw RolHasAssociatedUsuariosException.forId(command.id());
        }

        deletePort.deleteById(rolId);
    }
}