package com.ar.crm2.shared;

import com.ar.crm2.exception.InvariantViolationException;

import java.util.Objects;

/**
 * Reusable domain assertions.
 * All methods throw InvariantViolationException to enforce invariants at creation time.
 */
public final class DomainAssert {

    private DomainAssert() {
    }

    public static <T> T notNull(T value, String message) {
        if (value == null) {
            throw new InvariantViolationException(message);
        }
        return value;
    }

    public static <T> T sameAs(T actual, T expected, String message) {
        notNull(actual, message);
        notNull(expected, message);
        if (!Objects.equals(actual, expected)) {
            throw new InvariantViolationException(message);
        }
        return actual;
    }

    public static String notBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new InvariantViolationException(message);
        }
        return value.trim();
    }

    /**
     * Validates that a string length is between min and max (inclusive).
     * Rejects null/blank, trims the value, and validates the normalized length.
     */
    public static String lengthBetween(String value, int min, int max, String message) {
        String normalized = notBlank(value, message);
        if (normalized.length() < min || normalized.length() > max) {
            throw new InvariantViolationException(message);
        }
        return normalized;
    }
}