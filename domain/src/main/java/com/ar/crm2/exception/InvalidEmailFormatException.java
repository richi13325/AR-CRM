package com.ar.crm2.exception;

/**
 * Exception thrown when an email address fails domain-level format validation.
 * Does not check deliverability or DNS existence — only structural validity.
 */
public class InvalidEmailFormatException extends DomainException {

    public InvalidEmailFormatException() {
        super("El correo debe tener un formato válido.");
    }

    public InvalidEmailFormatException(String message) {
        super(message);
    }

    public InvalidEmailFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    // --- Field-name based factories (Spanish messages generated internally) ---

    public static InvalidEmailFormatException required(String fieldName) {
        return new InvalidEmailFormatException("El campo " + fieldName + " es obligatorio.");
    }

    public static InvalidEmailFormatException tooLong(String fieldName) {
        return new InvalidEmailFormatException(
                "El campo " + fieldName + " excede los 150 caracteres.");
    }

    public static InvalidEmailFormatException malformed(String fieldName) {
        return new InvalidEmailFormatException(
                "El campo " + fieldName + " tiene un formato de correo inválido.");
    }
}
