package com.ar.crm2.application.ai.service;

import com.ar.crm2.application.ai.command.ObtenerAccionCommand;
import com.ar.crm2.application.ai.port.in.ObtenerAccionUseCase;
import com.ar.crm2.application.ai.port.out.FindAiAccionPort;
import com.ar.crm2.application.empresa.port.in.ActorEmpresaScopePort;
import com.ar.crm2.exception.AccionNotFoundException;
import com.ar.crm2.model.entity.ia.AiAccion;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.model.vo.UsuarioId;
import lombok.RequiredArgsConstructor;

/**
 * Application service that returns a single AI action proposal scoped
 * to the requester and their tenant.
 *
 * <p>All ownership / tenant decisions live on the {@link AiAccion}
 * aggregate via {@code requireOwnedBy(...)}. The application service
 * coordinates the tenant scope resolution and the proposal lookup.
 *
 * <p><b>Tenant resolution (architecture correction):</b> this service
 * depends on the Empresa-owned {@link ActorEmpresaScopePort} port,
 * not on a class named adapter or on an AI-specific resolver
 * service. The neutral domain exception raised by the port is
 * translated to the AI-public one via
 * {@link AiTenantExceptionTranslator} at the call site.
 */
@RequiredArgsConstructor
public class ObtenerAccionService implements ObtenerAccionUseCase {

    private final ActorEmpresaScopePort actorEmpresaScopePort;
    private final FindAiAccionPort findAiAccionPort;

    @Override
    public AiAccion obtener(ObtenerAccionCommand command) {
        EmpresaId empresaId = AiTenantExceptionTranslator.resolve(
                actorEmpresaScopePort, command.actorUsuarioId(), command.empresaId()
        );
        UsuarioId actor = UsuarioId.from(command.actorUsuarioId());

        AiAccion accion = findAiAccionPort.findById(command.accionId())
                .orElseThrow(() -> AccionNotFoundException.forId(command.accionId()));

        accion.requireOwnedBy(actor, empresaId);
        return accion;
    }
}