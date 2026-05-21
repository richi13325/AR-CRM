package com.ar.crm2.exception;

/**
 * Exception thrown when a set of default columns is invalid
 * (wrong count, null elements, or wrong column type).
 */
public class InvalidDefaultColumnsException extends DomainException {

    public InvalidDefaultColumnsException() {
        super("Las columnas predeterminadas del tablero deben ser no nulas y PREDETERMINADAS.");
    }

    public InvalidDefaultColumnsException(int expectedCount) {
        super("El tablero debe tener " + expectedCount + " columnas predeterminadas.");
    }

    public InvalidDefaultColumnsException(String message) {
        super(message);
    }

    public InvalidDefaultColumnsException(String message, Throwable cause) {
        super(message, cause);
    }
}
