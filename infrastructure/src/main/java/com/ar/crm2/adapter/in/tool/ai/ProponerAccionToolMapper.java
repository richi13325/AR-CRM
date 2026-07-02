package com.ar.crm2.adapter.in.tool.ai;

import com.ar.crm2.adapter.in.tool.ai.dto.ProponerAccionRequest;
import com.ar.crm2.adapter.in.tool.ai.dto.ProponerAccionResponse;
import com.ar.crm2.application.ai.command.ProponerAccionCommand;
import com.ar.crm2.model.enums.TipoAccion;

/**
 * Mapper that translates between the infrastructure tool wire DTOs
 * and the application-owned command / response.
 *
 * <p>The tool adapter calls into this mapper instead of building
 * {@code ProponerAccionCommand} or {@link ProponerAccionResponse}
 * inline. This follows the same boundary pattern as
 * {@code AgendaCommandMapper}: the tool stays a thin input adapter;
 * mapping lives in a dedicated, testable class.
 *
 * <p>The mapper is stateless and side-effect free. Trusted scope
 * fields (actor / tenant / conv ids) are NOT mapped here — they
 * are resolved inside {@code ProponerAccionUseCase} through
 * {@code AiToolContextPort} to preserve the safety boundary.
 * The mapper only translates the model-supplied proposal shape.
 */
public final class ProponerAccionToolMapper {

    private ProponerAccionToolMapper() {
    }

    /**
     * Builds the inbound use case command from the model-supplied
     * request. The TTL is sourced from the request DTO so the model
     * can choose how long the proposal stays pending; trusted scope
     * is intentionally absent because the use case resolves it.
     *
     * @param request the model-supplied proposal shape
     * @return a fully populated application command, ready to dispatch
     */
    public static ProponerAccionCommand toCommand(ProponerAccionRequest request) {
        return new ProponerAccionCommand(
            TipoAccion.valueOf(request.tipoAccion()),
            request.payloadJson(),
            request.rationale(),
            request.ttlMinutos()
        );
    }

    /**
     * Builds the tool response the model reads back from the use case
     * result. Pass-through — the use case already projects id +
     * estado; the mapper just returns the wire shape.
     */
    public static ProponerAccionResponse toResponse(
        com.ar.crm2.application.ai.port.in.result.ProponerAccionResponse result
    ) {
        if (result == null) {
            throw new IllegalStateException(
                "ProponerAccionResponse is required to build a tool response"
            );
        }
        return new ProponerAccionResponse(result.accionId(), result.estado());
    }
}
