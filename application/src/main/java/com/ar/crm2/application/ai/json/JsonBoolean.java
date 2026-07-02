package com.ar.crm2.application.ai.json;

/**
 * JSON boolean literal ({@code true} or {@code false}).
 */
public record JsonBoolean(boolean value) implements JsonValue {

    @Override
    public String toJsonString() {
        return value ? "true" : "false";
    }
}