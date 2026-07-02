package com.ar.crm2.application.ai.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Command-level invariants for
 * {@link ListarAccionesPendientesCommand}.
 *
 * <p><b>User-approved PR7 contract:</b> {@code empresaId} is REQUIRED
 * at the command boundary. The system MUST NOT auto-resolve a single
 * owned company and MUST NOT guess or fall back. The controller
 * already rejects missing {@code empresaId} at the REST DTO layer
 * (via {@code @NotNull}), and the command enforces the same invariant
 * so that no application-service code path can short-circuit it.
 */
class ListarAccionesPendientesCommandTest {

    @Test
    @DisplayName("ListarAccionesPendientesCommand rechaza null empresaId con mensaje controlado")
    void listar_empresaIdNull_rechaza() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new ListarAccionesPendientesCommand(
                        UUID.randomUUID(),  // actorUsuarioId
                        null,               // empresaId — MUST be rejected
                        50                  // limite
                )
        );
        assertEquals("empresaId is required", ex.getMessage());
    }

    @Test
    @DisplayName("ListarAccionesPendientesCommand acepta empresaId valido (caso positivo de triangulacion)")
    void listar_empresaIdValido_creaComando() {
        // Triangulation: a positive case proves the constructor did not
        // become over-strict and that valid empresaIds still flow through.
        UUID empresaId = UUID.randomUUID();
        ListarAccionesPendientesCommand cmd = new ListarAccionesPendientesCommand(
                UUID.randomUUID(), empresaId, 50
        );
        assertNotNull(cmd);
        assertEquals(empresaId, cmd.empresaId());
        assertEquals(50, cmd.limite());
    }

    @Test
    @DisplayName("ListarAccionesPendientesCommand sigue rechazando null actorUsuarioId (regresion de invariantes previos)")
    void listar_actorUsuarioIdNull_rechaza() {
        // Regression: prior invariants MUST still hold; the new empresaId
        // validation must not displace existing validations.
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new ListarAccionesPendientesCommand(
                        null, UUID.randomUUID(), 50
                )
        );
        assertEquals("actorUsuarioId is required", ex.getMessage());
    }

    @Test
    @DisplayName("ListarAccionesPendientesCommand rechaza limite fuera del rango permitido")
    void listar_limiteFueraDeRango_rechaza() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new ListarAccionesPendientesCommand(
                        UUID.randomUUID(), UUID.randomUUID(), 0
                )
        );
        assertEquals("limite must be between 1 and 200", ex.getMessage());
    }
}
