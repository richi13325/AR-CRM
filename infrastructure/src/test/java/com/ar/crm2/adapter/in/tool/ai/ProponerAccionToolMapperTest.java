package com.ar.crm2.adapter.in.tool.ai;

import com.ar.crm2.adapter.in.tool.ai.dto.ProponerAccionRequest;
import com.ar.crm2.application.ai.command.ProponerAccionCommand;
import com.ar.crm2.application.ai.port.in.result.ProponerAccionResponse;
import com.ar.crm2.model.enums.TipoAccion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * RED-first tests for {@link ProponerAccionToolMapper}.
 *
 * <p>The mapper is the single place that translates between the
 * infrastructure wire DTOs ({@link ProponerAccionRequest},
 * {@link com.ar.crm2.adapter.in.tool.ai.dto.ProponerAccionResponse})
 * and the application-owned {@link ProponerAccionCommand} / use case
 * result. These tests pin the contract so a future refactor cannot:
 * <ul>
 *   <li>Forget to convert {@code String} into the typed
 *       {@link TipoAccion}.</li>
 *   <li>Drop the {@code ttlMinutos}.</li>
 *   <li>Build a response with a null use case result.</li>
 *   <li>Leak actor/tenant/conv scope into the mapper (the mapper
 *       only translates the model-supplied shape — trusted scope
 *       is owned by the use case).</li>
 * </ul>
 */
class ProponerAccionToolMapperTest {

    @Test
    @DisplayName("toCommand builds a ProponerAccionCommand with the typed TipoAccion and all request fields")
    void toCommand_copiesAllRequestFields() {
        ProponerAccionRequest request = new ProponerAccionRequest(
            "CREATE_TAREA",
            "{\"titulo\":\"Llamar\"}",
            "el cliente pidió seguimiento",
            1440
        );

        ProponerAccionCommand cmd = ProponerAccionToolMapper.toCommand(request);

        assertNotNull(cmd);
        assertEquals(TipoAccion.CREATE_TAREA, cmd.tipo(),
            "mapper must convert String into the matching typed TipoAccion");
        assertEquals("{\"titulo\":\"Llamar\"}", cmd.payloadJson());
        assertEquals("el cliente pidió seguimiento", cmd.rationale());
        assertEquals(1440, cmd.ttlMinutos());
    }

    @Test
    @DisplayName("toCommand rejects an unknown tipoAccion string via TipoAccion.valueOf")
    void toCommand_unknownTipoAccion_throws() {
        ProponerAccionRequest request = new ProponerAccionRequest(
            "BOGUS_TIPO", "{}", "r", 60
        );
        assertThrows(IllegalArgumentException.class,
            () -> ProponerAccionToolMapper.toCommand(request));
    }

    @Test
    @DisplayName("toResponse projects the use case result into the wire shape")
    void toResponse_projectsIdAndEstado() {
        ProponerAccionResponse result =
            new ProponerAccionResponse("accion-xyz", "PENDING");

        com.ar.crm2.adapter.in.tool.ai.dto.ProponerAccionResponse response =
            ProponerAccionToolMapper.toResponse(result);

        assertEquals("accion-xyz", response.accionId());
        assertEquals("PENDING", response.estado());
    }

    @Test
    @DisplayName("toResponse with null result throws IllegalStateException")
    void toResponse_nullResult_refusesToBuild() {
        assertThrows(IllegalStateException.class,
            () -> ProponerAccionToolMapper.toResponse(null));
    }
}
