package com.ar.crm2.application.ai.service;

import com.ar.crm2.application.ai.command.ListarConversacionesAsistenteCommand;
import com.ar.crm2.application.ai.port.in.ListarConversacionesAsistenteUseCase;
import com.ar.crm2.application.ai.port.out.ListAiConversacionesPort;
import com.ar.crm2.application.empresa.port.in.ActorEmpresaScopePort;
import com.ar.crm2.model.entity.ia.AiConversacion;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Application service that lists AI conversations scoped to the
 * requester.
 *
 * <p><b>Tenant resolution (architecture correction):</b> this service
 * depends on the Empresa-owned {@link ActorEmpresaScopePort} port,
 * not on a class named adapter or on an AI-specific resolver
 * service. The neutral domain exception raised by the port is
 * translated to the AI-public one via
 * {@link AiTenantExceptionTranslator} at the call site.
 */
@RequiredArgsConstructor
public class ListarConversacionesAsistenteService implements ListarConversacionesAsistenteUseCase {

    private final ActorEmpresaScopePort actorEmpresaScopePort;
    private final ListAiConversacionesPort listAiConversacionesPort;

    @Override
    public List<AiConversacion> listar(ListarConversacionesAsistenteCommand command) {
        var empresaId = AiTenantExceptionTranslator.resolve(
                actorEmpresaScopePort, command.actorUsuarioId(), command.empresaId()
        );
        return listAiConversacionesPort.listByActor(
                command.actorUsuarioId(), empresaId.value(), command.limite()
        );
    }
}