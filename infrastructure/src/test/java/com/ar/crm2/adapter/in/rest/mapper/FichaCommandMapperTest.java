package com.ar.crm2.adapter.in.rest.mapper;

import com.ar.crm2.adapter.in.rest.dto.request.CreateFichaRequest;
import com.ar.crm2.adapter.in.rest.dto.request.EditFichaRequest;
import com.ar.crm2.application.ficha.command.CreateFichaCommand;
import com.ar.crm2.application.ficha.command.EditFichaCommand;
import com.ar.crm2.model.enums.TipoFicha;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FichaCommandMapper (etiquetaIds threading after slice 3).
 * No mocks — the mapper is a static utility.
 */
class FichaCommandMapperTest {

    @Test
    void toCommand_withNullEtiquetaIds_shouldThreadNull() {
        CreateFichaRequest request = new CreateFichaRequest(
            UUID.randomUUID(),
            TipoFicha.TAREA,
            null,
            UUID.randomUUID(),
            null
        );

        CreateFichaCommand cmd = FichaCommandMapper.toCommand(request, null);

        assertNull(cmd.etiquetaIds());
    }

    @Test
    void toCommand_withEmptyEtiquetaIds_shouldThreadEmptyList() {
        CreateFichaRequest request = new CreateFichaRequest(
            UUID.randomUUID(),
            TipoFicha.TAREA,
            null,
            UUID.randomUUID(),
            List.of()
        );

        CreateFichaCommand cmd = FichaCommandMapper.toCommand(request, null);

        assertNotNull(cmd.etiquetaIds());
        assertTrue(cmd.etiquetaIds().isEmpty());
    }

    @Test
    void toCommand_withEtiquetaIds_shouldThreadAsList() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        CreateFichaRequest request = new CreateFichaRequest(
            UUID.randomUUID(),
            TipoFicha.TAREA,
            null,
            UUID.randomUUID(),
            List.of(id1, id2)
        );

        CreateFichaCommand cmd = FichaCommandMapper.toCommand(request, null);

        assertNotNull(cmd.etiquetaIds());
        assertEquals(2, cmd.etiquetaIds().size());
        assertEquals(id1, cmd.etiquetaIds().get(0));
        assertEquals(id2, cmd.etiquetaIds().get(1));
    }

    @Test
    void toEditCommand_withEtiquetaIds_shouldThreadAsList() {
        UUID id = UUID.randomUUID();
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        EditFichaRequest request = new EditFichaRequest(
            UUID.randomUUID(),
            TipoFicha.TAREA,
            null,
            UUID.randomUUID(),
            List.of(id1, id2)
        );

        EditFichaCommand cmd = FichaCommandMapper.toCommand(id, request);

        assertEquals(id, cmd.id());
        assertNotNull(cmd.etiquetaIds());
        assertEquals(2, cmd.etiquetaIds().size());
        assertEquals(id1, cmd.etiquetaIds().get(0));
        assertEquals(id2, cmd.etiquetaIds().get(1));
    }
}
