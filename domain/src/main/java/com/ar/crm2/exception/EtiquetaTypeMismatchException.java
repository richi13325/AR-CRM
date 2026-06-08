package com.ar.crm2.exception;

/**
 * Exception thrown when a Ficha attempts to be associated with an Etiqueta
 * whose TipoEtiqueta does not match the Ficha's TipoFicha.
 */
public class EtiquetaTypeMismatchException extends DomainException {

    public EtiquetaTypeMismatchException() {
        super("La etiqueta debe corresponder al mismo tipo que la ficha.");
    }

    public EtiquetaTypeMismatchException(String message) {
        super(message);
    }

    public EtiquetaTypeMismatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
