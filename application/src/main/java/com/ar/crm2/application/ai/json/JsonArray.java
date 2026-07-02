package com.ar.crm2.application.ai.json;

import java.util.List;
import java.util.Objects;

/**
 * JSON array — an ordered list of {@link JsonValue}s.
 *
 * <p>Not used by the current AI action payload contract (every
 * discriminator is a flat object), but exposed so the parser handles
 * unexpected array payloads gracefully — the mapper rejects them with
 * a controlled {@code AccionInvalidaException} instead of leaking
 * low-level parser noise.
 */
public record JsonArray(List<JsonValue> elements) implements JsonValue {

    public JsonArray {
        Objects.requireNonNull(elements, "elements");
        elements = List.copyOf(elements);
    }

    @Override
    public String toJsonString() {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (JsonValue v : elements) {
            if (!first) sb.append(',');
            sb.append(v.toJsonString());
            first = false;
        }
        return sb.append(']').toString();
    }
}