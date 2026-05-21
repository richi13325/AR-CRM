package com.ar.crm2.exception;

/**
 * Exception thrown when an action is attempted by a user
 * lacking the required system privileges (e.g., only SuperUsuario can perform it).
 */
public class UnauthorizedDomainActionException extends DomainException {

    public UnauthorizedDomainActionException(String action) {
        super("Solo un SuperUsuario puede " + action + ".");
    }

    public UnauthorizedDomainActionException(String message, Throwable cause) {
        super(message, cause);
    }
}