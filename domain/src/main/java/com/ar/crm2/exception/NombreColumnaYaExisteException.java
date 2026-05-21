package com.ar.crm2.exception;

/**
 * Exception thrown when attempting to create a column
 * whose name already exists in the system.
 */
public class NombreColumnaYaExisteException extends DomainException {

    public NombreColumnaYaExisteException() {
        super("Ya existe una columna con ese nombre.");
    }

    public NombreColumnaYaExisteException(String message) {
        super(message);
    }

    public NombreColumnaYaExisteException(String message, Throwable cause) {
        super(message, cause);
    }
}