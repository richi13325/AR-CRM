package com.ar.crm2.exception;

/**
 * Exception thrown when a URL/link fails domain-level format validation.
 * Does not check DNS existence or reachability — only structural validity.
 */
public class InvalidUrlFormatException extends DomainException {

    public InvalidUrlFormatException() {
        super("La URL debe tener un formato válido.");
    }

    public InvalidUrlFormatException(String message) {
        super(message);
    }

    public InvalidUrlFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    // --- Field-name based factories (Spanish messages generated internally) ---

    public static InvalidUrlFormatException required(String fieldName) {
        return new InvalidUrlFormatException("El campo " + fieldName + " es obligatorio.");
    }

    public static InvalidUrlFormatException malformed(String fieldName) {
        return new InvalidUrlFormatException(
                "El campo " + fieldName + " tiene un formato de URL inválido.");
    }
}
