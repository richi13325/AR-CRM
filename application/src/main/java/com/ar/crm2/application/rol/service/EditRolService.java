package com.ar.crm2.application.rol.service;

import com.ar.crm2.application.rol.command.EditRolCommand;
import com.ar.crm2.application.rol.exception.RolNotFoundException;
import com.ar.crm2.application.rol.port.in.EditRolUseCase;
import com.ar.crm2.application.rol.port.out.FindRolByIdPort;
import com.ar.crm2.application.rol.port.out.SaveRolPort;
import com.ar.crm2.model.entity.Rol;
import com.ar.crm2.model.vo.RolId;
import lombok.RequiredArgsConstructor;

/**
 * Application service implementing EditRolUseCase.
 * Orchestrates loading the aggregate, applying the immutable domain update via reconstitute,
 * and saving. Preserves: id and activo.
 */
@RequiredArgsConstructor
public class EditRolService implements EditRolUseCase {

    private final FindRolByIdPort findPort;
    private final SaveRolPort savePort;

    @Override
    public Rol edit(EditRolCommand command) {
        RolId rolId = RolId.from(command.id());

        Rol existing = findPort.findById(rolId)
                .orElseThrow(() -> RolNotFoundException.forId(command.id()));

        Rol updated = Rol.reconstitute(
                existing.getId(),
                command.nombre(),
                command.descripcion(),
                existing.isActivo()
        );

        return savePort.save(updated);
    }
}