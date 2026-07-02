package com.ar.crm2.application.ai.service;

import com.ar.crm2.application.ai.command.ObtenerConversacionAsistenteCommand;
import com.ar.crm2.application.ai.exception.AsistenteTenantException;
import com.ar.crm2.application.ai.exception.ConversacionAsistenteNoEncontradaException;
import com.ar.crm2.application.ai.port.in.ObtenerConversacionAsistenteUseCase;
import com.ar.crm2.application.ai.port.out.FindAiConversacionPort;
import com.ar.crm2.application.ai.port.out.FindAiMensajesByConversacionPort;
import com.ar.crm2.application.empresa.port.in.ActorEmpresaScopePort;
import com.ar.crm2.model.entity.ia.AiConversacion;
import com.ar.crm2.model.entity.ia.AiMensaje;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.model.vo.UsuarioId;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Application service that returns an AI conversation + its full
 * ordered message history, scoped to the requester.
 *
 * <p>All ownership / tenant decisions live on the {@link AiConversacion}
 * aggregate via {@code requireOwnedBy(...)}. The application service
 * coordinates the tenant scope resolution, conversation lookup, and
 * message fetch.
 *
 * <p><b>PR6 — resource-first tenant resolution:</b> the tenant is
 * derived from the addressed {@link AiConversacion} resource — NOT from
 * the request body's {@code empresaId} parameter. The flow is:
 * <ol>
 *   <li>Load the conversation by id ({@link #findAiConversacionPort}).</li>
 *   <li><b>Cross-check</b>: if the command carries an {@code empresaId}
 *       and it differs from the resource's stored {@code empresaId},
 *       reject with
 *       {@link AsistenteTenantException#conversacionNoPerteneceALaEmpresaSeleccionada(String, String)}
 *       (controlled 403 — "This conversation does not belong to the
 *       selected company").</li>
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
public class ObtenerConversacionAsistenteService implements ObtenerConversacionAsistenteUseCase {

    private final ActorEmpresaScopePort actorEmpresaScopePort;
    private final FindAiConversacionPort findAiConversacionPort;
    private final FindAiMensajesByConversacionPort findAiMensajesPort;

    @Override
    public ResultadoConversacionAsistente obtener(ObtenerConversacionAsistenteCommand command) {
        // PR6 step 1: load the resource — it is the source of truth for tenant authority.
        AiConversacion conversacion = findAiConversacionPort.findById(command.aiConversacionId())
                .orElseThrow(() -> ConversacionAsistenteNoEncontradaException.forId(
                        command.aiConversacionId().toString()
                ));

        EmpresaId recursoEmpresaId = conversacion.getEmpresaId();

        // PR6 step 2: strict cross-check between the request's empresaId and the resource's.
        // The command constructor guarantees empresaId != null, so the
        // cross-check is ALWAYS evaluated — there is no skip path when
        // the request omits the company.
        if (!recursoEmpresaId.value().equals(command.empresaId())) {
            throw AsistenteTenantException.conversacionNoPerteneceALaEmpresaSeleccionada(
                    command.aiConversacionId().toString(), command.empresaId().toString()
            );
        }

        // PR6 step 3: validate the actor owns the resource tenant.
        AiTenantExceptionTranslator.assertActorOwnsTenant(
                actorEmpresaScopePort, command.actorUsuarioId(), recursoEmpresaId
        );

        UsuarioId actor = UsuarioId.from(command.actorUsuarioId());

        conversacion.requireOwnedBy(actor, recursoEmpresaId);

        List<AiMensaje> mensajes = findAiMensajesPort.findByConversacionId(command.aiConversacionId());
        return new ResultadoConversacionAsistente(conversacion, mensajes);
    }
}