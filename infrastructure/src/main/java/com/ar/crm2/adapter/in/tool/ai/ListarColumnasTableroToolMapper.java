package com.ar.crm2.adapter.in.tool.ai;

import com.ar.crm2.adapter.in.tool.ai.dto.ListarColumnasTableroResponse;
import com.ar.crm2.model.entity.Columna;

import java.util.List;

/**
 * Mapper that translates between the domain {@link Columna}
 * catalog entries and the {@code ListarColumnasTableroResponse}
 * wire DTO.
 *
 * <p>Stateless, side-effect free, no business validation.
 */
public final class ListarColumnasTableroToolMapper {

    private ListarColumnasTableroToolMapper() {}

    public static List<ListarColumnasTableroResponse> toResponseList(
            List<Columna> columnas) {
        if (columnas == null || columnas.isEmpty()) {
            return List.of();
        }
        return columnas.stream()
            .map(ListarColumnasTableroToolMapper::toResponse)
            .toList();
    }

    private static ListarColumnasTableroResponse toResponse(Columna c) {
        return new ListarColumnasTableroResponse(
            c.getId().value().toString(),
            c.getColumnanombre(),
            c.getColor(),
            c.getTipoTablero().name(),
            c.getTipoColumna().name()
        );
    }
}