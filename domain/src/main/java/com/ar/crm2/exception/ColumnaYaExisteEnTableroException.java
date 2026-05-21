package com.ar.crm2.exception;

/**
 * Exception thrown when attempting to add a column to a board
 * that already contains that column.
 */
public class ColumnaYaExisteEnTableroException extends DomainException {

    public ColumnaYaExisteEnTableroException() {
        super("La columna ya existe en este tablero.");
    }

    public ColumnaYaExisteEnTableroException(String message) {
        super(message);
    }

    public ColumnaYaExisteEnTableroException(String message, Throwable cause) {
        super(message, cause);
    }
}