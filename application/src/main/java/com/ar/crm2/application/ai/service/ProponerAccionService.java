package com.ar.crm2.application.ai.service;

import com.ar.crm2.application.ai.command.ProponerAccionCommand;
import com.ar.crm2.application.ai.command.RegistrarAccionCommand;
import com.ar.crm2.application.ai.port.in.ProponerAccionUseCase;
import com.ar.crm2.application.ai.port.in.RegistrarAccionUseCase;
import com.ar.crm2.application.ai.port.in.result.ProponerAccionResponse;
import com.ar.crm2.application.ai.port.out.AiToolContextPort;
import com.ar.crm2.application.ai.port.out.dto.AiToolContext;
import com.ar.crm2.model.entity.ia.AiAccion;
import lombok.RequiredArgsConstructor;

/**
 * Application service that coordinates the staging of an AI-suggested
 * CRM action proposal in PENDING state from a tool invocation.
 *
 * <p>This service is the only place where the trusted tool context
 * resolution and the staging use case are stitched together. The
 * Spring AI {@code ProponerAccionTool} input adapter is a thin facade
 * that delegates to {@link #proponer(ProponerAccionCommand)}; it
 * MUST NOT coordinate the resolver and the staging use case inline.
 *
 * <p>Flow:
 * <ol>
 *   <li>Resolve the trusted scope via {@link AiToolContextPort}
 *       — the model payload carries no actor / tenant / conv
 *       identity.</li>
 *   <li>Build a {@link RegistrarAccionCommand} that stitches the
 *       trusted scope with the model-supplied proposal shape.</li>
 *   <li>Delegate to the existing {@link RegistrarAccionUseCase} so
 *       the staging path stays in one place (the factory + save
 *       port pairing lives there).</li>
 *   <li>Project the staged {@code AiAccion} into a
 *       {@link ProponerAccionResponse}.</li>
 * </ol>
 *
 * <p><b>Safety boundary:</b> this service depends ONLY on
 * {@link AiToolContextPort} and {@link RegistrarAccionUseCase}.
 * It MUST NOT depend on real CRM mutation use cases — the only
 * mechanism that can execute the proposal is
 * {@code ConfirmarAccionUseCase}, invoked by the same requester via
 * REST / UI.
 */
@RequiredArgsConstructor
public class ProponerAccionService implements ProponerAccionUseCase {

    private final AiToolContextPort contextResolver;
    private final RegistrarAccionUseCase registrarAccionUseCase;

    @Override
    public ProponerAccionResponse proponer(ProponerAccionCommand command) {
        // 1. Trusted scope comes from the resolver (ThreadLocal / MDC in
        //    production), NEVER from the model payload.
        AiToolContext ctx = contextResolver.resolve();

        // 2. Stitch the trusted scope with the model-supplied shape into
        //    the staging command. waMensajeId is left null because the
        //    model payload does not include it and the tool cannot
        //    spoof it.
        RegistrarAccionCommand staging = new RegistrarAccionCommand(
            ctx.actorUsuarioId(),
            ctx.empresaId(),
            ctx.aiConversacionId(),
            ctx.waConversacionId(),
            null,
            command.tipo().name(),
            command.payloadJson(),
            command.rationale(),
            command.ttlMinutos()
        );

        // 3. Delegate to the existing staging use case. The factory +
        //    save pairing is reused; the safety boundary is preserved
        //    by NOT depending on real mutation use cases.
        AiAccion staged = registrarAccionUseCase.registrar(staging);

        // 4. Project the staged aggregate into the result.
        return new ProponerAccionResponse(
            staged.getId().value().toString(),
            staged.getEstado().name()
        );
    }
}
