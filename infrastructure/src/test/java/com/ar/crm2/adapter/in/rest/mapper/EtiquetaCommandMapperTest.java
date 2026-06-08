package com.ar.crm2.adapter.in.rest.mapper;

import com.ar.crm2.adapter.in.rest.dto.request.CreateEtiquetaRequest;
import com.ar.crm2.adapter.in.rest.dto.request.EditEtiquetaRequest;
import com.ar.crm2.application.etiqueta.command.CreateEtiquetaCommand;
import com.ar.crm2.application.etiqueta.command.DeleteEtiquetaCommand;
import com.ar.crm2.application.etiqueta.command.EditEtiquetaCommand;
import com.ar.crm2.application.etiqueta.command.GetAllEtiquetasCommand;
import com.ar.crm2.application.etiqueta.command.GetEtiquetaByIdCommand;
import com.ar.crm2.model.enums.TipoEtiqueta;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure-function tests for EtiquetaCommandMapper.
 * No mocks needed — the mapper is a static utility.
 */
class EtiquetaCommandMapperTest {

    @Test
    void toCreateCommand_shouldMapAllFields() {
        CreateEtiquetaRequest request = new CreateEtiquetaRequest(
            "Urgent", TipoEtiqueta.TAREA, "#FF0000"
        );

        CreateEtiquetaCommand cmd = EtiquetaCommandMapper.toCreateCommand(request);

        assertEquals("Urgent", cmd.nombre());
        assertEquals(TipoEtiqueta.TAREA, cmd.tipoEtiqueta());
        assertEquals("#FF0000", cmd.color());
    }

    @Test
    void toEditCommand_shouldMapAllFields() {
        UUID id = UUID.randomUUID();
        EditEtiquetaRequest request = new EditEtiquetaRequest("Renamed", "#00FF00");

        EditEtiquetaCommand cmd = EtiquetaCommandMapper.toEditCommand(id, request);

        assertEquals(id, cmd.id());
        assertEquals("Renamed", cmd.nombre());
        assertEquals("#00FF00", cmd.color());
    }

    @Test
    void toGetByIdCommand_shouldMapId() {
        UUID id = UUID.randomUUID();

        GetEtiquetaByIdCommand cmd = EtiquetaCommandMapper.toGetByIdCommand(id);

        assertEquals(id, cmd.id());
    }

    @Test
    void toDeleteCommand_shouldMapIdAndConfirmFlag() {
        UUID id = UUID.randomUUID();

        DeleteEtiquetaCommand cmdTrue = EtiquetaCommandMapper.toDeleteCommand(id, true);
        DeleteEtiquetaCommand cmdFalse = EtiquetaCommandMapper.toDeleteCommand(id, false);

        assertEquals(id, cmdTrue.id());
        assertTrue(cmdTrue.confirm());
        assertEquals(id, cmdFalse.id());
        assertFalse(cmdFalse.confirm());
    }

    @Test
    void toGetAllCommand_withNullTipo_shouldMapToCommandWithNullFilter() {
        GetAllEtiquetasCommand cmd = EtiquetaCommandMapper.toGetAllCommand(null);

        assertNull(cmd.tipoEtiqueta());
    }

    @Test
    void toGetAllCommand_withTipo_shouldMapToCommandWithFilter() {
        GetAllEtiquetasCommand cmd = EtiquetaCommandMapper.toGetAllCommand(TipoEtiqueta.TAREA);

        assertEquals(TipoEtiqueta.TAREA, cmd.tipoEtiqueta());
    }
}
