package com.ar.crm2.application.ai.service;

import com.ar.crm2.application.ai.command.ListarAccionesPendientesCommand;
import com.ar.crm2.application.ai.exception.AsistenteTenantException;
import com.ar.crm2.application.ai.port.in.ListarAccionesPendientesUseCase;
import com.ar.crm2.application.ai.port.out.ListPendingAiAccionesPort;
import com.ar.crm2.application.empresa.port.in.ActorEmpresaScopePort;
import com.ar.crm2.model.entity.ia.AiAccion;
import com.ar.crm2.model.vo.EmpresaId;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Application service that lists the requester's PENDING AI action
 * proposals.
 *
 * <p><b>User-approved PR7 contract:</b> this service DOES NOT
 * auto-resolve a single owned company. The {@code empresaId} selector
 * is REQUIRED at the command boundary (the DTO + canonical
 * constructor both reject null). When the supplied {@code empresaId}
 * is not owned by the actor — or the actor owns no companies at all —
 * the service raises {@link AsistenteTenantException} via
 * {@link AiTenantExceptionTranslator#resolveForSelector}, which the
 * {@code GlobalExceptionHandler} maps to HTTP 403.
 *
 * <p>PENDING-only filtering is enforced at the port boundary (the
 * Spring Data query uses {@code estado = PENDING}).
 *
 * <p><b>Tenant resolution (architecture correction):</b> this service
 * depends on the Empresa-owned port, not on an AI-specific resolver.
 * The neutral domain exception raised by the port is translated to
 * the AI-public one via {@link AiTenantExceptionTranslator} at the
 * call site.
 */
@RequiredArgsConstructor
public class ListarAccionesPendientesService implements ListarAccionesPendientesUseCase {

    private final ActorEmpresaScopePort actorEmpresaScopePort;
    private final ListPendingAiAccionesPort listPendingAiAccionesPort;

    @Override
    public List<AiAccion> listar(ListarAccionesPendientesCommand command) {
        EmpresaId empresaId = AiTenantExceptionTranslator.resolveForSelector(
                actorEmpresaScopePort, command.actorUsuarioId(), command.empresaId()
        );
        return listPendingAiAccionesPort.listPendingByActor(
                command.actorUsuarioId(), empresaId.value(), command.limite()
        );
    }
}
