package com.ar.crm2.exception;

/**
 * Exception thrown when a state transition on an Empresa violates business rules.
 */
public class EmpresaStateTransitionException extends DomainException {

    public EmpresaStateTransitionException(String message) {
        super(message);
    }

    public EmpresaStateTransitionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Build "No se puede volver a Prospecto desde el estado {estadoActual}."
     */
    public static EmpresaStateTransitionException transicionAProspectoNoPermitida(String estadoActual) {
        return new EmpresaStateTransitionException(
                "No se puede volver a Prospecto desde el estado " + estadoActual + "."
        );
    }

    /**
     * Build "No se puede marcar como inactivo una empresa con tratos activos."
     */
    public static EmpresaStateTransitionException inactivoConTratosActivos() {
        return new EmpresaStateTransitionException(
                "No se puede marcar como inactivo una empresa con tratos activos."
        );
    }
}