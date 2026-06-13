package com.ar.crm2.application.usuario.service;

import com.ar.crm2.application.identity.port.out.DeleteIdentityPort;
import com.ar.crm2.application.identity.port.out.ProvisionIdentityPort;
import com.ar.crm2.application.identity.port.out.SetIdentityAttributesPort;
import com.ar.crm2.application.usuario.command.CreateUsuarioCommand;
import com.ar.crm2.application.usuario.port.in.CreateUsuarioUseCase;
import com.ar.crm2.application.usuario.port.out.SaveUsuarioPort;
import com.ar.crm2.model.entity.Usuario;
import com.ar.crm2.model.vo.RolId;
import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * Application service implementing CreateUsuarioUseCase.
 * <p>
 * Consistency model: Keycloak is provisioned first, then the locally
 * generated {@code usuario_id} is pushed to Keycloak as a custom user
 * attribute (consumed by the configured protocol mapper to emit the
 * {@code usuario_id} JWT claim) BEFORE the local row is persisted. This keeps
 * Keycloak and the local DB in lockstep: if either external step fails, the
 * whole flow is rolled back via compensation deletes, and the caller observes
 * a single, attributable failure.
 * <p>
 * Flow:
 * <ol>
 *   <li>Provision the Keycloak user (fails fast on connection / auth issues).</li>
 *   <li>Build the local {@link Usuario} with the provisioned {@code keycloakId}.
 *       {@code Usuario.create(...)} generates the {@code usuario_id} now, so
 *       it is available for the next step.</li>
 *   <li>Push {@code usuario_id} to Keycloak via
 *       {@link SetIdentityAttributesPort#setAttributes(String, Map)}.
 *       If this fails, the Keycloak user is deleted as compensation, the
 *       local row is never saved, and the original failure is rethrown.</li>
 *   <li>Persist the local row. If this fails after the attribute sync, the
 *       Keycloak user is deleted as compensation and the original save
 *       failure is rethrown — leaving a Keycloak user without a CRM
 *       counterpart would leave the JWT {@code usuario_id} claim dangling.</li>
 * </ol>
 * Compensation failures (Keycloak delete itself failing) are swallowed and
 * do not mask the original error.
 * <p>
 * No Spring annotations — constructor injection via Lombok.
 */
@RequiredArgsConstructor
public class CreateUsuarioService implements CreateUsuarioUseCase {

    private final SaveUsuarioPort savePort;
    private final ProvisionIdentityPort provisionPort;
    private final DeleteIdentityPort deleteIdentityPort;
    private final SetIdentityAttributesPort setAttributesPort;

    @Override
    public Usuario create(CreateUsuarioCommand command) {
        // Provision Keycloak first — we need keycloakId before we can build the
        // local Usuario entity, and a Keycloak failure must short-circuit
        // before any local work happens.
        var provisioned = provisionPort.provision(
            command.correo(),
            command.initialPassword(),
            true // enabled
        );

        // Build the local entity up front so the generated usuario_id is
        // available for the Keycloak attribute sync that follows.
        Usuario usuario = Usuario.create(
            command.nombre(),
            command.correo(),
            RolId.from(command.rolId()),
            provisioned.keycloakId()
        );

        // Sync usuario_id to Keycloak BEFORE the local save. If this fails,
        // the Keycloak user is rolled back (it has no CRM counterpart) and
        // the original failure is surfaced to the caller.
        try {
            setAttributesPort.setAttributes(
                provisioned.keycloakId(),
                Map.of("usuario_id", usuario.getId().value().toString())
            );
        } catch (RuntimeException syncFailure) {
            // Compensate: delete the Keycloak user — it has no CRM counterpart
            // and would otherwise emit a stale usuario_id claim on next login.
            try {
                deleteIdentityPort.delete(provisioned.keycloakId());
            } catch (RuntimeException compensationFailure) {
                // Log but don't mask the original sync failure.
                // The Keycloak user may remain orphaned — operator must review.
            }
            throw syncFailure;
        }

        // Persist locally. If this fails after the attribute sync, the
        // Keycloak user is rolled back for the same reason as above: leaving
        // a Keycloak user with no CRM link would leak a stale usuario_id
        // claim on subsequent logins.
        Usuario saved;
        try {
            saved = savePort.save(usuario);
        } catch (RuntimeException saveFailure) {
            // Compensate: delete the Keycloak user.
            try {
                deleteIdentityPort.delete(provisioned.keycloakId());
            } catch (RuntimeException compensationFailure) {
                // Log but don't mask the original save failure.
                // The Keycloak user may remain orphaned — operator must review.
            }
            throw saveFailure;
        }

        return saved;
    }
}
