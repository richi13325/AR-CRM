package com.ar.crm2.adapter.in.tool.ai;

import com.ar.crm2.adapter.in.tool.ai.dto.ObtenerMensajesRecientesRequest;
import com.ar.crm2.adapter.in.tool.ai.dto.ObtenerMensajesRecientesResponse;
import com.ar.crm2.application.ai.port.out.projection.WhatsappMensajeResumen;

import java.util.List;

/**
 * Mapper that translates between the infrastructure tool wire DTOs
 * and the application-owned read port projection.
 *
 * <p>The tool adapter delegates all DTO ↔ projection translation to
 * this mapper so the tool class itself stays a thin facade. Mirrors
 * the {@code ProponerAccionToolMapper} pattern.
 */
public final class ObtenerMensajesRecientesToolMapper {

    private ObtenerMensajesRecientesToolMapper() {}

    /**
     * Projects an ordered list of port projections into the wire DTO
     * list the model reads. Order is preserved (chronologically
     * ascending per the port contract).
     */
    public static List<ObtenerMensajesRecientesResponse> toResponseList(
            List<WhatsappMensajeResumen> messages) {
        if (messages == null) {
            return List.of();
        }
        return messages.stream()
            .map(m -> new ObtenerMensajesRecientesResponse(
                m.waMensajeId().toString(),
                m.direccion(),
                m.tipo(),
                m.contenido(),
                m.mediaUrl(),
                m.creadoEn()
            ))
            .toList();
    }

    /**
     * Computes the sublist index range that yields the most recent N
     * messages from a chronologically-ascending list. Returning a
     * defensive copy keeps the mapper side-effect free.
     *
     * @param messages chronologically-ascending list from the port
     * @param limit    model-supplied cap (validated by the request DTO)
     * @return the last {@code limit} messages in original order
     */
    public static List<WhatsappMensajeResumen> takeLastN(
            List<WhatsappMensajeResumen> messages, int limit) {
        if (messages == null || messages.isEmpty() || limit <= 0) {
            return List.of();
        }
        int from = Math.max(0, messages.size() - limit);
        return List.copyOf(messages.subList(from, messages.size()));
    }

    /**
     * Reads the cap from the model-supplied request DTO. Kept here
     * for symmetry with {@link #toResponseList(List)} — the tool
     * calls one mapper, not both helpers inline.
     */
    public static int limitOf(ObtenerMensajesRecientesRequest request) {
        return request.limit();
    }
}