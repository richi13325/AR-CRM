package com.ar.crm2.adapter.in.rest.mapper;

import com.ar.crm2.adapter.in.rest.dto.response.ColumnaTableroDto;
import com.ar.crm2.adapter.in.rest.dto.response.TableroResponse;
import com.ar.crm2.adapter.out.persistence.mapper.ColumnaMapper;
import com.ar.crm2.adapter.out.persistence.repository.ColumnaRepository;
import com.ar.crm2.model.entity.Columna;
import com.ar.crm2.model.entity.ColumnaTablero;
import com.ar.crm2.model.entity.Tablero;

import java.util.List;
import java.util.Optional;

/**
 * Infrastructure assembler responsible for hydrating catalog {@link Columna}
 * data and building {@link TableroResponse} DTOs.
 *
 * <p>This is the new home of repository-backed response hydration. The
 * controller and the DTO no longer reach into the {@link ColumnaRepository}
 * directly. The assembler encapsulates the catalog lookup, the persistence
 * entity → domain mapping, and the per-column DTO assembly, and exposes a
 * single {@link #assemble(Tablero)} method.
 *
 * <p><strong>Why this class exists:</strong> the previous implementation
 * performed the hydration inside {@code TableroResponse.fromDomain(Tablero,
 * ColumnaRepository)}, which coupled the DTO to the repository and to the
 * persistence {@code ColumnaEntity}/{@code ColumnaMapper}. The assembler
 * moves that coupling out of the DTO/controller layer and into a dedicated
 * infrastructure mapper.
 *
 * <p><strong>Layered architecture contract:</strong>
 * <ul>
 *   <li>Domain: {@link Tablero}, {@link Columna}, {@link ColumnaTablero} — no repository imports.</li>
 *   <li>Application: services coordinate ports; no infrastructure imports.</li>
 *   <li>Infrastructure: this assembler is the only place where catalog
 *       repository access happens for Tablero response building.</li>
 *   <li>REST DTOs: pure value mapping; no repository access.</li>
 * </ul>
 *
 * <p><strong>Wiring:</strong> this class is exposed as an explicit Spring
 * bean from boot wiring and injected into {@code TableroController}. That
 * keeps the controller free of repository dependencies while preserving
 * repository-backed hydration inside the infrastructure layer.
 */
public class TableroResponseAssembler {

    private final ColumnaRepository columnaRepository;

    public TableroResponseAssembler(ColumnaRepository columnaRepository) {
        this.columnaRepository = columnaRepository;
    }

    /**
     * Hydrates the catalog column data for the given Tablero and produces a
     * {@link TableroResponse} DTO.
     *
     * <p>For each {@link ColumnaTablero} entry in the aggregate, the
     * assembler resolves the catalog {@link Columna} (if present) and
     * pairs it with the contextual board data (WIP, note, total) to build
     * a {@link ColumnaTableroDto}.
     *
     * @param tablero the domain Tablero aggregate to assemble into a response
     * @return a fully-hydrated TableroResponse
     */
    public TableroResponse assemble(Tablero tablero) {
        List<ColumnaTableroDto> columnasDto = tablero.getColumnasTablero().stream()
            .map(this::toColumnaTableroDto)
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

    private ColumnaTableroDto toColumnaTableroDto(ColumnaTablero ct) {
        Optional<Columna> columna = columnaRepository
            .findById(ct.getColumnaId().value().toString())
            .map(ColumnaMapper::toDomain);
        return ColumnaTableroDto.fromDomain(ct, columna.orElse(null));
    }
}
