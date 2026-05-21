package com.ar.crm2.exception;

/**
 * Exception thrown when a column does not belong to the target board.
 * This is an invariant violation, not an authorization/superuser issue.
 */
public class ColumnaNoPerteneceAlTableroException extends DomainException {

    public ColumnaNoPerteneceAlTableroException() {
        super("La columna no pertenece a este tablero.");
    }

    public ColumnaNoPerteneceAlTableroException(String message) {
        super(message);
    }

    public ColumnaNoPerteneceAlTableroException(String message, Throwable cause) {
        super(message, cause);
    }
}