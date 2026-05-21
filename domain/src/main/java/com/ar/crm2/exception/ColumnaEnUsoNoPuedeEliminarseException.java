package com.ar.crm2.exception;

/**
 * Exception thrown when a column cannot be definitively deleted because it is
 * currently in use by one or more boards.
 * This is a business rule violation, distinct from {@link ColumnaConFichasNoPuedeEliminarseException}
 * which concerns fichas inside the column.
 */
public class ColumnaEnUsoNoPuedeEliminarseException extends DomainException {

    public ColumnaEnUsoNoPuedeEliminarseException() {
        super("La columna no puede eliminarse porque está en uso en un tablero.");
    }

    public ColumnaEnUsoNoPuedeEliminarseException(String message) {
        super(message);
    }

    public ColumnaEnUsoNoPuedeEliminarseException(String message, Throwable cause) {
        super(message, cause);
    }
}