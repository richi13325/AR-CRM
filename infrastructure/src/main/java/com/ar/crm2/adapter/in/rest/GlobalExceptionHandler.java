package com.ar.crm2.adapter.in.rest;

import com.ar.crm2.application.agenda.exception.AgendaNotFoundException;
import com.ar.crm2.application.ai.exception.AiAssistantException;
import com.ar.crm2.application.ai.exception.AsistenteTenantException;
import com.ar.crm2.application.ai.exception.ConversacionAsistenteNoEncontradaException;
import com.ar.crm2.application.columna.exception.ColumnaHasAssociatedFichasException;
import com.ar.crm2.application.columna.exception.ColumnaNotFoundException;
import com.ar.crm2.application.contacto.exception.ContactoHasAssociatedTratosException;
import com.ar.crm2.application.contacto.exception.ContactoNotFoundException;
import com.ar.crm2.application.empresa.exception.EmpresaHasAssociatedTratosException;
import com.ar.crm2.application.empresa.exception.EmpresaNotFoundException;
import com.ar.crm2.application.etiqueta.exception.EtiquetaNotFoundException;
import com.ar.crm2.application.etiqueta.exception.EtiquetaRequiresConfirmationException;
import com.ar.crm2.application.ficha.exception.FichaNotFoundException;
import com.ar.crm2.application.identity.model.IdentityProvisioningException;
import com.ar.crm2.application.rol.exception.RolHasAssociatedUsuariosException;
import com.ar.crm2.application.rol.exception.RolNotFoundException;
import com.ar.crm2.application.security.exception.AuthenticatedUsuarioRequiredException;
import com.ar.crm2.application.superusuario.exception.SuperUsuarioNotFoundException;
import com.ar.crm2.application.tablero.exception.TableroNotFoundException;
import com.ar.crm2.application.tarea.exception.TareaNotFoundException;
import com.ar.crm2.application.trato.exception.TratoNotFoundException;
import com.ar.crm2.application.usuario.exception.UsuarioNotFoundException;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for REST adapters.
 * Translates domain exceptions to HTTP responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles RolNotFoundException as 404 Not Found.
     */
    @ExceptionHandler(RolNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleRolNotFoundException(RolNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(ColumnaNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleColumnaNotFoundException(ColumnaNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Handles UsuarioNotFoundException as 404 Not Found.
     */
    @ExceptionHandler(UsuarioNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUsuarioNotFoundException(UsuarioNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Handles SuperUsuarioNotFoundException as 404 Not Found.
     */
    @ExceptionHandler(SuperUsuarioNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleSuperUsuarioNotFoundException(SuperUsuarioNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Handles RolHasAssociatedUsuariosException as 409 Conflict.
     */
    @ExceptionHandler(RolHasAssociatedUsuariosException.class)
    public ResponseEntity<Map<String, String>> handleRolHasAssociatedUsuariosException(RolHasAssociatedUsuariosException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(ColumnaHasAssociatedFichasException.class)
    public ResponseEntity<Map<String, String>> handleColumnaHasAssociatedFichasException(ColumnaHasAssociatedFichasException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("error", ex.getMessage()));
    }

        /**
         * Handles EmpresaNotFoundException as 404 Not Found.
         */
    @ExceptionHandler(EmpresaNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleEmpresaNotFoundException(EmpresaNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Handles ContactoNotFoundException as 404 Not Found.
     */
    @ExceptionHandler(ContactoNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleContactoNotFoundException(ContactoNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Handles TratoNotFoundException as 404 Not Found.
     */
    @ExceptionHandler(TratoNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleTratoNotFoundException(TratoNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Handles TareaNotFoundException as 404 Not Found.
     */
    @ExceptionHandler(TareaNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleTareaNotFoundException(TareaNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Handles FichaNotFoundException as 404 Not Found.
     */
    @ExceptionHandler(FichaNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleFichaNotFoundException(FichaNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Handles EtiquetaNotFoundException as 404 Not Found.
     * When the exception aggregates missing ids (resolver path), the
     * response still uses 404 since the request asked for an entity the
     * catalog could not resolve.
     */
    @ExceptionHandler(EtiquetaNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleEtiquetaNotFoundException(EtiquetaNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Handles EtiquetaRequiresConfirmationException as 409 Conflict.
     * Caller must re-issue the delete with confirm=true to cascade.
     */
    @ExceptionHandler(EtiquetaRequiresConfirmationException.class)
    public ResponseEntity<Map<String, String>> handleEtiquetaRequiresConfirmationException(EtiquetaRequiresConfirmationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(AgendaNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleAgendaNotFoundException(AgendaNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Handles EmpresaHasAssociatedTratosException as 409 Conflict.
     */
    @ExceptionHandler(EmpresaHasAssociatedTratosException.class)
    public ResponseEntity<Map<String, String>> handleEmpresaHasAssociatedTratosException(EmpresaHasAssociatedTratosException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Handles ContactoHasAssociatedTratosException as 409 Conflict.
     */
    @ExceptionHandler(ContactoHasAssociatedTratosException.class)
    public ResponseEntity<Map<String, String>> handleContactoHasAssociatedTratosException(ContactoHasAssociatedTratosException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Handles TableroNotFoundException as 404 Not Found.
     */
    @ExceptionHandler(TableroNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleTableroNotFoundException(TableroNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Handles ColumnaConFichasNoPuedeEliminarseException as 409 Conflict.
     * This domain exception is thrown when a column deletion is attempted
     * on a column that contains fichas.
     */
    @ExceptionHandler(ColumnaConFichasNoPuedeEliminarseException.class)
    public ResponseEntity<Map<String, String>> handleColumnaConFichasNoPuedeEliminarse(ColumnaConFichasNoPuedeEliminarseException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Handles ColumnaYaExisteEnTableroException as 409 Conflict.
     * Thrown when the caller attempts to assign a Columna that is already
     * part of the target Tablero. Surfaced by
     * POST /api/tableros/asignar-columna; must NOT fall through to the
     * generic DomainException 400 handler (would hide the conflict
     * semantics and confuse clients).
     */
    @ExceptionHandler(ColumnaYaExisteEnTableroException.class)
    public ResponseEntity<Map<String, String>> handleColumnaYaExisteEnTablero(ColumnaYaExisteEnTableroException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Handles domain business rule violations as 400 Bad Request.
     * Covers DomainException and all subclasses (InvariantViolationException,
     * UnauthorizedDomainActionException, TipoTableroMismatchException, etc.).
     */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<Map<String, String>> handleDomainException(DomainException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Handles illegal argument exceptions as 400 Bad Request.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Handles @Valid body validation failures as 400 Bad Request, con el detalle
     * por campo, en el mismo formato {"error": ...} que el resto de respuestas.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + (fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalido"))
            .collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of("error", detalle.isBlank() ? "Datos invalidos" : detalle));
    }

    /**
     * Handles IllegalStateException as 502 Bad Gateway.
     * Used when an upstream integration (e.g. Evolution API) fails to return
     * the data we need, distinct from a client input error.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
            .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Handles AuthenticatedUsuarioRequiredException as 403 Forbidden.
     * This exception is thrown when the JWT token lacks the required usuario_id claim.
     */
    @ExceptionHandler(AuthenticatedUsuarioRequiredException.class)
    public ResponseEntity<Map<String, String>> handleAuthenticatedUsuarioRequiredException(AuthenticatedUsuarioRequiredException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Handles IdentityProvisioningException due to connection/auth issues as 502 Bad Gateway.
     */
    @ExceptionHandler(IdentityProvisioningException.class)
    public ResponseEntity<Map<String, String>> handleIdentityProvisioningException(IdentityProvisioningException ex) {
        HttpStatus status = switch (ex.getReason()) {
            case USER_ALREADY_EXISTS -> HttpStatus.CONFLICT;
            case USER_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONNECTION_FAILURE, AUTHENTICATION_FAILURE, SERVER_ERROR -> HttpStatus.BAD_GATEWAY;
        };
        return ResponseEntity.status(status)
            .body(Map.of("error", ex.getMessage()));
    }

    // ── AI Assistant (PR 4) ──────────────────────────────────────────
    // Each handler maps a domain or application-level exception thrown by
    // the AI assistant bounded context (AiController + ChatClient tool
    // surface) to the HTTP status code documented in
    // {@code openspec/changes/add-crm-ai-assistant-spring-ai/specs/ai-action-proposal/spec.md}
    // and {@code specs/ai-assistant/spec.md}.

    /**
     * Handles {@link AccionNotFoundException} as 404 Not Found.
     * The AI action proposal referenced by the REST path does not exist.
     */
    @ExceptionHandler(AccionNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleAccionNotFoundException(AccionNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Handles {@link AccionStateException} as 409 Conflict.
     * The proposal is in a state that does not allow the requested
     * transition (e.g. confirming an already CONFIRMED or EXPIRED action).
     */
    @ExceptionHandler(AccionStateException.class)
    public ResponseEntity<Map<String, String>> handleAccionStateException(AccionStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Handles {@link AccionStateTransitionException} as 409 Conflict.
     * Raised by the {@code AiAccion} domain state-machine methods when a
     * transition violates business rules.
     */
    @ExceptionHandler(AccionStateTransitionException.class)
    public ResponseEntity<Map<String, String>> handleAccionStateTransitionException(AccionStateTransitionException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Handles {@link AccionVersionMismatchException} as 409 Conflict.
     * The {@code expectedVersion} on the request does not match the
     * proposal's current optimistic-lock version.
     */
    @ExceptionHandler(AccionVersionMismatchException.class)
    public ResponseEntity<Map<String, String>> handleAccionVersionMismatchException(AccionVersionMismatchException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Handles {@link AccionExpiredException} as 409 Conflict.
     * The PENDING proposal passed its expiry time before the user
     * confirmed it.
     */
    @ExceptionHandler(AccionExpiredException.class)
    public ResponseEntity<Map<String, String>> handleAccionExpiredException(AccionExpiredException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Handles {@link AccionNotOwnedByActorException} as 403 Forbidden.
     * The actor is not the original requester of the AI action
     * proposal, or the proposal belongs to a different tenant.
     */
    @ExceptionHandler(AccionNotOwnedByActorException.class)
    public ResponseEntity<Map<String, String>> handleAccionNotOwnedByActorException(AccionNotOwnedByActorException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Handles {@link ConversacionAsistenteNotOwnedByActorException} as
     * 403 Forbidden. The actor is not the starter of the AI
     * conversation, or the conversation belongs to a different
     * tenant.
     */
    @ExceptionHandler(ConversacionAsistenteNotOwnedByActorException.class)
    public ResponseEntity<Map<String, String>> handleConversacionAsistenteNotOwnedByActorException(
            ConversacionAsistenteNotOwnedByActorException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Handles {@link ConversacionAsistenteNoEncontradaException} as 404
     * Not Found. The AI conversation referenced by the REST path does
     * not exist (or is not visible to the requester).
     */
    @ExceptionHandler(ConversacionAsistenteNoEncontradaException.class)
    public ResponseEntity<Map<String, String>> handleConversacionAsistenteNoEncontradaException(
            ConversacionAsistenteNoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Handles {@link AsistenteTenantException} as 403 Forbidden.
     * The actor does not own the tenant that owns the source WhatsApp
     * channel / AI resource they tried to operate on. Bridges the
     * application-level exception that wraps the neutral
     * {@code TenantScopeViolationException}.
     */
    @ExceptionHandler(AsistenteTenantException.class)
    public ResponseEntity<Map<String, String>> handleAsistenteTenantException(AsistenteTenantException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Handles {@link AiAssistantException} as 502 Bad Gateway.
     *
     * <p>The application-owned {@link AiAssistantException} documents
     * "Maps to HTTP 502 Bad Gateway at the REST boundary" on its
     * own Javadoc. Without this handler the exception would fall
     * through to a generic 500. The handler closes audit #3 by
     * honoring that boundary contract: 502 + the upstream failure
     * reason so a client can diagnose the cause without seeing the
     * framework types. The original cause is preserved on the
     * exception for any observability hook that introspects it
     * (logging / metrics).
     */
    @ExceptionHandler(AiAssistantException.class)
    public ResponseEntity<Map<String, String>> handleAiAssistantException(AiAssistantException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
            .body(Map.of("error", ex.getMessage()));
    }
}
