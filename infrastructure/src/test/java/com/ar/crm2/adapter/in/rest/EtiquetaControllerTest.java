package com.ar.crm2.adapter.in.rest;

import com.ar.crm2.adapter.in.rest.dto.request.CreateEtiquetaRequest;
import com.ar.crm2.adapter.in.rest.dto.request.EditEtiquetaRequest;
import com.ar.crm2.adapter.in.rest.dto.response.EtiquetaResponse;
import com.ar.crm2.adapter.in.rest.mapper.EtiquetaCommandMapper;
import com.ar.crm2.application.etiqueta.command.CreateEtiquetaCommand;
import com.ar.crm2.application.etiqueta.command.DeleteEtiquetaCommand;
import com.ar.crm2.application.etiqueta.command.EditEtiquetaCommand;
import com.ar.crm2.application.etiqueta.command.GetAllEtiquetasCommand;
import com.ar.crm2.application.etiqueta.command.GetEtiquetaByIdCommand;
import com.ar.crm2.application.etiqueta.exception.EtiquetaNotFoundException;
import com.ar.crm2.application.etiqueta.exception.EtiquetaRequiresConfirmationException;
import com.ar.crm2.application.etiqueta.port.in.CreateEtiquetaUseCase;
import com.ar.crm2.application.etiqueta.port.in.DeleteEtiquetaUseCase;
import com.ar.crm2.application.etiqueta.port.in.EditEtiquetaUseCase;
import com.ar.crm2.application.etiqueta.port.in.GetAllEtiquetasUseCase;
import com.ar.crm2.application.etiqueta.port.in.GetEtiquetaByIdUseCase;
import com.ar.crm2.model.entity.Etiqueta;
import com.ar.crm2.model.enums.TipoEtiqueta;
import com.ar.crm2.model.vo.EtiquetaId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Controller tests for EtiquetaController.
 * Covers the inbound REST adapter surface: status code mapping, DTO -> command
 * mapping, and propagation of use-case results to EtiquetaResponse.
 *
 * <p>Strict TDD: written before EtiquetaController, EtiquetaCommandMapper,
 * EtiquetaRequest/Response DTOs existed. Now it pins their contract.
 */
@ExtendWith(MockitoExtension.class)
class EtiquetaControllerTest {

    @Mock
    private CreateEtiquetaUseCase createUseCase;

    @Mock
    private GetAllEtiquetasUseCase getAllUseCase;

    @Mock
    private GetEtiquetaByIdUseCase getByIdUseCase;

    @Mock
    private EditEtiquetaUseCase editUseCase;

    @Mock
    private DeleteEtiquetaUseCase deleteUseCase;

    @InjectMocks
    private EtiquetaController controller;

    // ── Helpers ─────────────────────────────────────────────────────

    private Etiqueta buildDomainEtiqueta(UUID id, String nombre, TipoEtiqueta tipo, String color) {
        return Etiqueta.reconstitute(
            EtiquetaId.from(id),
            nombre,
            tipo,
            color,
            LocalDateTime.now()
        );
    }

    // ── create ──────────────────────────────────────────────────────

