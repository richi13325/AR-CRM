package com.ar.crm2.adapter.in.rest.dto.response;

import com.ar.crm2.adapter.out.persistence.entity.ColumnaEntity;
import com.ar.crm2.adapter.out.persistence.mapper.ColumnaMapper;
import com.ar.crm2.adapter.out.persistence.repository.ColumnaRepository;
import com.ar.crm2.model.entity.Columna;
import com.ar.crm2.model.entity.Tablero;
import com.ar.crm2.model.enums.TipoTablero;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * REST response DTO for Tablero.
 * Exposes all fields needed for front-end board views, including columns in order.
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
     * Maps a domain Tablero to this response DTO.
     *
     * <p>Requires {@link ColumnaRepository} to hydrate catalog column data (nombre, color)
     * since the domain model no longer carries full {@link com.ar.crm2.model.entity.Columna}
     * objects — only {@code columnaId} references.
     *
     * @param tablero          the domain Tablero aggregate
     * @param columnaRepository the repository for resolving catalog Columna data
     */
    public static TableroResponse fromDomain(Tablero tablero, ColumnaRepository columnaRepository) {
        List<ColumnaTableroDto> columnasDto = tablero.getColumnasTablero().stream()
            .map(ct -> {
                Optional<ColumnaEntity> columnaEntity = columnaRepository.findById(ct.getColumnaId().value().toString());
                Columna columna = columnaEntity.map(ColumnaMapper::toDomain).orElse(null);
                return ColumnaTableroDto.fromDomain(ct, columna);
            })
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