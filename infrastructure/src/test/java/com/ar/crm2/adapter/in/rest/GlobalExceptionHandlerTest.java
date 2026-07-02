package com.ar.crm2.adapter.in.rest;

import com.ar.crm2.application.ai.exception.AsistenteTenantException;
import com.ar.crm2.application.ai.exception.AiAssistantException;
import com.ar.crm2.application.ai.exception.ConversacionAsistenteNoEncontradaException;
import com.ar.crm2.application.columna.exception.ColumnaHasAssociatedFichasException;
import com.ar.crm2.application.columna.exception.ColumnaNotFoundException;
import com.ar.crm2.application.etiqueta.exception.EtiquetaNotFoundException;
import com.ar.crm2.application.etiqueta.exception.EtiquetaRequiresConfirmationException;
import com.ar.crm2.application.identity.model.IdentityProvisioningException;
import com.ar.crm2.application.tablero.exception.TableroNotFoundException;
import com.ar.crm2.exception.AccionExpiredException;
import com.ar.crm2.exception.AccionNotFoundException;
import com.ar.crm2.exception.AccionNotOwnedByActorException;
import com.ar.crm2.exception.AccionStateException;
import com.ar.crm2.exception.AccionStateTransitionException;
import com.ar.crm2.exception.AccionVersionMismatchException;
import com.ar.crm2.exception.ColumnaConFichasNoPuedeEliminarseException;
import com.ar.crm2.exception.ColumnaYaExisteEnTableroException;
import com.ar.crm2.exception.ConversacionAsistenteNotOwnedByActorException;
import com.ar.crm2.exception.DomainException;
import com.ar.crm2.exception.InvariantViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GlobalExceptionHandler.
 * Verifies that domain exceptions are translated to correct HTTP status codes and bodies.
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    // ── 404 Not Found ──────────────────────────────────────────────

    @Test
    void handleColumnaNotFoundException_shouldReturn404() {
        UUID id = UUID.randomUUID();
        ColumnaNotFoundException ex = ColumnaNotFoundException.forId(id);

        ResponseEntity<Map<String, String>> response = handler.handleColumnaNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").contains(id.toString()));
    }

    @Test
    void handleTableroNotFoundException_shouldReturn404() {
        UUID id = UUID.randomUUID();
        TableroNotFoundException ex = TableroNotFoundException.forId(id);

        ResponseEntity<Map<String, String>> response = handler.handleTableroNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").contains(id.toString()));
    }

    @Test
    void handleEtiquetaNotFoundException_shouldReturn404() {
        UUID id = UUID.randomUUID();
        EtiquetaNotFoundException ex = EtiquetaNotFoundException.forId(id);

        ResponseEntity<Map<String, String>> response = handler.handleEtiquetaNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").contains(id.toString()));
    }

    @Test
    void handleEtiquetaRequiresConfirmationException_shouldReturn409() {
        EtiquetaRequiresConfirmationException ex = new EtiquetaRequiresConfirmationException();

        ResponseEntity<Map<String, String>> response = handler.handleEtiquetaRequiresConfirmationException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().get("error"));
    }

    // ── 409 Conflict ───────────────────────────────────────────────

    @Test
    void handleColumnaHasAssociatedFichasException_shouldReturn409() {
        UUID id = UUID.randomUUID();
        ColumnaHasAssociatedFichasException ex = ColumnaHasAssociatedFichasException.forId(id);

        ResponseEntity<Map<String, String>> response = handler.handleColumnaHasAssociatedFichasException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").contains(id.toString()));
    }

    @Test
    void handleColumnaConFichasNoPuedeEliminarse_shouldReturn409() {
        ColumnaConFichasNoPuedeEliminarseException ex =
                new ColumnaConFichasNoPuedeEliminarseException();

        ResponseEntity<Map<String, String>> response =
                handler.handleColumnaConFichasNoPuedeEliminarse(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void handleColumnaYaExisteEnTablero_shouldReturn409() {
        // Regression: POST /api/tableros/asignar-columna throws this
        // domain exception when the column is already assigned. It must
        // map to 409 Conflict, not the generic DomainException 400.
        ColumnaYaExisteEnTableroException ex = new ColumnaYaExisteEnTableroException();

        ResponseEntity<Map<String, String>> response = handler.handleColumnaYaExisteEnTablero(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode(),
            "ColumnaYaExisteEnTableroException must map to 409 Conflict");
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("error"));
    }

    // ── 400 Bad Request ────────────────────────────────────────────

    @Test
    void handleDomainException_shouldReturn400() {
        DomainException ex = new InvariantViolationException("Some invariant was violated");

        ResponseEntity<Map<String, String>> response = handler.handleDomainException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Some invariant was violated", response.getBody().get("error"));
    }

    @Test
    void handleIllegalArgument_shouldReturn400() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid UUID format");

        ResponseEntity<Map<String, String>> response = handler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid UUID format", response.getBody().get("error"));
    }

    // ── IdentityProvisioningException (Keycloak) ─────────────────────

    @Test
    void handleIdentityProvisioningException_connectionFailure_returns502() {
        IdentityProvisioningException ex = new IdentityProvisioningException(
                "Connection refused",
                IdentityProvisioningException.Reason.CONNECTION_FAILURE
        );

        ResponseEntity<Map<String, String>> response = handler.handleIdentityProvisioningException(ex);

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").contains("Connection refused"));
    }

    @Test
    void handleIdentityProvisioningException_userAlreadyExists_returns409() {
        IdentityProvisioningException ex = new IdentityProvisioningException(
                "kc-123",
                "User already exists",
                IdentityProvisioningException.Reason.USER_ALREADY_EXISTS
        );

        ResponseEntity<Map<String, String>> response = handler.handleIdentityProvisioningException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void handleIdentityProvisioningException_userNotFound_returns404() {
        IdentityProvisioningException ex = new IdentityProvisioningException(
                "kc-456",
                "User not found",
                IdentityProvisioningException.Reason.USER_NOT_FOUND
        );

        ResponseEntity<Map<String, String>> response = handler.handleIdentityProvisioningException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ── AI Assistant (PR 4) ───────────────────────────────────────────
    // Verifies the new exception handlers added to GlobalExceptionHandler
    // during PR 4 map the AI-specific domain / application exceptions to
    // the HTTP status documented in the OpenSpec change.

    @Test
    void handleAccionNotFoundException_shouldReturn404() {
        UUID id = UUID.randomUUID();
        AccionNotFoundException ex = AccionNotFoundException.forId(id);

        ResponseEntity<Map<String, String>> response = handler.handleAccionNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").contains(id.toString()));
    }

    @Test
    void handleAccionStateException_shouldReturn409() {
        UUID id = UUID.randomUUID();
        AccionStateException ex = AccionStateException.invalidState(
                id.toString(), "confirmar", "CONFIRMED");

        ResponseEntity<Map<String, String>> response = handler.handleAccionStateException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").contains("CONFIRMED"));
    }

    @Test
    void handleAccionStateTransitionException_shouldReturn409() {
        AccionStateTransitionException ex = AccionStateTransitionException.transicionNoPermitida(
                "REJECTED", "confirmar");

        ResponseEntity<Map<String, String>> response = handler.handleAccionStateTransitionException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").contains("confirmar"));
    }

    @Test
    void handleAccionVersionMismatchException_shouldReturn409() {
        UUID id = UUID.randomUUID();
        AccionVersionMismatchException ex = AccionVersionMismatchException.mismatch(
                id.toString(), 1, 3);

        ResponseEntity<Map<String, String>> response = handler.handleAccionVersionMismatchException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void handleAccionExpiredException_shouldReturn409() {
        UUID id = UUID.randomUUID();
        AccionExpiredException ex = AccionExpiredException.expired(
                id.toString(), "2026-06-25T10:00:00");

        ResponseEntity<Map<String, String>> response = handler.handleAccionExpiredException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void handleAccionNotOwnedByActorException_shouldReturn403() {
        AccionNotOwnedByActorException ex = AccionNotOwnedByActorException.notRequester(
                "actor-1", "accion-1");

        ResponseEntity<Map<String, String>> response = handler.handleAccionNotOwnedByActorException(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void handleConversacionAsistenteNotOwnedByActorException_shouldReturn403() {
        ConversacionAsistenteNotOwnedByActorException ex = ConversacionAsistenteNotOwnedByActorException.tenantMismatch(
                "actor-1", "ai-conv-1");

        ResponseEntity<Map<String, String>> response = handler.handleConversacionAsistenteNotOwnedByActorException(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void handleConversacionAsistenteNoEncontradaException_shouldReturn404() {
        ConversacionAsistenteNoEncontradaException ex = ConversacionAsistenteNoEncontradaException.forId(
                "ai-conv-1");

        ResponseEntity<Map<String, String>> response = handler.handleConversacionAsistenteNoEncontradaException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void handleAsistenteTenantException_shouldReturn403() {
        AsistenteTenantException ex = AsistenteTenantException.empresaNoEncontradaParaActor("actor-1");

        ResponseEntity<Map<String, String>> response = handler.handleAsistenteTenantException(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    // ── 502 Bad Gateway — AI Assistant upstream failures ────────────
    //
    // Audit #3 closing test: AiAssistantException documents "Maps to HTTP
    // 502 Bad Gateway at the REST boundary" but the GlobalExceptionHandler
    // did NOT register a dedicated @ExceptionHandler for it. Pin the
    // new handler so the contract documented on the exception matches the
    // REST boundary behavior end-to-end.

    @Test
    void handleAiAssistantException_upstreamFailure_shouldReturn502() {
        // Pin: the upstreamFailure(...) factory message reaches the
        // response body so a client can see what failed.
        AiAssistantException ex = AiAssistantException.upstreamFailure(
            "ChatClient call failed: provider timeout"
        );

        ResponseEntity<Map<String, String>> response =
            handler.handleAiAssistantException(ex);

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode(),
            "AiAssistantException MUST map to 502 Bad Gateway — the exception "
                + "documents the boundary contract, and the global handler "
                + "must honor it (audit #3).");
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().get("error"),
            "the error body MUST carry the upstream failure reason so the "
                + "client can diagnose the cause");
        assertTrue(response.getBody().get("error")
                .contains("ChatClient call failed: provider timeout"),
            "the error body MUST carry the upstream failure reason verbatim; "
                + "got: " + response.getBody().get("error"));
    }

    @Test
    void handleAiAssistantException_invalidAssistantOutput_shouldReturn502() {
        AiAssistantException ex = AiAssistantException.invalidAssistantOutput(
            "ChatResponse output text is null"
        );

        ResponseEntity<Map<String, String>> response =
            handler.handleAiAssistantException(ex);

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode(),
            "AiAssistantException.invalidAssistantOutput(...) MUST also map to "
                + "502 — both factory methods describe a framework/marshaling "
                + "failure and the REST boundary contract is the same.");
    }

    @Test
    void handleAiAssistantException_preservesCause() {
        // The handler MUST preserve the cause so a future observability
        // hook (logging / metric) can introspect it. The body key is the
        // upstream reason; the exception's getCause() chain stays attached
        // for the failure annex.
        Throwable originalCause = new RuntimeException("openai provider 504");
        AiAssistantException ex = AiAssistantException.upstreamFailure(
            "ChatClient call failed", originalCause
        );

        ResponseEntity<Map<String, String>> response =
            handler.handleAiAssistantException(ex);

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        // Cause must stay attached on the exception we passed in (we do
        // NOT mutate it).
        assertSame(originalCause, ex.getCause(),
            "GlobalExceptionHandler must NOT strip or rewrap the cause — "
                + "the original RuntimeException must stay attached for "
                + "diagnostic logging by observability hooks");
    }

    // ── Error body structure ────────────────────────────────────────

    @Test
    void errorBody_shouldContainErrorKey() {
        ColumnaNotFoundException ex = ColumnaNotFoundException.forId(UUID.randomUUID());

        ResponseEntity<Map<String, String>> response = handler.handleColumnaNotFoundException(ex);

        Map<String, String> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("error"));
        assertNotNull(body.get("error"));
    }
}