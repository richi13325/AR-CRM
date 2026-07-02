package com.ar.crm2.adapter.in.tool.ai;

import com.ar.crm2.adapter.in.tool.ai.dto.ListarColumnasTableroRequest;
import com.ar.crm2.adapter.in.tool.ai.dto.ListarColumnasTableroResponse;
import com.ar.crm2.application.ai.port.out.ColumnaLecturaPort;
import com.ar.crm2.application.contacto.port.in.CreateContactoUseCase;
import com.ar.crm2.application.ficha.port.in.MoverColumnaFichaUseCase;
import com.ar.crm2.application.tarea.port.in.CreateTareaUseCase;
import com.ar.crm2.application.trato.port.in.CreateTratoUseCase;
import com.ar.crm2.model.entity.Columna;
import com.ar.crm2.model.enums.TipoColumna;
import com.ar.crm2.model.enums.TipoTablero;
import com.ar.crm2.model.vo.ColumnaId;
import com.ar.crm2.model.vo.SuperUsuarioId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * RED-first tests for {@link ListarColumnasTableroTool} — the
 * read-only {@code @Tool} input adapter that returns the catalog
 * columns for a board type so the model can pick a valid target
 * column before proposing {@code MOVE_KANBAN_FICHA}.
 */
@ExtendWith(MockitoExtension.class)
class ListarColumnasTableroToolTest {

    @Mock
    private ColumnaLecturaPort columnaLecturaPort;

    private ListarColumnasTableroTool tool;

    @BeforeEach
    void setUp() {
        tool = new ListarColumnasTableroTool(columnaLecturaPort);
    }

    @Test
    @DisplayName("listarColumnasTablero returns the catalog columns for the requested board type")
    void listarColumnasTablero_returnsCatalog() {
        when(columnaLecturaPort.findByTipoTablero(any(TipoTablero.class)))
            .thenReturn(List.of(
                sampleColumna("Pendiente", TipoTablero.TAREAS, TipoColumna.PREDETERMINADA),
                sampleColumna("En curso", TipoTablero.TAREAS, TipoColumna.PREDETERMINADA),
                sampleColumna("Hecho", TipoTablero.TAREAS, TipoColumna.PREDETERMINADA)
            ));

        List<ListarColumnasTableroResponse> result = tool.listarColumnasTablero(
            new ListarColumnasTableroRequest("TAREAS"));

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Pendiente", result.get(0).nombre());
        assertEquals("TAREAS", result.get(0).tipoTablero());
        assertEquals("PREDETERMINADA", result.get(0).tipoColumna());

        ArgumentCaptor<TipoTablero> captor = ArgumentCaptor.forClass(TipoTablero.class);
        verify(columnaLecturaPort).findByTipoTablero(captor.capture());
        assertEquals(TipoTablero.TAREAS, captor.getValue(),
            "the tool MUST convert the model-supplied string to the TipoTablero enum");
    }

    @Test
    @DisplayName("listarColumnasTablero returns an empty list when the catalog is empty")
    void listarColumnasTablero_returnsEmptyWhenCatalogEmpty() {
        when(columnaLecturaPort.findByTipoTablero(any(TipoTablero.class))).thenReturn(List.of());

        List<ListarColumnasTableroResponse> result = tool.listarColumnasTablero(
            new ListarColumnasTableroRequest("TRATOS"));

        assertNotNull(result);
        assertTrue(result.isEmpty(),
            "an empty catalog must propagate as an empty list — never null");
    }

    @Test
    @DisplayName("@Tool method is annotated and request carries @ToolParam")
    void toolMethod_isAnnotated() throws NoSuchMethodException {
        Method m = ListarColumnasTableroTool.class.getDeclaredMethod(
            "listarColumnasTablero", ListarColumnasTableroRequest.class);
        Tool toolAnn = m.getAnnotation(Tool.class);
        assertNotNull(toolAnn, "@Tool annotation required");
        assertFalse(toolAnn.description().isBlank());
        assertEquals("listarColumnasTablero", toolAnn.name());

        Constructor<?>[] ctors = ListarColumnasTableroRequest.class.getDeclaredConstructors();
        assertEquals(1, ctors.length);
        Parameter[] params = ctors[0].getParameters();
        assertEquals(1, params.length, "Request must expose exactly one field: tipoTablero");
        ToolParam ann = params[0].getAnnotation(ToolParam.class);
        assertNotNull(ann, "@ToolParam required on tipoTablero");
        assertFalse(ann.description().isBlank());
        assertEquals("tipoTablero", params[0].getName());
    }

    @Test
    @DisplayName("Constructor declares ONLY the read port — no mutation use cases")
    void constructor_doesNotInjectRealMutationUseCases() {
        Constructor<?>[] ctors = ListarColumnasTableroTool.class.getDeclaredConstructors();
        assertEquals(1, ctors.length);
        Class<?>[] params = ctors[0].getParameterTypes();
        assertEquals(1, params.length,
            "tool must take exactly 1 collaborator: ColumnaLecturaPort");
        assertEquals(ColumnaLecturaPort.class, params[0]);
        for (Class<?> forbidden : List.of(
                CreateContactoUseCase.class, CreateTratoUseCase.class,
                CreateTareaUseCase.class, MoverColumnaFichaUseCase.class)) {
            for (Field f : ListarColumnasTableroTool.class.getDeclaredFields()) {
                assertFalse(f.getType().equals(forbidden),
                    "field '" + f.getName() + "' leaks " + forbidden.getSimpleName());
            }
        }
    }

    @Test
    @DisplayName("Tool class does not hold DTOs / domain entities as fields (delegation only)")
    void toolClass_doesNotHoldDtoConstructionFields() {
        for (Field f : ListarColumnasTableroTool.class.getDeclaredFields()) {
            assertFalse(f.getType().equals(ListarColumnasTableroRequest.class));
            assertFalse(f.getType().equals(ListarColumnasTableroResponse.class));
            assertFalse(f.getType().equals(Columna.class));
        }
    }

    private static Columna sampleColumna(String nombre, TipoTablero tipo, TipoColumna subtipo) {
        return Columna.reconstitute(
            ColumnaId.create(),
            nombre,
            "#FFFFFF",
            tipo,
            subtipo,
            false
        );
    }
}