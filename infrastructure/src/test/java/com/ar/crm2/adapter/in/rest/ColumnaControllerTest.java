package com.ar.crm2.adapter.in.rest;

import com.ar.crm2.adapter.in.rest.dto.request.CreateColumnaRequest;
import com.ar.crm2.adapter.in.rest.dto.request.EditColumnaRequest;
import com.ar.crm2.adapter.in.rest.dto.response.ColumnaResponse;
import com.ar.crm2.adapter.in.rest.mapper.ColumnaCommandMapper;
import com.ar.crm2.application.columna.command.CreateColumnaCommand;
import com.ar.crm2.application.columna.command.DeleteColumnaCommand;
import com.ar.crm2.application.columna.command.EditColumnaCommand;
import com.ar.crm2.application.columna.command.GetColumnaByIdCommand;
import com.ar.crm2.application.columna.port.in.CreateColumnaUseCase;
import com.ar.crm2.application.columna.port.in.DeleteColumnaUseCase;
import com.ar.crm2.application.columna.port.in.EditColumnaUseCase;
import com.ar.crm2.application.columna.port.in.GetAllColumnasUseCase;
import com.ar.crm2.application.columna.port.in.GetColumnaByIdUseCase;
import com.ar.crm2.model.entity.Columna;
import com.ar.crm2.model.enums.TipoColumna;
import com.ar.crm2.model.enums.TipoTablero;
import com.ar.crm2.model.vo.ColumnaId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ColumnaControllerTest {

    @Mock
    private CreateColumnaUseCase createUseCase;

    @Mock
    private GetAllColumnasUseCase getAllUseCase;

    @Mock
    private GetColumnaByIdUseCase getByIdUseCase;

    @Mock
    private EditColumnaUseCase editUseCase;

    @Mock
    private DeleteColumnaUseCase deleteUseCase;

    @InjectMocks
    private ColumnaController controller;

    // ── Helpers ─────────────────────────────────────────────────────

    private Columna createDomainColumna(UUID id, String nombre) {
        return Columna.reconstitute(
                ColumnaId.from(id),
                nombre,
                "#FFFFFF",
                TipoTablero.TAREAS,
                TipoColumna.PREDETERMINADA,
                false
        );
    }

    // ── create ──────────────────────────────────────────────────────

    @Test
    void create_shouldReturnCreatedWithColumnaResponse() {
        UUID id = UUID.randomUUID();
        Columna columna = createDomainColumna(id, "Backlog");

        when(createUseCase.create(any(CreateColumnaCommand.class))).thenReturn(columna);

        CreateColumnaRequest request = new CreateColumnaRequest(
                UUID.randomUUID(),
                "Backlog",
                "#FFFFFF",
                TipoTablero.TAREAS,
                TipoColumna.PREDETERMINADA
        );

        ResponseEntity<ColumnaResponse> response = controller.create(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(id, response.getBody().id());
        assertEquals("Backlog", response.getBody().nombre());
        verify(createUseCase).create(any(CreateColumnaCommand.class));
    }

    @Test
    void create_shouldMapRequestToCorrectCommandFields() {
        UUID suId = UUID.randomUUID();
        Columna columna = createDomainColumna(UUID.randomUUID(), "To Do");

        when(createUseCase.create(any())).thenReturn(columna);

        CreateColumnaRequest request = new CreateColumnaRequest(
                suId,
                "To Do",
                "#00FF00",
                TipoTablero.TAREAS,
                TipoColumna.PREDETERMINADA
        );

        controller.create(request);

        ArgumentCaptor<CreateColumnaCommand> captor = ArgumentCaptor.forClass(CreateColumnaCommand.class);
        verify(createUseCase).create(captor.capture());

        CreateColumnaCommand cmd = captor.getValue();
        assertEquals(suId, cmd.superUsuarioId().orElse(null));
        assertEquals("To Do", cmd.nombre());
        assertEquals("#00FF00", cmd.color());
        assertEquals(TipoTablero.TAREAS, cmd.tipoTablero());
        assertEquals(TipoColumna.PREDETERMINADA, cmd.tipoColumna());
    }

    // ── getAll ─────────────────────────────────────────────────────

    @Test
    void getAll_shouldReturnOkWithListOfColumnas() {
        Columna c1 = createDomainColumna(UUID.randomUUID(), "Backlog");
        Columna c2 = createDomainColumna(UUID.randomUUID(), "To Do");

        when(getAllUseCase.getAll()).thenReturn(List.of(c1, c2));

        ResponseEntity<List<ColumnaResponse>> response = controller.getAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(getAllUseCase).getAll();
    }

    @Test
    void getAll_shouldMapEachColumnaToResponse() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Columna c1 = createDomainColumna(id1, "Backlog");
        Columna c2 = createDomainColumna(id2, "Done");

        when(getAllUseCase.getAll()).thenReturn(List.of(c1, c2));

        ResponseEntity<List<ColumnaResponse>> response = controller.getAll();

        assertNotNull(response.getBody());
        assertEquals(id1, response.getBody().get(0).id());
        assertEquals("Backlog", response.getBody().get(0).nombre());
        assertEquals(id2, response.getBody().get(1).id());
        assertEquals("Done", response.getBody().get(1).nombre());
    }

    // ── getById ────────────────────────────────────────────────────

    @Test
    void getById_shouldReturnOkWithColumnaResponse() {
        UUID id = UUID.randomUUID();
        Columna columna = createDomainColumna(id, "Backlog");

        when(getByIdUseCase.getById(any(GetColumnaByIdCommand.class))).thenReturn(columna);

        ResponseEntity<ColumnaResponse> response = controller.getById(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(id, response.getBody().id());
        assertEquals("Backlog", response.getBody().nombre());
    }

    @Test
    void getById_shouldPassCorrectIdToCommand() {
        UUID id = UUID.randomUUID();
        Columna columna = createDomainColumna(id, "In Progress");

        when(getByIdUseCase.getById(any())).thenReturn(columna);

        controller.getById(id);

        ArgumentCaptor<GetColumnaByIdCommand> captor = ArgumentCaptor.forClass(GetColumnaByIdCommand.class);
        verify(getByIdUseCase).getById(captor.capture());
        assertEquals(id, captor.getValue().id());
    }

    // ── edit ───────────────────────────────────────────────────────

    @Test
    void edit_shouldReturnOkWithUpdatedColumnaResponse() {
        UUID id = UUID.randomUUID();
        Columna columna = createDomainColumna(id, "Sprint");

        when(editUseCase.edit(any(EditColumnaCommand.class))).thenReturn(columna);

        EditColumnaRequest request = new EditColumnaRequest(
                "Sprint",
                "#0000FF",
                TipoTablero.TAREAS,
                TipoColumna.PREDETERMINADA
        );

        ResponseEntity<ColumnaResponse> response = controller.edit(id, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(id, response.getBody().id());
        verify(editUseCase).edit(any(EditColumnaCommand.class));
    }

    @Test
    void edit_shouldPassCorrectIdAndRequestToMapper() {
        UUID id = UUID.randomUUID();
        Columna columna = createDomainColumna(id, "Review");

        when(editUseCase.edit(any())).thenReturn(columna);

        EditColumnaRequest request = new EditColumnaRequest(
                "Review",
                "#FF0000",
                TipoTablero.TAREAS,
                TipoColumna.PREDETERMINADA
        );

        controller.edit(id, request);

        ArgumentCaptor<EditColumnaCommand> captor = ArgumentCaptor.forClass(EditColumnaCommand.class);
        verify(editUseCase).edit(captor.capture());

        EditColumnaCommand cmd = captor.getValue();
        assertEquals(id, cmd.id());
        assertEquals("Review", cmd.nombre());
        assertEquals("#FF0000", cmd.color());
    }

    // ── delete ─────────────────────────────────────────────────────

    @Test
    void delete_shouldReturnNoContent() {
        UUID id = UUID.randomUUID();
        doNothing().when(deleteUseCase).delete(any(DeleteColumnaCommand.class));

        ResponseEntity<Void> response = controller.delete(id);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(deleteUseCase).delete(any(DeleteColumnaCommand.class));
    }

    @Test
    void delete_shouldPassCorrectIdToCommand() {
        UUID id = UUID.randomUUID();

        controller.delete(id);

        ArgumentCaptor<DeleteColumnaCommand> captor = ArgumentCaptor.forClass(DeleteColumnaCommand.class);
        verify(deleteUseCase).delete(captor.capture());
        assertEquals(id, captor.getValue().id());
    }
}