package com.ar.crm2.adapter.in.tool.ai.dto;

import org.springframework.ai.tool.annotation.ToolParam;

/**
 * Infrastructure DTO passed by the model to the
 * {@code buscarClientePorTelefono} AI tool.
 *
 * <p>The tool only takes the phone number — the tenant
 * ({@code empresaId}) is resolved from the trusted
 * {@code AiToolContext} so the model cannot probe a foreign
 * tenant.
 */
public record BuscarClientePorTelefonoRequest(
    @ToolParam(description = "Phone number in E.164 format, e.g. +5491155555555. "
        + "Used to look up an existing Contacto. The lookup is scoped to the "
        + "trusted tenant — a foreign tenant cannot be probed.",
        required = true)
    String telefono
) {

    public BuscarClientePorTelefonoRequest {
        if (telefono == null || telefono.isBlank()) {
            throw new IllegalArgumentException("telefono is required");
        }
    }
}