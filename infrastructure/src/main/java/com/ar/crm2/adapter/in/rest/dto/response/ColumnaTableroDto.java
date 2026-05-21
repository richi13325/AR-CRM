package com.ar.crm2.adapter.in.rest.dto.response;

import com.ar.crm2.model.entity.ColumnaTablero;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * REST response DTO for ColumnaTablero (column in board context).
 * Carries both the column catalog data and board-specific contextual state.
 */
public record ColumnaTableroDto(
    UUID id,
    String nombre,
    String color,
    Integer limiteWip,
    String nota,
    String estadoTarea,
    String estadoTrato,
    BigDecimal totalValorEstimado
) {
    /**
     * Maps a domain ColumnaTablero to this response DTO.
     *
     * <p>Note: In the catalog model, ColumnaTablero no longer wraps a full
     * {@link com.ar.crm2.model.entity.Columna}. This overload accepts the
     * catalog Columna separately so the response can still expose column
     * definition data (nombre, color) for API consumers.
     *
     * @param ct      the board-column contextual wrapper
     * @param columna the catalog Columna (hydrated by the mapper from the repository)
     */
    public static ColumnaTableroDto fromDomain(ColumnaTablero ct, com.ar.crm2.model.entity.Columna columna) {
        return new ColumnaTableroDto(
            ct.getColumnaId().value(),
            columna != null ? columna.getColumnanombre() : null,
            columna != null ? columna.getColor() : null,
            ct.getLimiteWip(),
            ct.getNota(),
            ct.getEstadoTarea() != null ? ct.getEstadoTarea().name() : null,
            ct.getEstadoTrato() != null ? ct.getEstadoTrato().name() : null,
            ct.getTotalValorEstimado()
        );
    }
}