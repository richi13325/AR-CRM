package com.ar.crm2.application.ai.json;

/**
 * JSON {@code null} literal.
 *
 * <p>Modeled as a singleton record — there is exactly one null value,
 * so {@link #INSTANCE} is the canonical instance.
 */
public record JsonNull() implements JsonValue {

    public static final JsonNull INSTANCE = new JsonNull();

    @Override
    public String toJsonString() {
        return "null";
    }
}