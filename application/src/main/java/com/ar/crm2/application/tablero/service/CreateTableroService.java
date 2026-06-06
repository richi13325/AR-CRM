package com.ar.crm2.application.tablero.service;

import com.ar.crm2.application.columna.port.out.SaveColumnaPort;
import com.ar.crm2.application.tablero.command.CreateTableroCommand;
import com.ar.crm2.application.tablero.port.out.SaveTableroPort;
import com.ar.crm2.application.tablero.port.in.CreateTableroUseCase;
import com.ar.crm2.model.entity.Columna;
import com.ar.crm2.model.entity.ColumnaTablero;
import com.ar.crm2.model.entity.Tablero;
import com.ar.crm2.model.enums.TipoColumna;
import com.ar.crm2.model.enums.TipoEstadoColumnaTableroTarea;
import com.ar.crm2.model.enums.TipoEstadoColumnaTableroTrato;
import com.ar.crm2.model.enums.TipoTablero;
import com.ar.crm2.model.vo.SuperUsuarioId;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Application service implementing CreateTableroUseCase.
 * Orchestrates domain entity creation and outbound persistence via SaveTableroPort.
 * No Spring annotations — constructor injection via Lombok.
 */
@RequiredArgsConstructor
public class CreateTableroService implements CreateTableroUseCase {

    private final SaveTableroPort savePort;
    private final SaveColumnaPort saveColumnaPort;

    @Override
    public Tablero create(CreateTableroCommand command) {
        // Catalog Columna rows must be persisted BEFORE saving the Tablero.
        // ColumnaTablero holds a ColumnaId reference; the read path in
        // TableroMapper.toColumnaTableroDomain() re-hydrates the catalog Columna
        // for each child. Saving the Tablero first would create dangling
        // columna_id references in columnas_tablero and crash on read.
        List<ColumnaTablero> columnasPredeterminadas = buildDefaultColumns(command.tipoTablero());

        Tablero tablero = Tablero.create(
            command.nombre(),
            command.descripcion(),
            command.tipoTablero(),
            columnasPredeterminadas,
            SuperUsuarioId.from(command.superUsuarioId())
        );

        return savePort.save(tablero);
    }

    /**
     * Builds exactly 4 default PREDETERMINADA columns for the given Tablero type.
     * Each column is a catalog entry board-independent; the ColumnaTablero
     * contextualizes it within the board context.
     *
     * <p>Default columns:
     * <ul>
     *   <li>TAREAS: PENDIENTE, EN_CURSO, FINALIZADA, CANCELADA</li>
     *   <li>TRATOS: ABIERTO, GANADO, PERDIDO, ARCHIVED</li>
     * </ul>
     *
     * @param tipoTablero the type of the board
     * @return list of 4 default ColumnaTablero contextual wrappers
     */
    private List<ColumnaTablero> buildDefaultColumns(TipoTablero tipoTablero) {
        SuperUsuarioId superUsuarioId = SuperUsuarioId.create();

        List<ColumnaTablero> columnas = new ArrayList<>(4);

        switch (tipoTablero) {
            case TAREAS -> {
                columnas.add(makeColumnaTablero(superUsuarioId,
                    "Pendiente", TipoEstadoColumnaTableroTarea.PENDIENTE, null, 5));
                columnas.add(makeColumnaTablero(superUsuarioId,
                    "En Curso", TipoEstadoColumnaTableroTarea.EN_CURSO, null, 3));
                columnas.add(makeColumnaTablero(superUsuarioId,
                    "Finalizada", TipoEstadoColumnaTableroTarea.FINALIZADA, null, 5));
                columnas.add(makeColumnaTablero(superUsuarioId,
                    "Cancelada", TipoEstadoColumnaTableroTarea.PENDIENTE, null, 5));
            }
            case TRATOS -> {
                columnas.add(makeColumnaTableroTrato(superUsuarioId,
                    "Abierto", TipoEstadoColumnaTableroTrato.ABIERTO, BigDecimal.ZERO, 10));
                columnas.add(makeColumnaTableroTrato(superUsuarioId,
                    "Ganado", TipoEstadoColumnaTableroTrato.GANADO, BigDecimal.ZERO, 10));
                columnas.add(makeColumnaTableroTrato(superUsuarioId,
                    "Perdido", TipoEstadoColumnaTableroTrato.PERDIDO, BigDecimal.ZERO, 10));
                columnas.add(makeColumnaTableroTrato(superUsuarioId,
                    "Archived", TipoEstadoColumnaTableroTrato.PERDIDO, BigDecimal.ZERO, 10));
            }
        }

        return columnas;
    }

    private ColumnaTablero makeColumnaTablero(
        SuperUsuarioId superUsuarioId,
        String nombre,
        TipoEstadoColumnaTableroTarea estadoTarea,
        TipoEstadoColumnaTableroTrato estadoTrato,
        int limiteWip
    ) {
        Columna columna = Columna.create(
            superUsuarioId,
            nombre,
            TipoTablero.TAREAS,
            TipoColumna.PREDETERMINADA,
            "#FFFFFF",
            false
        );

        // Persist the catalog Columna first so columnas_tablero.columna_id
        // references a row that already exists. TableroMapper re-hydrates
        // the catalog on read and would throw IllegalStateException otherwise.
        Columna persisted = saveColumnaPort.save(columna);

        return ColumnaTablero.create(
            persisted.getId(),
            TipoTablero.TAREAS,
            limiteWip,
            null,
            estadoTarea,
            null,
            BigDecimal.ZERO
        );
    }

    private ColumnaTablero makeColumnaTableroTrato(
        SuperUsuarioId superUsuarioId,
        String nombre,
        TipoEstadoColumnaTableroTrato estadoTrato,
        BigDecimal totalValorEstimado,
        int limiteWip
    ) {
        Columna columna = Columna.create(
            superUsuarioId,
            nombre,
            TipoTablero.TRATOS,
            TipoColumna.PREDETERMINADA,
            "#FFFFFF",
            false
        );

        // Persist the catalog Columna first so columnas_tablero.columna_id
        // references a row that already exists. TableroMapper re-hydrates
        // the catalog on read and would throw IllegalStateException otherwise.
        Columna persisted = saveColumnaPort.save(columna);

        return ColumnaTablero.create(
            persisted.getId(),
            TipoTablero.TRATOS,
            limiteWip,
            null,
            null,
            estadoTrato,
            totalValorEstimado
        );
    }
}