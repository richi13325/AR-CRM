package com.ar.crm2.adapter.in.tool.ai.dto;

import com.ar.crm2.model.enums.EstadoRelacion;

/**
 * Infrastructure DTO returned by the {@code buscarClientePorTelefono}
 * AI tool.
 *
 * <p>When the lookup misses, every field except {@code encontrado}
 * is null — the {@code encontrado} flag lets the model reason about
 * a miss without inspecting nulls (which it tends to confuse with
 * "unknown"). The {@code empresaId} mirrors the trusted tenant so
 * the model can show the user which company the match belongs to.
 */
public record BuscarClientePorTelefonoResponse(
    boolean encontrado,
    String id,
    String nombre,
    String telefono,
    String correo,
    EstadoRelacion estadoRelacion,
    String empresaId
) {

    public static BuscarClientePorTelefonoResponse miss() {
        return new BuscarClientePorTelefonoResponse(
            false, null, null, null, null, null, null);
    }

    public static BuscarClientePorTelefonoResponse hit(
            String id, String nombre, String telefono, String correo,
            EstadoRelacion estadoRelacion, String empresaId) {
        return new BuscarClientePorTelefonoResponse(
            true, id, nombre, telefono, correo, estadoRelacion, empresaId);
    }
}