package com.ar.crm2.application.ai.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Command-level invariants for {@link ObtenerConversacionAsistenteCommand}.
 *
 * <p><b>PR6 — resource-first tenant contract:</b> {@code empresaId}
 * is REQUIRED at the command boundary. The strict cross-check
 * between the request's {@code empresaId} and the resource's
 * {@code conversacion.empresaId} cannot be skipped by omitting the
 * parameter; the controller already requires it as a
 * {@code @RequestParam}, and the command must enforce the same
 * invariant so that no application-service code path can short-circuit
 * the cross-check.
 */
class ObtenerConversacionAsistenteCommandTest {

    @Test
    @DisplayName("ObtenerConversacionAsistenteCommand rechaza null empresaId con mensaje controlado")
    void obtener_empresaIdNull_rechaza() {
        // PR6 contract: empresaId is mandatory at the command boundary so
        // the service layer never executes the cross-check against null.
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new ObtenerConversacionAsistenteCommand(
                        UUID.randomUUID(),  // actorUsuarioId
                        UUID.randomUUID(),  // aiConversacionId
                        null                // empresaId — MUST be rejected
                )
        );
        assertEquals("empresaId is required", ex.getMessage());
    }

    @Test
    @DisplayName("ObtenerConversacionAsistenteCommand acepta empresaId valido (caso positivo de triangulacion)")
    void obtener_empresaIdValido_creaComando() {
        // Triangulation: a positive case proves the constructor did not
        // become over-strict and that valid empresaIds still flow through.
        UUID empresaId = UUID.randomUUID();
        ObtenerConversacionAsistenteCommand cmd = new ObtenerConversacionAsistenteCommand(
                UUID.randomUUID(), UUID.randomUUID(), empresaId
        );
        assertNotNull(cmd);
        assertEquals(empresaId, cmd.empresaId());
    }

    @Test
    @DisplayName("ObtenerConversacionAsistenteCommand sigue rechazando null aiConversacionId (regresion de invariantes previos)")
    void obtener_aiConversacionIdNull_rechaza() {
        // Regression: prior invariants MUST still hold; the new empresaId
        // validation must not displace existing validations.
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new ObtenerConversacionAsistenteCommand(
                        UUID.randomUUID(), null, UUID.randomUUID()
                )
        );
        assertEquals("aiConversacionId is required", ex.getMessage());
    }
}