package com.ar.crm2.exception;

/**
 * Exception thrown when an attempt is made to create an Etiqueta whose
 * (nombre, tipoEtiqueta) pair collides with an existing Etiqueta.
 */
public class DuplicateEtiquetaNameException extends DomainException {

    public DuplicateEtiquetaNameException() {
        super("Ya existe una etiqueta con el mismo nombre y tipo.");
    }

    public DuplicateEtiquetaNameException(String message) {
        super(message);
    }

    public DuplicateEtiquetaNameException(String message, Throwable cause) {
        super(message, cause);
    }
}