    @Test
    void create_shouldReturn201WithEtiquetaResponse() {
        UUID id = UUID.randomUUID();
        Etiqueta etiqueta = buildDomainEtiqueta(id, "Urgent", TipoEtiqueta.TAREA, "#FF0000");

        when(createUseCase.create(any(CreateEtiquetaCommand.class))).thenReturn(etiqueta);

        CreateEtiquetaRequest request = new CreateEtiquetaRequest(
            "Urgent", TipoEtiqueta.TAREA, "#FF0000"
        );

        ResponseEntity<EtiquetaResponse> response = controller.create(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(id, response.getBody().id());
        assertEquals("Urgent", response.getBody().nombre());
        assertEquals("TAREA", response.getBody().tipoEtiqueta());
        assertEquals("#FF0000", response.getBody().color());
        verify(createUseCase).create(any(CreateEtiquetaCommand.class));
    }

    @Test
    void create_shouldMapRequestFieldsToCommand() {
        Etiqueta etiqueta = buildDomainEtiqueta(UUID.randomUUID(), "VIP", TipoEtiqueta.TRATO, "#FFD700");

        when(createUseCase.create(any())).thenReturn(etiqueta);

        CreateEtiquetaRequest request = new CreateEtiquetaRequest(
            "VIP", TipoEtiqueta.TRATO, "#FFD700"
        );

        controller.create(request);

        ArgumentCaptor<CreateEtiquetaCommand> captor = ArgumentCaptor.forClass(CreateEtiquetaCommand.class);
        verify(createUseCase).create(captor.capture());

        CreateEtiquetaCommand cmd = captor.getValue();
        assertEquals("VIP", cmd.nombre());
        assertEquals(TipoEtiqueta.TRATO, cmd.tipoEtiqueta());
        assertEquals("#FFD700", cmd.color());
    }

    // ── getAll ──────────────────────────────────────────────────────

    @Test
    void getAll_shouldReturnOkWithListOfEtiquetas() {
        Etiqueta e1 = buildDomainEtiqueta(UUID.randomUUID(), "Urgent", TipoEtiqueta.TAREA, "#FF0000");
        Etiqueta e2 = buildDomainEtiqueta(UUID.randomUUID(), "VIP", TipoEtiqueta.TRATO, "#FFD700");

        when(getAllUseCase.getAll(any(Optional.class))).thenReturn(List.of(e1, e2));

        ResponseEntity<List<EtiquetaResponse>> response = controller.getAll(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(getAllUseCase).getAll(any(Optional.class));
    }

    @Test
    void getAll_shouldPassTipoFilterToUseCase() {
        Etiqueta e1 = buildDomainEtiqueta(UUID.randomUUID(), "Urgent", TipoEtiqueta.TAREA, "#FF0000");

        when(getAllUseCase.getAll(any(Optional.class))).thenReturn(List.of(e1));

        controller.getAll(TipoEtiqueta.TAREA);

        ArgumentCaptor<Optional<TipoEtiqueta>> captor = ArgumentCaptor.forClass(Optional.class);
        verify(getAllUseCase).getAll(captor.capture());
        assertEquals(Optional.of(TipoEtiqueta.TAREA), captor.getValue());
    }

    @Test
    void getAll_shouldMapEachEtiquetaToResponse() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Etiqueta e1 = buildDomainEtiqueta(id1, "A", TipoEtiqueta.TAREA, "#000000");
        Etiqueta e2 = buildDomainEtiqueta(id2, "B", TipoEtiqueta.TRATO, "#FFFFFF");

        when(getAllUseCase.getAll(any(Optional.class))).thenReturn(List.of(e1, e2));

        ResponseEntity<List<EtiquetaResponse>> response = controller.getAll(null);

        assertNotNull(response.getBody());
        assertEquals(id1, response.getBody().get(0).id());
        assertEquals("A", response.getBody().get(0).nombre());
        assertEquals("TAREA", response.getBody().get(0).tipoEtiqueta());
        assertEquals(id2, response.getBody().get(1).id());
        assertEquals("B", response.getBody().get(1).nombre());
        assertEquals("TRATO", response.getBody().get(1).tipoEtiqueta());
    }

    // ── getById ─────────────────────────────────────────────────────

    @Test
    void getById_shouldReturnOkWithEtiquetaResponse() {
        UUID id = UUID.randomUUID();
        Etiqueta etiqueta = buildDomainEtiqueta(id, "Urgent", TipoEtiqueta.TAREA, "#FF0000");

        when(getByIdUseCase.getById(any(GetEtiquetaByIdCommand.class))).thenReturn(etiqueta);

        ResponseEntity<EtiquetaResponse> response = controller.getById(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(id, response.getBody().id());
        assertEquals("Urgent", response.getBody().nombre());
    }

    @Test
    void getById_shouldPassCorrectIdToCommand() {
        UUID id = UUID.randomUUID();
        Etiqueta etiqueta = buildDomainEtiqueta(id, "X", TipoEtiqueta.TAREA, "#000000");

        when(getByIdUseCase.getById(any())).thenReturn(etiqueta);

        controller.getById(id);

        ArgumentCaptor<GetEtiquetaByIdCommand> captor = ArgumentCaptor.forClass(GetEtiquetaByIdCommand.class);
        verify(getByIdUseCase).getById(captor.capture());
        assertEquals(id, captor.getValue().id());
    }

    @Test
    void getById_shouldReturn404WhenEtiquetaNotFound() {
        UUID id = UUID.randomUUID();
        when(getByIdUseCase.getById(any(GetEtiquetaByIdCommand.class)))
            .thenThrow(EtiquetaNotFoundException.forId(id));

        assertThrows(EtiquetaNotFoundException.class, () -> controller.getById(id));
    }

    // ── edit ────────────────────────────────────────────────────────

    @Test
    void edit_shouldReturnOkWithUpdatedEtiquetaResponse() {
        UUID id = UUID.randomUUID();
        Etiqueta etiqueta = buildDomainEtiqueta(id, "Renamed", TipoEtiqueta.TAREA, "#00FF00");

        when(editUseCase.edit(any(EditEtiquetaCommand.class))).thenReturn(etiqueta);

        EditEtiquetaRequest request = new EditEtiquetaRequest("Renamed", "#00FF00");

        ResponseEntity<EtiquetaResponse> response = controller.edit(id, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(id, response.getBody().id());
        assertEquals("Renamed", response.getBody().nombre());
        assertEquals("#00FF00", response.getBody().color());
        verify(editUseCase).edit(any(EditEtiquetaCommand.class));
    }

    @Test
    void edit_shouldPassCorrectIdAndRequestToMapper() {
        UUID id = UUID.randomUUID();
        Etiqueta etiqueta = buildDomainEtiqueta(id, "Renamed", TipoEtiqueta.TAREA, "#00FF00");

        when(editUseCase.edit(any())).thenReturn(etiqueta);

        EditEtiquetaRequest request = new EditEtiquetaRequest("Renamed", "#00FF00");

        controller.edit(id, request);

        ArgumentCaptor<EditEtiquetaCommand> captor = ArgumentCaptor.forClass(EditEtiquetaCommand.class);
        verify(editUseCase).edit(captor.capture());

        EditEtiquetaCommand cmd = captor.getValue();
        assertEquals(id, cmd.id());
        assertEquals("Renamed", cmd.nombre());
        assertEquals("#00FF00", cmd.color());
    }

    // ── delete ──────────────────────────────────────────────────────

    @Test
    void delete_shouldReturnNoContent() {
        UUID id = UUID.randomUUID();
        doNothing().when(deleteUseCase).delete(any(DeleteEtiquetaCommand.class));

        ResponseEntity<Void> response = controller.delete(id, true);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(deleteUseCase).delete(any(DeleteEtiquetaCommand.class));
    }

    @Test
    void delete_shouldPassCorrectIdAndConfirmFlagToCommand() {
        UUID id = UUID.randomUUID();

        controller.delete(id, false);

        ArgumentCaptor<DeleteEtiquetaCommand> captor = ArgumentCaptor.forClass(DeleteEtiquetaCommand.class);
        verify(deleteUseCase).delete(captor.capture());
        assertEquals(id, captor.getValue().id());
        assertFalse(captor.getValue().confirm());
    }

    @Test
    void delete_shouldPropagateConfirmationRequiredException() {
        UUID id = UUID.randomUUID();
        doThrow(new EtiquetaRequiresConfirmationException())
            .when(deleteUseCase).delete(any(DeleteEtiquetaCommand.class));

        assertThrows(EtiquetaRequiresConfirmationException.class,
            () -> controller.delete(id, false));
    }
}
