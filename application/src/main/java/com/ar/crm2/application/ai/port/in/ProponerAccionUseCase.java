package com.ar.crm2.application.ai.port.in;

import com.ar.crm2.application.ai.command.ProponerAccionCommand;
import com.ar.crm2.application.ai.port.in.result.ProponerAccionResponse;

/**
 * Inbound input port for staging an AI-suggested CRM action proposal
 * in PENDING state from a tool invocation.
 *
 * <p>Mirrors the {@code TableroController.asignarColumna ->
 * AsignarColumnaTableroUseCase} pattern: the tool adapter is a thin
 * input adapter that delegates the staging to ONE use case. The use
 * case is the single boundary that:
 * <ul>
 *   <li>Resolves the trusted actor / tenant / conv ids through
 *       {@code AiToolContextPort} (outbound).</li>
 *   <li>Builds the staging {@code RegistrarAccionCommand} and calls
 *       the existing {@code RegistrarAccionUseCase} to persist a
 *       {@code PENDING} {@code AiAccion}.</li>
 *   <li>Returns a {@link ProponerAccionResponse} so the tool can
 *       project the staged id + estado into the wire response DTO.</li>
 * </ul>
 *
 * <p><b>Safety boundary:</b> this use case is the ONLY entry point
 * the AI tool path is allowed to use. Real CRM mutation use cases
 * (CreateContacto / CreateTrato / CreateTarea / MoverColumnaFicha)
 * are NOT dependencies of this contract — the only way to execute
 * a proposal is through {@code ConfirmarAccionUseCase}, invoked by
 * the same requester through the REST / UI flow.
 */
public interface ProponerAccionUseCase {

    /**
     * Stages a new AI action proposal in PENDING state.
     *
     * @param command the tool-supplied proposal shape
     * @return the staged proposal id and resulting estado
     */
    ProponerAccionResponse proponer(ProponerAccionCommand command);
}
