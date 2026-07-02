package com.ar.crm2.application.ai.service;

import com.ar.crm2.application.ai.command.RegistrarAccionCommand;
import com.ar.crm2.application.ai.port.in.RegistrarAccionUseCase;
import com.ar.crm2.application.ai.port.out.SaveAiAccionPort;
import com.ar.crm2.model.entity.ia.AiAccion;
import com.ar.crm2.model.vo.AiConversacionId;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.model.vo.UsuarioId;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/**
 * Application service that stages AI-suggested CRM action proposals in
 * PENDING state.
 *
 * <p><b>Safety boundary:</b> this service NEVER invokes real CRM mutation
 * use cases (CreateContactoUseCase, CreateTratoUseCase,
 * CreateTareaUseCase, MoverColumnaFichaUseCase). It only builds a
 * domain entity and persists it via the outbound port. Only
 * {@code ConfirmarAccionUseCase} is allowed to call real
 * mutation use cases, and only after the original requester confirms.
 */
@RequiredArgsConstructor
public class RegistrarAccionService implements RegistrarAccionUseCase {

    private final SaveAiAccionPort savePort;

    /**
     * Stages the proposal via the domain factory, then persists it.
     *
     * <p>The factory enforces all domain invariants (payload opacity,
     * version=1, PENDING state, future expiry, audit-link
     * {@code aiConversacionId}). This service does not
     * re-validate domain rules.
     */
    @Override
    public AiAccion registrar(RegistrarAccionCommand command) {
        AiAccion accion = AiAccion.crear(
                EmpresaId.from(command.empresaId()),
                UsuarioId.from(command.actorUsuarioId()),
                command.waConversacionId(),
                command.waMensajeId(),
                AiConversacionId.from(command.aiConversacionId()),
                command.tipoAccion(),
                command.payloadJson(),
                command.rationale(),
                command.ttlMinutos(),
                LocalDateTime.now()
        );

        return savePort.save(accion);
    }
}