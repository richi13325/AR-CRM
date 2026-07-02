package com.ar.crm2.application.ai.port.in.result;

import com.ar.crm2.model.enums.EstadoAccion;

/**
 * Result of the {@code ProponerAccionUseCase}.
 *
 * <p>Carries the staged proposal id and the resulting estado. The
 * tool adapter (Spring AI {@code @Tool} input adapter) projects this
 * result into the wire response DTO so the model can reference the
 * proposal in its user-facing message and (later) via
 * {@code ConfirmarAccionUseCase}.
 *
 * <p>Staging only — the proposal stays in {@link EstadoAccion#PENDING}
 * until the same requester confirms it through the REST / UI flow.
 *
 * @param accionId  the staged {@code AiAccion} id
 * @param estado    the resulting estado (always {@code PENDING} on
 *                  successful staging; the use case never returns any
 *                  other value)
 */
public record ProponerAccionResponse(
    String accionId,
    String estado
) {
}
