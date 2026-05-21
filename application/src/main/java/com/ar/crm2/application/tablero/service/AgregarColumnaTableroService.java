package com.ar.crm2.application.tablero.service;

import com.ar.crm2.application.tablero.command.AgregarColumnaTableroCommand;
import com.ar.crm2.application.tablero.exception.TableroNotFoundException;
import com.ar.crm2.application.tablero.port.in.AgregarColumnaTableroUseCase;
import com.ar.crm2.application.tablero.port.out.FindTableroByIdPort;
import com.ar.crm2.application.tablero.port.out.SaveTableroPort;
import com.ar.crm2.model.entity.Columna;
import com.ar.crm2.model.entity.ColumnaTablero;
import com.ar.crm2.model.entity.Tablero;
import com.ar.crm2.model.vo.ColumnaId;
import com.ar.crm2.model.vo.SuperUsuarioId;
import com.ar.crm2.model.vo.TableroId;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

/**
 * Application service implementing AgregarColumnaTableroUseCase.
 * Loads the Tablero aggregate, builds the ColumnaTablero contextual wrapper,
 * invokes domain behavior, and persists the updated aggregate.
 */
@RequiredArgsConstructor
public class AgregarColumnaTableroService implements AgregarColumnaTableroUseCase {

    private final FindTableroByIdPort findPort;
    private final SaveTableroPort savePort;

    @Override
    public Tablero agregarColumna(AgregarColumnaTableroCommand command) {
        TableroId tableroId = TableroId.from(command.tableroId());

        Tablero existing = findPort.findById(tableroId)
                .orElseThrow(() -> TableroNotFoundException.forId(command.tableroId()));

        // Build the Columna domain entity (catalog column, board-independent)
        Columna columna = Columna.create(
                SuperUsuarioId.create(),
                command.nombre(),
                existing.getTipoTablero(),
                command.tipoColumna(),
                command.color(),
                command.existeOtraColumnaConMismoNombre()
        );

        // Build the contextual ColumnaTablero wrapper using catalog column identity
        ColumnaTablero columnaTablero = ColumnaTablero.create(
                columna.getId(),
                existing.getTipoTablero(),
                command.limiteWip(),
                command.nota(),
                command.estadoTarea(),
                command.estadoTrato(),
                command.totalValorEstimado()
        );

        // Delegate to domain aggregate
        Tablero updated = existing.agregarColumnaTablero(columnaTablero);

        return savePort.save(updated);
    }
}