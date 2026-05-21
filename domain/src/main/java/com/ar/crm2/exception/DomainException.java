package com.ar.crm2.exception;

/**
 * Base exception for all domain-specific business rule violations.
 * Represents explicit domain rules being broken, not generic errors.
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}