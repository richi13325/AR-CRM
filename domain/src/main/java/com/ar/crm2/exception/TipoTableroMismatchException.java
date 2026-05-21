package com.ar.crm2.exception;

/**
 * Exception thrown when a column's TipoTablero does not match
 * the board's TipoTablero.
 */
public class TipoTableroMismatchException extends DomainException {

    public TipoTableroMismatchException() {
        super("La columna debe corresponder al mismo tipo de tablero.");
    }

    public TipoTableroMismatchException(String message) {
        super(message);
    }

    public TipoTableroMismatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
