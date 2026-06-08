package com.ar.crm2.adapter.in.rest;

import com.ar.crm2.application.columna.exception.ColumnaHasAssociatedFichasException;
import com.ar.crm2.application.columna.exception.ColumnaNotFoundException;
import com.ar.crm2.application.etiqueta.exception.EtiquetaNotFoundException;
import com.ar.crm2.application.etiqueta.exception.EtiquetaRequiresConfirmationException;
import com.ar.crm2.application.identity.model.IdentityProvisioningException;
import com.ar.crm2.application.tablero.exception.TableroNotFoundException;
import com.ar.crm2.exception.ColumnaConFichasNoPuedeEliminarseException;
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