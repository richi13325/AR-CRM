package com.ar.crm2.application.tablero.service;

import com.ar.crm2.application.columna.command.CreateColumnaCommand;
import com.ar.crm2.application.columna.port.in.CreateColumnaUseCase;
import com.ar.crm2.application.columna.port.out.FindAllColumnasPort;
import com.ar.crm2.application.columna.service.ColumnaNamePolicy;
import com.ar.crm2.application.tablero.command.CreateTableroCommand;
import com.ar.crm2.application.tablero.port.out.SaveTableroPort;
import com.ar.crm2.application.tablero.port.in.CreateTableroUseCase;
import com.ar.crm2.model.entity.Columna;
import com.ar.crm2.model.entity.ColumnaTablero;
import com.ar.crm2.model.entity.Tablero;
import com.ar.crm2.model.enums.TipoColumna;
import com.ar.crm2.model.enums.TipoTablero;
import com.ar.crm2.model.vo.SuperUsuarioId;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Application service implementing CreateTableroUseCase.
 * Orchestrates domain entity creation and outbound persistence via SaveTableroPort.
 * No Spring annotations — constructor injection via Lombok.
 */
@RequiredArgsConstructor
public class CreateTableroService implements CreateTableroUseCase {

    private final SaveTableroPort savePort;
    private final FindAllColumnasPort findAllColumnasPort;
    private final CreateColumnaUseCase createColumnaUseCase;

    @Override
    public Tablero create(CreateTableroCommand command) {
        // Catalog Columna rows must be persisted BEFORE saving the Tablero.
        // ColumnaTablero holds a ColumnaId reference; the read path in
        // TableroMapper.toColumnaTableroDomain() re-hydrates the catalog Columna
        // for each child. Saving the Tablero first would create dangling
        // columna_id references in columnas_tablero and crash on read.
        List<ColumnaTablero> columnasPredeterminadas = buildDefaultColumns(command);

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
     *   <li>TAREAS: Pendiente, En Curso, Finalizada, Cancelada</li>
     *   <li>TRATOS: Abierto, Ganado, Perdido, Archived</li>
     * </ul>
     *
     * @param command the board creation command
     * @return list of 4 default ColumnaTablero contextual wrappers
     */
    private List<ColumnaTablero> buildDefaultColumns(CreateTableroCommand command) {
        TipoTablero tipoTablero = command.tipoTablero();
        List<Columna> existingColumnas = findAllColumnasPort.findAll();
        List<ColumnaTablero> columnas = new ArrayList<>(4);

        switch (tipoTablero) {
            case TAREAS -> {
                columnas.add(makeColumnaTablero(existingColumnas, command, "Pendiente", 5));
                columnas.add(makeColumnaTablero(existingColumnas, command, "En Curso", 3));
                columnas.add(makeColumnaTablero(existingColumnas, command, "Finalizada", 5));
                columnas.add(makeColumnaTablero(existingColumnas, command, "Cancelada", 5));
            }
            case TRATOS -> {
                columnas.add(makeColumnaTablero(existingColumnas, command, "Abierto", 10));
                columnas.add(makeColumnaTablero(existingColumnas, command, "Ganado", 10));
                columnas.add(makeColumnaTablero(existingColumnas, command, "Perdido", 10));
                columnas.add(makeColumnaTablero(existingColumnas, command, "Archived", 10));
            }
        }

        return columnas;
    }

    private ColumnaTablero makeColumnaTablero(
        List<Columna> existingColumnas,
        CreateTableroCommand command,
        String nombre,
        int limiteWip
    ) {
        TipoTablero tipoTablero = command.tipoTablero();
        Columna persisted = resolveDefaultCatalogColumn(existingColumnas, command, nombre, tipoTablero);

        return ColumnaTablero.create(
            persisted.getId(),
            tipoTablero,
            limiteWip,
            null,
            BigDecimal.ZERO
        );
    }

    private Columna resolveDefaultCatalogColumn(
        List<Columna> existingColumnas,
        CreateTableroCommand command,
        String nombre,
        TipoTablero tipoTablero
    ) {
        Optional<Columna> existing = ColumnaNamePolicy.findDefaultCatalogColumn(existingColumnas, tipoTablero, nombre);
        if (existing.isPresent()) {
            return existing.get();
        }

        return createColumnaUseCase.create(new CreateColumnaCommand(
            Optional.of(command.superUsuarioId()),
            nombre,
            "#FFFFFF",
            tipoTablero,
            TipoColumna.PREDETERMINADA
        ));
    }
}
