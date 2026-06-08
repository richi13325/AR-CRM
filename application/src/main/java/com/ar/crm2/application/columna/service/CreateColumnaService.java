package com.ar.crm2.application.columna.service;

import com.ar.crm2.application.columna.command.CreateColumnaCommand;
import com.ar.crm2.application.columna.port.in.CreateColumnaUseCase;
import com.ar.crm2.application.columna.port.out.FindAllColumnasPort;
import com.ar.crm2.application.columna.port.out.SaveColumnaPort;
import com.ar.crm2.model.entity.Columna;
import com.ar.crm2.model.enums.TipoColumna;
import com.ar.crm2.model.vo.ColumnaId;
import com.ar.crm2.model.vo.SuperUsuarioId;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

/**
 * Application service implementing CreateColumnaUseCase.
 * Orchestrates domain entity creation and outbound persistence via SaveColumnaPort.
 * No Spring annotations — constructor injection via Lombok.
 */
@RequiredArgsConstructor
public class CreateColumnaService implements CreateColumnaUseCase {

    private final SaveColumnaPort savePort;
    private final FindAllColumnasPort findAllPort;

    @Override
    public Columna create(CreateColumnaCommand command) {
        boolean existeDuplicado = ColumnaNamePolicy.hasDuplicateForCreate(
            findAllPort.findAll(),
            command.tipoTablero(),
            command.nombre()
        );

        Columna columna = Columna.create(
            command.superUsuarioId()
                .map(SuperUsuarioId::from)
                .orElse(null),
            command.nombre(),
            command.tipoTablero(),
            command.tipoColumna(),
            command.color(),
            existeDuplicado
        );

        return savePort.save(columna);
    }
}
