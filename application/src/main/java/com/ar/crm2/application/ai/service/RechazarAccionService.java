package com.ar.crm2.application.ai.service;

import com.ar.crm2.application.ai.command.RechazarAccionCommand;
import com.ar.crm2.application.ai.exception.AsistenteTenantException;
import com.ar.crm2.application.ai.port.in.RechazarAccionUseCase;
import com.ar.crm2.application.ai.port.out.FindAiAccionPort;
import com.ar.crm2.application.empresa.port.in.ActorEmpresaScopePort;
import com.ar.crm2.exception.AccionNotFoundException;
import com.ar.crm2.model.entity.ia.AiAccion;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.model.vo.UsuarioId;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/**
 * Application service that rejects a pending AI action proposal.
 *
 * <p>Side-effect free at the CRM layer — only flips proposal state.
 * All ownership / tenant / state decisions live on the
 * {@link AiAccion} aggregate ({@code requireOwnedBy(...)} +
 * {@code requirePending(...)}). The application service coordinates
 * the port calls and the persistence write.
 *
 * <p><b>PR6 — resource-first tenant resolution:</b> the tenant is
 * derived from the addressed {@link AiAccion} resource — NOT from the
 * request body's {@code empresaId} parameter. The flow is:
 * <ol>
 *   <li>Load the action by id ({@link #findAiAccionPort}).</li>
 *   <li><b>Cross-check</b>: if the command carries an {@code empresaId}
 *       and it differs from the resource's stored {@code empresaId},
 *       reject with
 *       {@link AsistenteTenantException#accionNoPerteneceALaEmpresaSeleccionada(String, String)}
 *       (controlled 403 — "This action does not belong to the selected
 *       company").</li>
 *   <li><b>Authorization</b>: validate the actor owns the resource
 *       tenant via the Empresa-owned {@link ActorEmpresaScopePort} port
 *       (translated to {@link AsistenteTenantException} at the call
 *       site via {@link AiTenantExceptionTranslator#assertActorOwnsTenant}).</li>
 *   <li>Delegate the rest of the lifecycle decisions to the aggregate.</li>
 * </ol>
 *
 * <p><b>Tenant resolution (architecture correction):</b> this service
 * depends on the Empresa-owned {@link ActorEmpresaScopePort} port,
 * not on a class named adapter or on an AI-specific resolver
 * service. The neutral domain exception raised by the port is
 * translated to the AI-public one via
 * {@link AiTenantExceptionTranslator} at the call site.
 */
@RequiredArgsConstructor
public class RechazarAccionService implements RechazarAccionUseCase {

    private final ActorEmpresaScopePort actorEmpresaScopePort;
    private final FindAiAccionPort findAiAccionPort;
    private final SaveAiAccionPortBridge savePort;

    @Override
    public AiAccion rechazar(RechazarAccionCommand command) {
        LocalDateTime ahora = LocalDateTime.now();

        // PR6 step 1: load the resource — it is the source of truth for tenant authority.
        AiAccion accion = findAiAccionPort.findById(command.accionId())
                .orElseThrow(() -> AccionNotFoundException.forId(command.accionId()));

        EmpresaId recursoEmpresaId = accion.getEmpresaId();

        // PR6 step 2: strict cross-check between the request's empresaId and the resource's.
        // The command constructor guarantees empresaId != null, so the
        // cross-check is ALWAYS evaluated — there is no skip path when
        // the request omits the company.
        if (!recursoEmpresaId.value().equals(command.empresaId())) {
            throw AsistenteTenantException.accionNoPerteneceALaEmpresaSeleccionada(
                    command.accionId().toString(), command.empresaId().toString()
            );
        }

        // PR6 step 3: validate the actor owns the resource tenant.
        AiTenantExceptionTranslator.assertActorOwnsTenant(
                actorEmpresaScopePort, command.actorUsuarioId(), recursoEmpresaId
        );

        UsuarioId actor = UsuarioId.from(command.actorUsuarioId());

        accion.requireOwnedBy(actor, recursoEmpresaId);
        accion.requirePending("rechazar");

        return savePort.save(accion.rechazar(ahora));
    }
}