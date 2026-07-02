package com.ar.crm2.application.ai.json;

/**
 * JSON number — the raw lexical token as it appeared in the source
 * document (e.g. {@code "50000.00"}, {@code "75"}, {@code "-3.14e2"}).
 *
 * <p>The value is preserved as a {@code String} rather than a parsed
 * {@code BigDecimal}/{@code Long}/{@code Double} so the mapper can
 * apply its own precision policy when coercing to UUIDs, integers,
 * decimals, dates, etc. Parsing into a numeric type too early would
 * silently lose precision on very large or very small numbers.
 */
public record JsonNumber(String raw) implements JsonValue {

    public JsonNumber {
        if (raw == null || raw.isEmpty()) {
            throw new IllegalArgumentException("JsonNumber.raw must be a non-empty string");
        }
    }

    @Override
    public String toJsonString() {
        return raw;
    }
}