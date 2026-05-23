package com.ar.crm2.application.superusuario.service;

import com.ar.crm2.application.superusuario.command.EditSuperUsuarioCommand;
import com.ar.crm2.application.superusuario.exception.SuperUsuarioNotFoundException;
import com.ar.crm2.application.superusuario.port.in.EditSuperUsuarioUseCase;
import com.ar.crm2.application.superusuario.port.out.FindSuperUsuarioByIdPort;
import com.ar.crm2.application.superusuario.port.out.SaveSuperUsuarioPort;
import com.ar.crm2.model.entity.SuperUsuario;
import com.ar.crm2.model.vo.SuperUsuarioId;
import lombok.RequiredArgsConstructor;

/**
 * Application service implementing EditSuperUsuarioUseCase.
 * Orchestrates loading the aggregate, applying the immutable domain update via reconstitute,
 * and saving. Preserves: id, passwordHash, creadoEn, activo.
 * No Spring annotations — constructor injection via Lombok.
 */
@RequiredArgsConstructor
public class EditSuperUsuarioService implements EditSuperUsuarioUseCase {

    private final FindSuperUsuarioByIdPort findPort;
    private final SaveSuperUsuarioPort savePort;

    @Override
    public SuperUsuario edit(EditSuperUsuarioCommand command) {
        SuperUsuarioId superUsuarioId = SuperUsuarioId.from(command.id());

        SuperUsuario existing = findPort.findById(superUsuarioId)
                .orElseThrow(() -> SuperUsuarioNotFoundException.forId(command.id()));

        String keycloakId = command.keycloakId() != null
                ? command.keycloakId()
                : existing.getKeycloakId();

        SuperUsuario updated = SuperUsuario.reconstitute(
                existing.getId(),
                command.correo(),
                existing.getPasswordHash(),
                existing.getCreadoEn(),
                existing.isActivo(),
                keycloakId
        );

        return savePort.save(updated);
    }
}