package com.ar.crm2.adapter.in.rest.dto.response;

import com.ar.crm2.model.entity.Columna;
import com.ar.crm2.model.entity.Tablero;
import com.ar.crm2.model.enums.TipoTablero;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST response DTO for Tablero.
 * Exposes all fields needed for front-end board views, including columns in order.
 *
 * <p><strong>Pure DTO mapping contract:</strong> this record performs value
 * mapping only. It does NOT call any repository. The pure mapping overload
 * {@link #fromDomain(Tablero, Map)} takes a pre-resolved column lookup map
 * (typically produced by the infrastructure assembler
 * {@code TableroResponseAssembler}), keeping the DTO decoupled from the
 * persistence layer.
 */
public record TableroResponse(
    UUID id,
    String nombre,
    String descripcion,
    TipoTablero tipoTablero,
    List<ColumnaTableroDto> columnas,
    LocalDateTime creadoEn
) {
    /**
     * Pure DTO mapping. Takes a pre-resolved map of
     * {@code ColumnaId → Columna} (typically built by the infrastructure
     * assembler) and produces a {@link TableroResponse} without performing
     * any repository access.
     *
     * @param tablero         the domain Tablero aggregate
     * @param resolvedColumnas a map keyed by {@link com.ar.crm2.model.vo.ColumnaId}
     *                         (UUID string form) carrying the catalog Columna
     *                         for each assigned column. Entries may be absent
     *                         for unresolved ids; the resulting DTO carries
     *                         {@code null} nombre/color for those slots.
     * @return a fully-mapped TableroResponse
     */
    public static TableroResponse fromDomain(Tablero tablero, Map<String, Columna> resolvedColumnas) {
        List<ColumnaTableroDto> columnasDto = tablero.getColumnasTablero().stream()
            .map(ct -> ColumnaTableroDto.fromDomain(ct, resolvedColumnas.get(ct.getColumnaId().value().toString())))
            .toList();

        return new TableroResponse(
            tablero.getId().value(),
            tablero.getNombre(),
            tablero.getDescripcion(),
            tablero.getTipoTablero(),
            columnasDto,
            tablero.getCreadoEn()
        );
    }
}
