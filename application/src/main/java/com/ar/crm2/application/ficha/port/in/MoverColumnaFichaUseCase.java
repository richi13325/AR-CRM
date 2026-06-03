package com.ar.crm2.application.ficha.port.in;

import com.ar.crm2.application.ficha.command.MoverColumnaFichaCommand;
import com.ar.crm2.model.entity.Ficha;

public interface MoverColumnaFichaUseCase {

    /**
     * Moves a Ficha to a different Columna.
     *
     * @param command holds the fichaId and targetColumnaId
     * @return the updated domain entity
     */
    Ficha moverAColumna(MoverColumnaFichaCommand command);
}
