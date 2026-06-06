package com.ar.crm2.application.usuario.service;

import com.ar.crm2.application.identity.port.out.SetIdentityEnabledPort;
import com.ar.crm2.application.identity.port.out.SyncIdentityEmailPort;
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
 * Syncs email and enabled state to Keycloak before local update.
 * No Spring annotations — constructor injection via Lombok.
 */
@RequiredArgsConstructor
public class EditUsuarioService implements EditUsuarioUseCase {

    private final FindUsuarioByIdPort findPort;
    private final SaveUsuarioPort savePort;
    private final SyncIdentityEmailPort syncEmailPort;
    private final SetIdentityEnabledPort setEnabledPort;

    @Override
    public Usuario edit(EditUsuarioCommand command) {
        UsuarioId usuarioId = UsuarioId.from(command.id());

        Usuario existing = findPort.findById(usuarioId)
                .orElseThrow(() -> UsuarioNotFoundException.forId(command.id()));

        String keycloakId = command.keycloakId() != null
                ? command.keycloakId()
                : existing.getKeycloakId();

        // Sync email to Keycloak first — if this fails, do not update local
        if (!command.correo().equals(existing.getCorreo()) && keycloakId != null) {
            syncEmailPort.syncEmail(keycloakId, command.correo());
        }

        // Sync enabled flag to Keycloak
        if (keycloakId != null) {
            setEnabledPort.setEnabled(keycloakId, existing.isActivo());
        }

        Usuario updated = Usuario.reconstitute(
                existing.getId(),
                command.nombre(),
                command.correo(),
                existing.getRolId(),
                existing.getCreadoEn(),
                existing.isActivo(),
                keycloakId
        );

        return savePort.save(updated);
    }
}