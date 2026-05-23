package com.ar.crm2.application.usuario.service;

import com.ar.crm2.application.usuario.command.EditUsuarioCommand;
import com.ar.crm2.application.usuario.exception.UsuarioNotFoundException;
import com.ar.crm2.application.usuario.port.in.EditUsuarioUseCase;
import com.ar.crm2.application.usuario.port.out.FindUsuarioByIdPort;
import com.ar.crm2.application.usuario.port.out.SaveUsuarioPort;
import com.ar.crm2.model.entity.Usuario;
import com.ar.crm2.model.vo.RolId;
import com.ar.crm2.model.vo.UsuarioId;
import lombok.RequiredArgsConstructor;

/**
 * Application service implementing EditUsuarioUseCase.
 * Orchestrates loading the aggregate, applying the immutable domain update via reconstitute,
 * and saving. Preserves: id, rolId, creadoEn, activo.
 * No Spring annotations — constructor injection via Lombok.
 */
@RequiredArgsConstructor
public class EditUsuarioService implements EditUsuarioUseCase {

    private final FindUsuarioByIdPort findPort;
    private final SaveUsuarioPort savePort;

    @Override
    public Usuario edit(EditUsuarioCommand command) {
        UsuarioId usuarioId = UsuarioId.from(command.id());

        Usuario existing = findPort.findById(usuarioId)
                .orElseThrow(() -> UsuarioNotFoundException.forId(command.id()));

        String keycloakId = command.keycloakId() != null
                ? command.keycloakId()
                : existing.getKeycloakId();

        Usuario updated = Usuario.reconstitute(
                existing.getId(),
                command.nombre(),
                command.correo(),
                existing.getPasswordHash(),
                existing.getRolId(),
                existing.getCreadoEn(),
                existing.isActivo(),
                keycloakId
        );

        return savePort.save(updated);
    }
}