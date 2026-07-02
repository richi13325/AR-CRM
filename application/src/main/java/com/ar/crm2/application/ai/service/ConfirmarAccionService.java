package com.ar.crm2.application.ai.service;

import com.ar.crm2.application.ai.command.ConfirmarAccionCommand;
import com.ar.crm2.application.ai.exception.AiAssistantException;
import com.ar.crm2.application.ai.exception.AsistenteTenantException;
import com.ar.crm2.application.ai.port.in.ConfirmarAccionUseCase;
import com.ar.crm2.application.ai.port.in.result.ResultadoEjecucionAccion;
import com.ar.crm2.application.ai.port.out.FindAiAccionPort;
import com.ar.crm2.application.contacto.command.CreateContactoCommand;
import com.ar.crm2.application.contacto.port.in.CreateContactoUseCase;
import com.ar.crm2.application.empresa.port.in.ActorEmpresaScopePort;
import com.ar.crm2.application.ficha.command.MoverColumnaFichaCommand;
import com.ar.crm2.application.ficha.port.in.MoverColumnaFichaUseCase;
import com.ar.crm2.application.tarea.command.CreateTareaCommand;
import com.ar.crm2.application.tarea.port.in.CreateTareaUseCase;
import com.ar.crm2.application.trato.command.CreateTratoCommand;
import com.ar.crm2.application.trato.port.in.CreateTratoUseCase;
import com.ar.crm2.exception.AccionExpiredException;
import com.ar.crm2.exception.AccionNotFoundException;
import com.ar.crm2.model.entity.ia.AiAccion;
import com.ar.crm2.model.enums.TipoAccion;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.model.vo.UsuarioId;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/**
 * Application service that confirms a pending AI action proposal.
 *
 * <p><b>Safety boundary:</b> this is the only AI service allowed to
 * invoke real CRM mutation use cases. The constructor accepts the
 * four mutation use cases by interface type — the type-level signature
 * makes the safety boundary explicit (no other AI service may take
 * them as dependencies).
 *
 * <p><b>Coordination only:</b> all ownership / tenant / state /
 * version / expiry decisions live on the {@link AiAccion} aggregate
 * (see {@code requireOwnedBy(...)} and {@code requireConfirmable(...)}).
 * The application service delegates each decision to the entity and
 * handles only the cross-cutting concerns: port coordination,
 * mutation dispatch, EXECUTED/FAILED lifecycle bookkeeping, and the
 * "expired → mark + re-throw" traceability side-effect.
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
 * service. The boot module wires the Empresa service as the port
 * implementation. The neutral domain
 * {@link com.ar.crm2.exception.TenantScopeViolationException} raised
 * by the port is translated to the AI-public
 * {@link com.ar.crm2.application.ai.exception.AsistenteTenantException}
 * via {@link AiTenantExceptionTranslator} at the call site.
 */
@RequiredArgsConstructor
public class ConfirmarAccionService implements ConfirmarAccionUseCase {

    private final ActorEmpresaScopePort actorEmpresaScopePort;
    private final FindAiAccionPort findAiAccionPort;
    private final SaveAiAccionPortBridge savePort;

    private final CreateContactoUseCase createContactoUseCase;
    private final CreateTratoUseCase createTratoUseCase;
    private final CreateTareaUseCase createTareaUseCase;
    private final MoverColumnaFichaUseCase moverColumnaFichaUseCase;

    @Override
    public ResultadoEjecucionAccion confirmar(ConfirmarAccionCommand command) {
        LocalDateTime ahora = LocalDateTime.now();

        // PR6 step 1: load the resource — it is the source of truth for tenant authority.
        AiAccion accion = findAiAccionPort.findById(command.accionId())
                .orElseThrow(() -> AccionNotFoundException.forId(command.accionId()));

        EmpresaId recursoEmpresaId = accion.getEmpresaId();

        // PR6 step 2: strict cross-check between the request's empresaId and the resource's.
        // The command constructor guarantees empresaId != null, so the
        // cross-check is ALWAYS evaluated — there is no skip path when
        // the request omits the company. Reject with a controlled
        // business exception (distinct from the aggregate ownership
        // exception) — never let it explode as a generic AI / runtime error.
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

        // All pre-flight policy decisions are owned by the aggregate.
        accion.requireOwnedBy(actor, recursoEmpresaId);
        try {
            accion.requireConfirmable(command.expectedVersion(), ahora);
        } catch (AccionExpiredException ex) {
            // The aggregate signals expiry but stays PENDING in memory.
            // Persist the EXPIRED transition here (application owns the
            // port) so the lifecycle is observable, then re-throw.
            savePort.save(accion.expirar(ahora));
            throw ex;
        }

        AiAccion confirmada = accion.confirmar(ahora);
        MutationDispatch mutationDispatch = prepareMutationDispatch(confirmada);

        // Dispatch to the real mutation use case, mark EXECUTED or FAILED.
        try {
            String resultadoId = mutationDispatch.execute();
            AiAccion ejecutada = confirmada.marcarEjecutada(resultadoId, ahora);
            AiAccion guardada = savePort.save(ejecutada);
            return ResultadoEjecucionAccion.ejecutada(resultadoId, guardada.getVersion());
        } catch (RuntimeException ex) {
            AiAccion fallida = confirmada.marcarFallida(truncate(ex.getMessage(), 1000), ahora);
            AiAccion guardada = savePort.save(fallida);
            return ResultadoEjecucionAccion.fallida(guardada.getErrorReason(), guardada.getVersion());
        }
    }

    /**
     * Dispatches to the real mutation use case matching
     * {@code confirmada.getTipoAccion()} and returns the resulting
     * entity id.
     */
    private MutationDispatch prepareMutationDispatch(AiAccion confirmada) {
        TipoAccion tipo;
        try {
            tipo = TipoAccion.valueOf(confirmada.getTipoAccion());
        } catch (IllegalArgumentException ex) {
            throw AiAssistantException.invalidAssistantOutput(
                    "tipoAccion desconocido: " + confirmada.getTipoAccion()
            );
        }

        return switch (tipo) {
            case CREATE_CONTACTO -> {
                CreateContactoCommand cmd = ConfirmarAccionMapper.toCreateContacto(confirmada);
                yield () -> createContactoUseCase.create(cmd).getId().value().toString();
            }
            case CREATE_TRATO -> {
                CreateTratoCommand cmd = ConfirmarAccionMapper.toCreateTrato(confirmada);
                yield () -> createTratoUseCase.create(cmd).getId().value().toString();
            }
            case CREATE_TAREA -> {
                CreateTareaCommand cmd = ConfirmarAccionMapper.toCreateTarea(confirmada);
                yield () -> createTareaUseCase.create(cmd).getId().value().toString();
            }
            case MOVE_KANBAN_FICHA -> {
                MoverColumnaFichaCommand cmd = ConfirmarAccionMapper.toMoverFicha(confirmada);
                yield () -> moverColumnaFichaUseCase.moverAColumna(cmd).getId().value().toString();
            }
        };
    }

    @FunctionalInterface
    private interface MutationDispatch {
        String execute();
    }

    private static String truncate(String s, int max) {
        if (s == null) return "unknown";
        return s.length() > max ? s.substring(0, max) : s;
    }
}
