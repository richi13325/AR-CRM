package com.ar.crm2.application.superusuario.service;

import com.ar.crm2.application.identity.port.out.SetIdentityEnabledPort;
import com.ar.crm2.application.identity.port.out.SyncIdentityEmailPort;
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
 * and saving. Preserves: id, creadoEn, activo.
 * Syncs email and enabled state to Keycloak before local update.
 * No Spring annotations — constructor injection via Lombok.
 */
@RequiredArgsConstructor
public class EditSuperUsuarioService implements EditSuperUsuarioUseCase {

    private final FindSuperUsuarioByIdPort findPort;
    private final SaveSuperUsuarioPort savePort;
    private final SyncIdentityEmailPort syncEmailPort;
    private final SetIdentityEnabledPort setEnabledPort;

    @Override
    public SuperUsuario edit(EditSuperUsuarioCommand command) {
        SuperUsuarioId superUsuarioId = SuperUsuarioId.from(command.id());

        SuperUsuario existing = findPort.findById(superUsuarioId)
                .orElseThrow(() -> SuperUsuarioNotFoundException.forId(command.id()));

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

        SuperUsuario updated = SuperUsuario.reconstitute(
                existing.getId(),
                command.correo(),
                existing.getCreadoEn(),
                existing.isActivo(),
                keycloakId
        );

        return savePort.save(updated);
    }
}