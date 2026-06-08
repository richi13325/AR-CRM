package com.ar.crm2.application.columna.service;

import com.ar.crm2.application.columna.command.CreateColumnaCommand;
import com.ar.crm2.application.columna.port.out.FindAllColumnasPort;
import com.ar.crm2.application.columna.port.out.SaveColumnaPort;
import com.ar.crm2.exception.NombreColumnaYaExisteException;
import com.ar.crm2.model.entity.Columna;
import com.ar.crm2.model.enums.TipoColumna;
import com.ar.crm2.model.enums.TipoTablero;
import com.ar.crm2.model.vo.SuperUsuarioId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreateColumnaServiceTest {

    @Test
    void create_rejectsTrimmedDuplicateNameWithinSameBoardType() {
        Columna existing = Columna.create(
            SuperUsuarioId.create(),
            "Pendiente",
            TipoTablero.TAREAS,
            TipoColumna.PREDETERMINADA,
            "#FFFFFF",
            false
        );

        CreateColumnaService service = new CreateColumnaService(passThroughSavePort(), fixedCatalog(existing));

        assertThrows(
            NombreColumnaYaExisteException.class,
            () -> service.create(command("  Pendiente  ", TipoTablero.TAREAS))
        );
    }

    @Test
    void create_allowsCaseVariantBecauseNormalizationIsTrimOnly() {
        Columna existing = Columna.create(
            SuperUsuarioId.create(),
            "Pendiente",
            TipoTablero.TAREAS,
            TipoColumna.PREDETERMINADA,
            "#FFFFFF",
            false
        );

        CreateColumnaService service = new CreateColumnaService(passThroughSavePort(), fixedCatalog(existing));

        Columna created = service.create(command("pendiente", TipoTablero.TAREAS));

        assertEquals("pendiente", created.getColumnanombre());
    }

    @Test
    void create_allowsSameNameForDifferentBoardType() {
        Columna existing = Columna.create(
            SuperUsuarioId.create(),
            "Pendiente",
            TipoTablero.TAREAS,
            TipoColumna.PREDETERMINADA,
            "#FFFFFF",
            false
        );

        CreateColumnaService service = new CreateColumnaService(passThroughSavePort(), fixedCatalog(existing));

        Columna created = service.create(command("Pendiente", TipoTablero.TRATOS));

        assertEquals(TipoTablero.TRATOS, created.getTipoTablero());
        assertEquals("Pendiente", created.getColumnanombre());
    }

    private static CreateColumnaCommand command(String nombre, TipoTablero tipoTablero) {
        return new CreateColumnaCommand(
            Optional.of(UUID.randomUUID()),
            nombre,
            "#FFFFFF",
            tipoTablero,
            TipoColumna.PREDETERMINADA
        );
    }

    private static SaveColumnaPort passThroughSavePort() {
        return columna -> columna;
    }

    private static FindAllColumnasPort fixedCatalog(Columna... columnas) {
        return () -> List.of(columnas);
    }
}
