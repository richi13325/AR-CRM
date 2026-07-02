package com.ar.crm2.application.ai.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Command-level invariants for {@link ConfirmarAccionCommand}.
 *
 * <p><b>PR6 — resource-first tenant contract:</b> {@code empresaId}
 * is REQUIRED at the command boundary. The strict cross-check
 * between the request's {@code empresaId} and the resource's
 * {@code accion.empresaId} cannot be skipped by omitting the
 * parameter; the controller already requires it as a
 * {@code @RequestParam}, and the command must enforce the same
 * invariant so that no application-service code path can short-circuit
 * the cross-check.
 */
class ConfirmarAccionCommandTest {

    @Test
    @DisplayName("ConfirmarAccionCommand rechaza null empresaId con mensaje controlado")
    void confirmar_empresaIdNull_rechaza() {
        // PR6 contract: empresaId is mandatory at the command boundary so
        // the service layer never executes the cross-check against null.
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new ConfirmarAccionCommand(
                        UUID.randomUUID(),  // actorUsuarioId
                        UUID.randomUUID(),  // accionId
                        1,                  // expectedVersion
                        null                // empresaId — MUST be rejected
                )
        );
        assertEquals("empresaId is required", ex.getMessage());
    }

    @Test
    @DisplayName("ConfirmarAccionCommand acepta empresaId valido (caso positivo de triangulacion)")
    void confirmar_empresaIdValido_creaComando() {
        // Triangulation: a positive case proves the constructor did not
        // become over-strict and that valid empresaIds still flow through.
        UUID empresaId = UUID.randomUUID();
        ConfirmarAccionCommand cmd = new ConfirmarAccionCommand(
                UUID.randomUUID(), UUID.randomUUID(), 1, empresaId
        );
        assertNotNull(cmd);
        assertEquals(empresaId, cmd.empresaId());
    }

    @Test
    @DisplayName("ConfirmarAccionCommand sigue rechazando null actorUsuarioId (regresion de invariantes previos)")
    void confirmar_actorUsuarioIdNull_rechaza() {
        // Regression: prior invariants MUST still hold; the new empresaId
        // validation must not displace existing validations.
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new ConfirmarAccionCommand(
                        null, UUID.randomUUID(), 1, UUID.randomUUID()
                )
        );
        assertEquals("actorUsuarioId is required", ex.getMessage());
    }
}