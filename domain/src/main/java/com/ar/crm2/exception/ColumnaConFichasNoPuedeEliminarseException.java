package com.ar.crm2.exception;

/**
 * Exception thrown when a column cannot be removed because it contains fichas.
 * This is a business invariant violation, not an authorization issue.
 */
public class ColumnaConFichasNoPuedeEliminarseException extends DomainException {

    public ColumnaConFichasNoPuedeEliminarseException() {
        super("La columna no puede eliminarse porque contiene fichas.");
    }

    public ColumnaConFichasNoPuedeEliminarseException(String message) {
        super(message);
    }

    public ColumnaConFichasNoPuedeEliminarseException(String message, Throwable cause) {
        super(message, cause);
    }
}