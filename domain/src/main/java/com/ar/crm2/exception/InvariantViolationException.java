package com.ar.crm2.exception;

/**
 * Exception thrown when a domain invariant is violated.
 * Represents generic business rule violations used by DomainAssert.
 */
public class InvariantViolationException extends DomainException {

    public InvariantViolationException() {
        super("Se violó una invariante de dominio.");
    }

    public InvariantViolationException(String message) {
        super(message);
    }

    public InvariantViolationException(String message, Throwable cause) {
        super(message, cause);
    }

    // --- Field-name based factories (Spanish messages generated internally) ---

    /**
     * Build "El campo {fieldName} es obligatorio."
     */
    public static InvariantViolationException required(String fieldName) {
        return new InvariantViolationException("El campo " + fieldName + " es obligatorio.");
    }

    /**
     * Build "El campo {fieldName} debe ser igual a {expected}."
     */
    public static InvariantViolationException mustBeEqual(String fieldName, Object expected) {
        return new InvariantViolationException("El campo " + fieldName + " debe ser igual a " + expected + ".");
    }

    /**
     * Build "El campo {fieldName} no puede estar en blanco."
     */
    public static InvariantViolationException blank(String fieldName) {
        return new InvariantViolationException("El campo " + fieldName + " no puede estar en blanco.");
    }

    /**
     * Build "El campo {fieldName} debe tener entre {min} y {max} caracteres."
     */
    public static InvariantViolationException lengthOutsideRange(String fieldName, int min, int max) {
        return new InvariantViolationException(
                "El campo " + fieldName + " debe tener entre " + min + " y " + max + " caracteres.");
    }
}
