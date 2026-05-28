package com.ar.crm2.shared;

import com.ar.crm2.exception.InvalidEmailFormatException;
import com.ar.crm2.exception.InvalidUrlFormatException;
import com.ar.crm2.exception.InvariantViolationException;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Reusable domain assertions.
 * All methods throw InvariantViolationException to enforce invariants at creation time.
 */
public final class DomainAssert {

    private DomainAssert() {
    }

    // (?!.*\.\.) — no consecutive dots anywhere (applies to local part)
    // [^\s@]+ — local part: non-empty, no whitespace, no @
    // @ — exactly one @
    // domain: each label starts/ends alphanumeric, hyphens allowed in middle only
    // (?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\.)+ — at least one dot, 1+ labels
    // [A-Za-z]{2,10} — TLD: alphabetic, 2–10 chars
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^(?!.*\\.\\.)[^@\\s]+@(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z]{2,10}$"
    );

    // protocol required, domain: labels alphanumeric at start/end, hyphens only in middle
    // at least one dot, TLD 2-10 alphabetic chars
    // optional port, optional path/query/fragment
    // rejects whitespace, no trailing dot in domain
    private static final Pattern LINK_PATTERN = Pattern.compile(
        "^https?://(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z]{2,10}(:\\d+)?(?:\\/[^\\s]*)?$"
    );

    /**
     * Validates that value is not null.
     * @throws InvariantViolationException with Spanish "El campo {fieldName} es obligatorio."
     */
    public static <T> T notNull(T value, String fieldName) {
        if (value == null) {
            throw InvariantViolationException.required(fieldName);
        }
        return value;
    }

    // Keep full-message overload for existing call sites and specific contexts
    public static <T> T notNull(T value, String message, boolean _unused) {
        if (value == null) {
            throw new InvariantViolationException(message);
        }
        return value;
    }

    /**
     * Validates that two values are equal (same).
     * @throws InvariantViolationException with Spanish "El campo {fieldName} debe ser igual a {expected}."
     */
    public static <T> T sameAs(T actual, T expected, String fieldName) {
        notNull(actual, fieldName);
        notNull(expected, fieldName);
        if (!Objects.equals(actual, expected)) {
            throw InvariantViolationException.mustBeEqual(fieldName, expected);
        }
        return actual;
    }

    public static <T> T sameAs(T actual, T expected, String message, boolean _unused) {
        notNull(actual, message, true);
        notNull(expected, message, true);
        if (!Objects.equals(actual, expected)) {
            throw new InvariantViolationException(message);
        }
        return actual;
    }

    /**
     * Validates that a string is not blank (null, empty, or whitespace-only).
     * @throws InvariantViolationException with Spanish "El campo {fieldName} no puede estar en blanco."
     */
    public static String notBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw InvariantViolationException.blank(fieldName);
        }
        return value.trim();
    }

    public static String notBlank(String value, String message, boolean _unused) {
        if (value == null || value.isBlank()) {
            throw new InvariantViolationException(message);
        }
        return value.trim();
    }

    /**
     * Validates that a string length is between min and max (inclusive).
     * Rejects null/blank, trims the value, and validates the normalized length.
     * @throws InvariantViolationException with Spanish "El campo {fieldName} debe tener entre {min} y {max} caracteres."
     */
    public static String lengthBetween(String value, String fieldName, int min, int max) {
        String normalized = notBlank(value, fieldName);
        if (normalized.length() < min || normalized.length() > max) {
            throw InvariantViolationException.lengthOutsideRange(fieldName, min, max);
        }
        return normalized;
    }

    public static String lengthBetween(String value, int min, int max, String message) {
        String normalized = notBlank(value, message, true);
        if (normalized.length() < min || normalized.length() > max) {
            throw new InvariantViolationException(message);
        }
        return normalized;
    }

    /**
     * Validates an optional string field — normalizes blank to null, then checks max length.
     * Returns null for null/blank input; otherwise returns trimmed value capped at maxLen.
     * @throws InvariantViolationException if value is non-blank and exceeds maxLen
     */
    public static String optionalLength(String value, int maxLen, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > maxLen) {
            throw InvariantViolationException.lengthOutsideRange(fieldName, 0, maxLen);
        }
        return trimmed;
    }

    /**
     * Validates email format and length (1–150 chars).
     * <p>
     * normalization: trims whitespace only — does NOT lowercase.
     * Lowercasing is intentionally skipped because:
     * <ul>
     *   <li>Email local parts ARE case-sensitive by RFC 5321</li>
     *   <li>Many providers treat them case-insensitively, but normalizing could break
     *       accounts where a user legitimately uses mixed case</li>
     *   <li>Domain part is case-insensitive in DNS, but preserving original case is safer</li>
     * </ul>
     *
     * @param fieldName used to generate Spanish error message
     * @throws InvalidEmailFormatException on any format or length violation
     */
    public static String email(String value, String fieldName) {
        String normalized = notBlank(value, fieldName);
        if (normalized.length() > 150) {
            throw InvalidEmailFormatException.tooLong(fieldName);
        }
        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw InvalidEmailFormatException.malformed(fieldName);
        }
        return normalized;
    }

    /**
     * Full-message overload kept for specific custom messages.
     * @deprecated use {@link #email(String, String)} for field-name based validation
     */
    @Deprecated
    public static String email(String value, String message, boolean _unused) {
        String normalized = notBlank(value, message, true);
        if (normalized.length() > 150) {
            throw new InvalidEmailFormatException(message);
        }
        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new InvalidEmailFormatException(message);
        }
        return normalized;
    }

    /**
     * Validates a URL/link format.
     * <p>
     * normalization: trims whitespace only — does NOT lowercase.
     * Protocol (http:// or https://) is required.
     * Domain must have labels separated by dots, each label alphanumeric at start/end,
     * hyphens allowed only in the middle. TLD must be alphabetic, 2–10 chars.
     * Optional port (e.g. :8080) and optional path/query/fragment are allowed.
     * Whitespace is rejected.
     *
     * @param fieldName used to generate Spanish error message
     * @throws InvalidUrlFormatException on format violation
     */
    public static String link(String value, String fieldName) {
        String normalized = notBlank(value, fieldName);
        if (!LINK_PATTERN.matcher(normalized).matches()) {
            throw InvalidUrlFormatException.malformed(fieldName);
        }
        return normalized;
    }

    /**
     * Full-message overload kept for specific custom messages.
     * @deprecated use {@link #link(String, String)} for field-name based validation
     */
    @Deprecated
    public static String link(String value, String message, boolean _unused) {
        String normalized = notBlank(value, message, true);
        if (!LINK_PATTERN.matcher(normalized).matches()) {
            throw new InvalidUrlFormatException(message);
        }
        return normalized;
    }
}