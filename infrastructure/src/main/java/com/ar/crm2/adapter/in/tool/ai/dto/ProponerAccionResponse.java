package com.ar.crm2.adapter.in.tool.ai.dto;

/**
 * Infrastructure DTO returned by the {@code proponerAccion} AI tool.
 *
 * <p>Spring AI serializes the result to a JSON string that the model
 * reads as the tool-call output. Keeping the shape minimal (id + estado)
 * avoids leaking the full {@code AiAccion} aggregate into the model
 * context.
 *
 * <p>Lives outside the tool class (in its own package) so the wire
 * shape is co-located with the request DTO. The tool's
 * {@code ProponerAccionToolMapper} builds this record from the staged
 * {@code AiAccion} aggregate so the tool class itself never assembles
 * wire shapes inline.
 */
public record ProponerAccionResponse(
    String accionId,
    String estado
) {
}