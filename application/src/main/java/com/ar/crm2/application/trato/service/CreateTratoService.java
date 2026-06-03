package com.ar.crm2.application.trato.service;

import com.ar.crm2.application.ficha.port.out.SaveFichaPort;
import com.ar.crm2.application.tablero.port.out.FindInitialColumnPort;
import com.ar.crm2.application.trato.command.CreateTratoCommand;
import com.ar.crm2.application.trato.port.out.SaveTratoPort;
import com.ar.crm2.application.trato.port.in.CreateTratoUseCase;
import com.ar.crm2.model.entity.Columna;
import com.ar.crm2.model.entity.Ficha;
import com.ar.crm2.model.entity.Trato;
import com.ar.crm2.model.enums.TipoFicha;
import com.ar.crm2.model.enums.TipoTablero;
import com.ar.crm2.model.vo.ContactoId;
import com.ar.crm2.model.vo.UsuarioId;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateTratoService implements CreateTratoUseCase {

    private final SaveTratoPort savePort;
    private final SaveFichaPort saveFichaPort;
    private final FindInitialColumnPort findInitialColumnPort;

    @Override
    public Trato create(CreateTratoCommand command) {
        Trato trato = Trato.create(
            ContactoId.from(command.contactoId()),
            UsuarioId.from(command.responsableId()),
            command.nombre(),
            command.valorEstimado(),
            command.probabilidad(),
            command.fechaCierreEsperada(),
            command.tipoContrato()
        );
        Trato savedTrato = savePort.save(trato);

        Columna columnaInicial = findInitialColumnPort.findInitialColumn(TipoTablero.TRATOS)
            .orElseThrow(() -> new IllegalStateException(
                "No initial column found for global TRATOS board. Cannot create Trato without Kanban Ficha."
            ));

        Ficha ficha = Ficha.create(
            columnaInicial.getId(),
            TipoFicha.TRATO,
            savedTrato.getId(),
            null
        );
        saveFichaPort.save(ficha);

        return savedTrato;
    }
}
