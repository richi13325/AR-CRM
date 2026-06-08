package com.ar.crm2.exception;

/**
 * Exception thrown when an Etiqueta color fails domain-level format validation.
 * The expected format is a 7-character hex string starting with '#' (e.g. "#FF0000").
 */
public class InvalidColorFormatException extends DomainException {

    public InvalidColorFormatException() {
        super("El color debe tener un formato hexadecimal válido (por ejemplo, #FF0000).");
    }

    public InvalidColorFormatException(String message) {
        super(message);
    }

    public InvalidColorFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public static InvalidColorFormatException required() {
        return new InvalidColorFormatException("El campo color es obligatorio.");
    }

    public static InvalidColorFormatException malformed() {
        return new InvalidColorFormatException(
            "El color debe tener un formato hexadecimal válido de 7 caracteres (por ejemplo, #FF0000)."
        );
    }
}
