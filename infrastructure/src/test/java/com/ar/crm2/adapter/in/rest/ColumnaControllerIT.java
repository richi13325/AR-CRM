package com.ar.crm2.adapter.in.rest;

import com.ar.crm2.adapter.in.rest.dto.request.CreateColumnaRequest;
import com.ar.crm2.adapter.in.rest.dto.request.EditColumnaRequest;
import com.ar.crm2.adapter.in.rest.dto.response.ColumnaResponse;
import com.ar.crm2.application.columna.command.CreateColumnaCommand;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ColumnaController REST adapter.
 *
 * @WebMvcTest loads only the web layer:
 * - Registers controllers, GlobalExceptionHandler and response mappers
 * - Configures MockMvc with real JSON serialization (Jackson)
 * - Does NOT load JPA, datasource or the full application context
 *
 * Use cases are mocked with @MockitoBean because they belong to the
 * application layer, not the web layer. This validates:
 * JSON deserialization → Controller → UseCase → JSON serialization → HTTP status.
 */
@WebMvcTest(controllers = {ColumnaController.class, GlobalExceptionHandler.class})
class ColumnaControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateColumnaUseCase createUseCase;

    @MockitoBean
    private GetAllColumnasUseCase getAllUseCase;

    @MockitoBean
    private GetColumnaByIdUseCase getByIdUseCase;

    @MockitoBean
    private EditColumnaUseCase editUseCase;

    @MockitoBean
    private DeleteColumnaUseCase deleteUseCase;

    // ── Helpers ─────────────────────────────────────────────────────

    private Columna buildColumna(UUID id, String nombre) {
        return Columna.reconstitute(
                ColumnaId.from(id),
                nombre,
                "#FFFFFF",
                TipoTablero.TAREAS,
                TipoColumna.PREDETERMINADA,
                false
        );
    }

    private ColumnaResponse buildColumnaResponse(UUID id, String nombre) {
        return new ColumnaResponse(
                id,
                nombre,
                "#FFFFFF",
                TipoTablero.TAREAS.name(),
                TipoColumna.PREDETERMINADA.name()
        );
    }

    // ── POST /api/columnas/create ─────────────────────────────────

    @Test
    void create_shouldReturn201WithDeserializedJson() throws Exception {
        UUID id = UUID.randomUUID();
        Columna columna = buildColumna(id, "Backlog");

        when(createUseCase.create(any(CreateColumnaCommand.class))).thenReturn(columna);

        String body = """
                {
                  "superUsuarioId": "%s",
                  "tableroId": "%s",
                  "nombre": "Backlog",
                  "color": "#FFFFFF",
                  "tipoTablero": "TAREAS",
                  "tipoColumna": "PREDETERMINADA"
                }
                """.formatted(UUID.randomUUID(), UUID.randomUUID());

        mockMvc.perform(post("/api/columnas/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nombre").value("Backlog"));

        verify(createUseCase).create(any(CreateColumnaCommand.class));
    }

    @Test
    void create_shouldPassCorrectFieldsToUseCase() throws Exception {
        UUID suId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        Columna columna = buildColumna(UUID.randomUUID(), "To Do");

        when(createUseCase.create(any())).thenReturn(columna);

        String body = """
                {
                  "superUsuarioId": "%s",
                  "id": "%s",
                  "nombre": "To Do",
                  "color": "#00FF00",
                  "tipoTablero": "TAREAS",
                  "tipoColumna": "PREDETERMINADA"
                }
                """.formatted(suId, id);

        mockMvc.perform(post("/api/columnas/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        verify(createUseCase).create(argThat(cmd ->
                suId.equals(cmd.superUsuarioId().orElse(null)) &&
                        "To Do".equals(cmd.nombre()) &&
                        "#00FF00".equals(cmd.color()) &&
                        TipoTablero.TAREAS.equals(cmd.tipoTablero()) &&
                        TipoColumna.PREDETERMINADA.equals(cmd.tipoColumna())
        ));
    }

    // ── GET /api/columnas/get-all ─────────────────────────────────

    @Test
    void getAll_shouldReturn200WithListOfColumnas() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        when(getAllUseCase.getAll()).thenReturn(List.of(
                buildColumna(id1, "Backlog"),
                buildColumna(id2, "Done")
        ));

        mockMvc.perform(get("/api/columnas/get-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(id1.toString()))
                .andExpect(jsonPath("$[0].nombre").value("Backlog"))
                .andExpect(jsonPath("$[1].id").value(id2.toString()))
                .andExpect(jsonPath("$[1].nombre").value("Done"));
    }

    // ── GET /api/columnas/get-by-id ────────────────────────────────

    @Test
    void getById_shouldReturn200WithColumna() throws Exception {
        UUID id = UUID.randomUUID();

        when(getByIdUseCase.getById(any(GetColumnaByIdCommand.class)))
                .thenReturn(buildColumna(id, "In Progress"));

        mockMvc.perform(get("/api/columnas/get-by-id")
                        .param("id", id.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nombre").value("In Progress"));
    }

    @Test
    void getById_shouldPassCorrectIdAsRequestParam() throws Exception {
        UUID id = UUID.randomUUID();

        when(getByIdUseCase.getById(any())).thenReturn(buildColumna(id, "To Do"));

        mockMvc.perform(get("/api/columnas/get-by-id")
                        .param("id", id.toString()))
                .andExpect(status().isOk());

        verify(getByIdUseCase).getById(argThat(cmd -> id.equals(cmd.id())));
    }

    // ── PUT /api/columnas/edit ─────────────────────────────────────

    @Test
    void edit_shouldReturn200WithUpdatedColumna() throws Exception {
        UUID id = UUID.randomUUID();
        Columna columna = buildColumna(id, "Review");

        when(editUseCase.edit(any(EditColumnaCommand.class))).thenReturn(columna);

        String body = """
                {
                  "nombre": "Review",
                  "color": "#FF0000",
                  "tipoTablero": "TAREAS",
                  "tipoColumna": "PREDETERMINADA",
                  "tableroId": null
                }
                """;

        mockMvc.perform(put("/api/columnas/edit")
                        .param("id", id.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nombre").value("Review"));
    }

    // ── DELETE /api/columnas/delete ────────────────────────────────

    @Test
    void delete_shouldReturn204() throws Exception {
        UUID id = UUID.randomUUID();

        doNothing().when(deleteUseCase).delete(any());

        mockMvc.perform(delete("/api/columnas/delete")
                        .param("id", id.toString()))
                .andExpect(status().isNoContent());

        verify(deleteUseCase).delete(any());
    }
}