package com.ar.crm2.adapter.in.rest;

import com.ar.crm2.adapter.in.rest.dto.request.AgregarColumnaRequest;
import com.ar.crm2.adapter.in.rest.dto.request.AsignarColumnaRequest;
import com.ar.crm2.adapter.in.rest.dto.request.CreateTableroRequest;
import com.ar.crm2.adapter.in.rest.dto.request.EditTableroRequest;
import com.ar.crm2.adapter.in.rest.dto.request.ReordenarColumnasRequest;
import com.ar.crm2.adapter.in.rest.dto.response.TableroResponse;
import com.ar.crm2.adapter.in.rest.mapper.TableroCommandMapper;
import com.ar.crm2.adapter.out.persistence.repository.ColumnaRepository;
import com.ar.crm2.application.security.ActorContext;
import com.ar.crm2.application.tablero.command.AgregarColumnaTableroCommand;
import com.ar.crm2.application.tablero.command.AsignarColumnaTableroCommand;
import com.ar.crm2.application.tablero.command.CreateTableroCommand;
import com.ar.crm2.application.tablero.command.DeleteTableroCommand;
import com.ar.crm2.application.tablero.command.EditTableroCommand;
import com.ar.crm2.application.tablero.command.EliminarColumnaDelTableroCommand;
import com.ar.crm2.application.tablero.command.GetTableroByIdCommand;
import com.ar.crm2.application.tablero.command.ReordenarColumnasCommand;
import com.ar.crm2.application.tablero.port.in.AgregarColumnaTableroUseCase;
import com.ar.crm2.application.tablero.port.in.AsignarColumnaTableroUseCase;
import com.ar.crm2.application.tablero.port.in.CreateTableroUseCase;
import com.ar.crm2.application.tablero.port.in.DeleteTableroUseCase;
import com.ar.crm2.application.tablero.port.in.EditTableroUseCase;
import com.ar.crm2.application.tablero.port.in.EliminarColumnaDelTableroUseCase;
import com.ar.crm2.application.tablero.port.in.GetAllTablerosUseCase;
import com.ar.crm2.application.tablero.port.in.GetTableroByIdUseCase;
import com.ar.crm2.application.tablero.port.in.ReordenarColumnasUseCase;
import com.ar.crm2.model.entity.Tablero;
import com.ar.crm2.model.enums.TipoEstadoColumnaTableroTarea;
import com.ar.crm2.model.enums.TipoEstadoColumnaTableroTrato;
import com.ar.crm2.model.enums.TipoTablero;
import com.ar.crm2.model.vo.ColumnaId;
import com.ar.crm2.model.vo.TableroId;
import com.ar.crm2.security.ActorContextRequestAttributeFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TableroControllerTest {

    @Mock
    private CreateTableroUseCase createUseCase;

    @Mock
    private GetAllTablerosUseCase getAllUseCase;

    @Mock
    private GetTableroByIdUseCase getByIdUseCase;

    @Mock
    private EditTableroUseCase editUseCase;

    @Mock
    private DeleteTableroUseCase deleteUseCase;

    @Mock
    private AgregarColumnaTableroUseCase agregarColumnaUseCase;

    @Mock
    private AsignarColumnaTableroUseCase asignarColumnaUseCase;

    @Mock
    private EliminarColumnaDelTableroUseCase eliminarColumnaUseCase;

    @Mock
    private ReordenarColumnasUseCase reordenarColumnasUseCase;

    @Mock
    private ColumnaRepository columnaRepository;

    @InjectMocks
    private TableroController controller;

    // ── Helpers ─────────────────────────────────────────────────────

    private Tablero createDomainTablero(UUID id, String nombre) {
        TableroId tableroId = TableroId.from(id);
        return Tablero.reconstitute(
                tableroId,
                nombre,
                "A board",
                List.of(),
                TipoTablero.TAREAS,
                LocalDateTime.now()
        );
    }

    // ── create ──────────────────────────────────────────────────────

    @Test
    void create_shouldReturnCreatedWithTableroResponse() {
        UUID id = UUID.randomUUID();
        UUID actorSuperUsuarioId = UUID.randomUUID();
        Tablero tablero = createDomainTablero(id, "Sprint Board");

        when(createUseCase.create(any(CreateTableroCommand.class))).thenReturn(tablero);

        CreateTableroRequest request = new CreateTableroRequest(
                "Sprint Board",
                "A board",
                TipoTablero.TAREAS,
                UUID.randomUUID(), // spoofable field — ignored
                true
        );

        ActorContext actorContext = new ActorContext(
                "auth-subject",
                "testuser",
                "test@example.com",
                Optional.of(UUID.randomUUID()),
                Optional.of(actorSuperUsuarioId),
                Set.of("USER")
        );

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getAttribute(ActorContextRequestAttributeFilter.ACTOR_CONTEXT_ATTRIBUTE))
                .thenReturn(actorContext);

        ResponseEntity<TableroResponse> response = controller.create(mockRequest, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(id, response.getBody().id());
        verify(createUseCase).create(any(CreateTableroCommand.class));

        // Verify superUsuarioId comes from ActorContext, NOT request body
        ArgumentCaptor<CreateTableroCommand> cmdCaptor = ArgumentCaptor.forClass(CreateTableroCommand.class);
        verify(createUseCase).create(cmdCaptor.capture());
        assertEquals(actorSuperUsuarioId, cmdCaptor.getValue().superUsuarioId());
    }

    @Test
    void create_shouldFailWhenActorContextMissingSuperUsuarioIdClaim() {
        CreateTableroRequest request = new CreateTableroRequest(
                "Board", "Desc", TipoTablero.TAREAS, UUID.randomUUID(), true
        );

        // ActorContext present but WITHOUT superUsuarioId claim
        ActorContext actorWithoutClaim = new ActorContext(
                "auth-subject",
                "testuser",
                "test@example.com",
                Optional.of(UUID.randomUUID()),
                Optional.empty(), // superUsuarioId claim missing
                Set.of("USER")
        );

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getAttribute(ActorContextRequestAttributeFilter.ACTOR_CONTEXT_ATTRIBUTE))
                .thenReturn(actorWithoutClaim);

        // Mapper throws IllegalStateException when superUsuarioId is absent from token
        assertThrows(IllegalStateException.class,
                () -> controller.create(mockRequest, request));
    }

    // ── getAll ─────────────────────────────────────────────────────

    @Test
    void getAll_shouldReturnOkWithListOfTableros() {
        Tablero t1 = createDomainTablero(UUID.randomUUID(), "Board 1");
        Tablero t2 = createDomainTablero(UUID.randomUUID(), "Board 2");

        when(getAllUseCase.getAll()).thenReturn(List.of(t1, t2));

        ResponseEntity<List<TableroResponse>> response = controller.getAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(getAllUseCase).getAll();
    }

    // ── getById ────────────────────────────────────────────────────

    @Test
    void getById_shouldReturnOkWithTableroResponse() {
        UUID id = UUID.randomUUID();
        Tablero tablero = createDomainTablero(id, "Sprint Board");

        when(getByIdUseCase.getById(any(GetTableroByIdCommand.class))).thenReturn(tablero);

        ResponseEntity<TableroResponse> response = controller.getById(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(id, response.getBody().id());
    }

    @Test
    void getById_shouldPassCorrectIdToCommand() {
        UUID id = UUID.randomUUID();
        Tablero tablero = createDomainTablero(id, "Board");

        when(getByIdUseCase.getById(any())).thenReturn(tablero);

        controller.getById(id);

        ArgumentCaptor<GetTableroByIdCommand> captor = ArgumentCaptor.forClass(GetTableroByIdCommand.class);
        verify(getByIdUseCase).getById(captor.capture());
        assertEquals(id, captor.getValue().id());
    }

    // ── edit ───────────────────────────────────────────────────────

    @Test
    void edit_shouldReturnOkWithUpdatedTableroResponse() {
        UUID id = UUID.randomUUID();
        Tablero tablero = createDomainTablero(id, "Updated Board");

        when(editUseCase.edit(any(EditTableroCommand.class))).thenReturn(tablero);

        EditTableroRequest request = new EditTableroRequest("Updated Board", "New description");

        ResponseEntity<TableroResponse> response = controller.edit(id, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(id, response.getBody().id());
    }

    // ── delete ─────────────────────────────────────────────────────

    @Test
    void delete_shouldReturnNoContent() {
        UUID id = UUID.randomUUID();
        doNothing().when(deleteUseCase).delete(any(DeleteTableroCommand.class));

        ResponseEntity<Void> response = controller.delete(id);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(deleteUseCase).delete(any(DeleteTableroCommand.class));
    }

    @Test
    void delete_shouldPassCorrectIdToCommand() {
        UUID id = UUID.randomUUID();

        controller.delete(id);

        ArgumentCaptor<DeleteTableroCommand> captor = ArgumentCaptor.forClass(DeleteTableroCommand.class);
        verify(deleteUseCase).delete(captor.capture());
        assertEquals(id, captor.getValue().id());
    }

    // ── agregarColumna ─────────────────────────────────────────────

    @Test
    void agregarColumna_shouldReturnCreatedWithTableroResponse() {
        UUID tableroId = UUID.randomUUID();
        Tablero tablero = createDomainTablero(tableroId, "Board with column");

        when(agregarColumnaUseCase.agregarColumna(any(AgregarColumnaTableroCommand.class)))
                .thenReturn(tablero);

        AgregarColumnaRequest request = new AgregarColumnaRequest(
                "New Column",
                "#FF0000",
                com.ar.crm2.model.enums.TipoColumna.PERSONALIZADA,
                5,
                "Note",
                TipoEstadoColumnaTableroTarea.PENDIENTE,
                null,
                new BigDecimal("1000"),
                false
        );

        ResponseEntity<TableroResponse> response = controller.agregarColumna(tableroId, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(agregarColumnaUseCase).agregarColumna(any(AgregarColumnaTableroCommand.class));
    }

    // ── eliminarColumna ────────────────────────────────────────────

    @Test
    void eliminarColumna_shouldReturnNoContent() {
        UUID tableroId = UUID.randomUUID();
        UUID columnaId = UUID.randomUUID();
        Tablero tablero = createDomainTablero(tableroId, "Board");

        when(eliminarColumnaUseCase.eliminarColumna(any(EliminarColumnaDelTableroCommand.class)))
                .thenReturn(tablero);

        ResponseEntity<Void> response = controller.eliminarColumna(tableroId, columnaId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(eliminarColumnaUseCase)
                .eliminarColumna(any(EliminarColumnaDelTableroCommand.class));
    }

    // ── asignarColumna ──────────────────────────────────────────────

    @Test
    void asignarColumna_shouldReturnCreatedWithTableroResponse() {
        UUID tableroId = UUID.randomUUID();
        UUID columnaId = UUID.randomUUID();
        Tablero tablero = createDomainTablero(tableroId, "Board with assigned column");

        when(asignarColumnaUseCase.asignarColumna(any(AsignarColumnaTableroCommand.class)))
                .thenReturn(tablero);

        AsignarColumnaRequest request = new AsignarColumnaRequest(
                3,
                "Assignment note",
                TipoEstadoColumnaTableroTarea.PENDIENTE,
                null,
                new BigDecimal("500")
        );

        ResponseEntity<TableroResponse> response =
                controller.asignarColumna(tableroId, columnaId, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(asignarColumnaUseCase).asignarColumna(any(AsignarColumnaTableroCommand.class));
    }

    @Test
    void asignarColumna_shouldPassCorrectIdsAndRequestToMapper() {
        UUID tableroId = UUID.randomUUID();
        UUID columnaId = UUID.randomUUID();
        Tablero tablero = createDomainTablero(tableroId, "Board");

        when(asignarColumnaUseCase.asignarColumna(any())).thenReturn(tablero);

        AsignarColumnaRequest request = new AsignarColumnaRequest(
                2,
                "Test note",
                null,
                TipoEstadoColumnaTableroTrato.ABIERTO,
                new BigDecimal("200")
        );

        controller.asignarColumna(tableroId, columnaId, request);

        ArgumentCaptor<AsignarColumnaTableroCommand> captor =
                ArgumentCaptor.forClass(AsignarColumnaTableroCommand.class);
        verify(asignarColumnaUseCase).asignarColumna(captor.capture());

        AsignarColumnaTableroCommand cmd = captor.getValue();
        assertEquals(tableroId, cmd.tableroId());
        assertEquals(columnaId, cmd.columnaId());
        assertEquals(2, cmd.limiteWip());
        assertEquals("Test note", cmd.nota());
    }

    // ── reordenarColumnas ──────────────────────────────────────────

    @Test
    void reordenarColumnas_shouldReturnOkWithReorderedTableroResponse() {
        UUID tableroId = UUID.randomUUID();
        Tablero tablero = createDomainTablero(tableroId, "Reordered Board");

        when(reordenarColumnasUseCase.reordenar(any(ReordenarColumnasCommand.class)))
                .thenReturn(tablero);

        List<ColumnaId> nuevoOrden = List.of(
                ColumnaId.from(UUID.randomUUID()),
                ColumnaId.from(UUID.randomUUID())
        );

        ReordenarColumnasRequest request = new ReordenarColumnasRequest(nuevoOrden);

        ResponseEntity<TableroResponse> response =
                controller.reordenarColumnas(tableroId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(reordenarColumnasUseCase).reordenar(any(ReordenarColumnasCommand.class));
    }
}