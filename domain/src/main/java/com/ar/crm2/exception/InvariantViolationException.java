package com.ar.crm2.exception;

/**
 * Exception thrown when a domain invariant is violated.
 * Represents business rule violations, NOT application errors.
 */
public class InvariantViolationException extends RuntimeException {

    public InvariantViolationException(String message) {
        super(message);
    }

    public InvariantViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}