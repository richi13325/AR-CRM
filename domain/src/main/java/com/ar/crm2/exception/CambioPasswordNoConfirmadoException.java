package com.ar.crm2.exception;

/**
 * Exception thrown when a password change is attempted
 * without the required email confirmation code.
 */
public class CambioPasswordNoConfirmadoException extends DomainException {

    public CambioPasswordNoConfirmadoException() {
        super("No se puede cambiar la contraseña sin confirmación del correo.");
    }

    public CambioPasswordNoConfirmadoException(String message, Throwable cause) {
        super(message, cause);
    }
}