package com.ar.crm2.adapter.in.tool.ai;

import com.ar.crm2.adapter.in.tool.ai.dto.ProponerAccionRequest;
import com.ar.crm2.adapter.in.tool.ai.dto.ProponerAccionResponse;
import com.ar.crm2.application.ai.command.ProponerAccionCommand;
import com.ar.crm2.application.ai.port.in.ProponerAccionUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;

/**
 * {@code @Tool}-annotated input adapter that stages AI-suggested CRM
 * mutations as action proposals. The model invokes this tool when the
 * assistant decides the user wants to create or move something — the
 * tool then forwards the proposal to the application staging use case
 * and returns the staged {@code AiAccion} id + status to the model.
 *
 * <p><b>Thin input adapter:</b> this class depends ONLY on
 * {@link ProponerAccionUseCase} (the inbound use case that owns the
 * tool-context resolution and the staging delegation). It does NOT
 * coordinate multiple dependencies inline — that is the use case's
 * job, mirroring the {@code TableroController.asignarColumna ->
 * AsignarColumnaTableroUseCase} pattern.
 *
 * <p><b>Safety boundary (D1 in design.md):</b> the use case never
 * invokes real CRM mutation use cases; the constructor and field set
 * are guarded by
 * {@code ProponerAccionToolTest#constructor_doesNotInjectRealMutationUseCases}.
 * The model supplies only the proposal shape — actor / tenant / conv
 * scope is resolved inside the use case from
 * {@code AiToolContextPort}.
 *
 * <p><b>Mapping:</b> the tool delegates all DTO ↔ command / response
 * translation to {@link ProponerAccionToolMapper}. The tool class
 * itself never assembles {@code ProponerAccionCommand} or
 * {@code ProponerAccionResponse} inline — that would violate the
 * adapter style and obscure the boundary. The wire-shape DTOs
 * ({@link ProponerAccionRequest}, {@link ProponerAccionResponse})
 * live in their own package so the tool class stays a thin facade.
 *
 * <p><b>AI tool surface architecture (Slice 14 decision):</b>
 * this tool is the ONLY AI tool that depends on an inbound use case
 * ({@link ProponerAccionUseCase}), because staging proposals has
 * business logic to coordinate (resolve trusted context, build the
 * staging command, call {@code RegistrarAccionUseCase}, return the
 * staged id + estado).
 *
 * <p>The other 4 read-only tools are NOT structured the same way:
 * <ul>
 *   <li>{@code BuscarClientePorTelefonoTool},</li>
 *   <li>{@code ListarColumnasTableroTool},</li>
 *   <li>{@code ObtenerMensajesRecientesTool},</li>
 *   <li>{@code ObtenerResumenChatTool}</li>
 * </ul>
 * depend directly on <b>AI-bounded-context outbound read ports</b>
 * ({@code ContactoLecturaPort}, {@code ColumnaLecturaPort},
 * {@code WhatsappMensajeLecturaPort}, {@code FindAiResumenPort})
 * — plus the trusted {@code AiToolContextPort} where tenant scope is
 * needed. Read tools have no business logic to wrap, so routing them
 * through an inbound use case would be unnecessary indirection.
 *
 * <p>NO AI tool — including this one — depends on a real CRM
 * mutation use case. That invariant is the AI tool safety boundary
 * and is pinned by
 * {@code AiToolArchitectureContractTest} (8 reflection-based cases)
 * plus the per-tool
 * {@code constructor_doesNotInjectRealMutationUseCases} and
 * {@code @Tool} description tests.
 */
@Slf4j
@RequiredArgsConstructor
public class ProponerAccionTool {

    private final ProponerAccionUseCase proponerAccionUseCase;

    /**
     * Stages a CRM action proposal in PENDING state.
     *
     * <p>The model invokes this method when the user asks for a
     * sensitive CRM mutation (create contacto/trato/tarea, move a
     * ficha). The returned {@link ProponerAccionResponse} is what the
     * model sees in its tool-call output — it carries the staged
     * {@code accionId} and {@code estado=PENDING} so the assistant
     * can tell the user the proposal is awaiting confirmation.
     *
     * @param request the model-supplied proposal shape
     * @return the staged proposal summary
     */
    @Tool(
        name = "proponerAccion",
        description = "Stage a CRM action proposal (PENDING) for the user to confirm. "
            + "Use ONLY for sensitive mutations: CREATE_CONTACTO, CREATE_TRATO, "
            + "CREATE_TAREA, MOVE_KANBAN_FICHA. The proposal stays PENDING until the "
            + "user confirms it via the front-end; this tool never mutates real CRM entities."
    )
    public ProponerAccionResponse proponerAccion(ProponerAccionRequest request) {
        ProponerAccionCommand command = ProponerAccionToolMapper.toCommand(request);
        var result = proponerAccionUseCase.proponer(command);
        log.info(
            "AI tool staged proposal: accionId={} estado={}",
            result.accionId(), result.estado()
        );
        return ProponerAccionToolMapper.toResponse(result);
    }
}
