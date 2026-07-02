package com.ar.crm2.application.ai.json;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * JSON object — an ordered map of field names to {@link JsonValue}s.
 *
 * <p>Field iteration order is preserved (uses {@link LinkedHashMap}) so
 * error messages can mention the order in which the parser encountered
 * fields. {@link #get(String)} returns {@code null} when the field is
 * absent — callers must distinguish "absent" from "present with null
 * value" via {@link JsonValue#isNull()}.
 */
public record JsonObject(Map<String, JsonValue> entries) implements JsonValue {

    public JsonObject {
        Objects.requireNonNull(entries, "entries");
        // Defensive unmodifiable wrapper around the LinkedHashMap so the
        // record stays immutable AND iteration order is preserved.
        // (Map.copyOf does NOT guarantee iteration order.)
        if (!(entries instanceof LinkedHashMap)) {
            Map<String, JsonValue> ordered = new LinkedHashMap<>(entries);
            entries = Collections.unmodifiableMap(ordered);
        } else {
            entries = Collections.unmodifiableMap(entries);
        }
    }

    /**
     * Returns the value associated with {@code field}, or {@code null}
     * when the field is absent. The returned value may be {@link JsonNull}
     * when the JSON document had an explicit {@code null} literal for
     * the field — callers that want to treat both cases the same should
     * use {@link #isNullOrMissing(String)}.
     */
    public JsonValue get(String field) {
        return entries.get(field);
    }

    /**
     * Returns {@code true} when the field is present in the object,
     * regardless of whether its value is {@link JsonNull}. For the
     * "absent or null" idiom see {@link #isNullOrMissing(String)}.
     */
    public boolean contains(String field) {
        return entries.containsKey(field);
    }

    /**
     * Returns {@code true} when the field is absent OR present with a
     * {@link JsonNull} value.
     */
    public boolean isNullOrMissing(String field) {
        JsonValue v = entries.get(field);
        return v == null || v.isNull();
    }

    @Override
    public String toJsonString() {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, JsonValue> e : entries.entrySet()) {
            if (!first) sb.append(',');
            sb.append('"').append(escape(e.getKey())).append('"').append(':');
            sb.append(e.getValue().toJsonString());
            first = false;
        }
        return sb.append('}').toString();
    }

    private static String escape(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 2);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }
}