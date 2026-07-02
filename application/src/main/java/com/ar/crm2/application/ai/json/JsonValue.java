package com.ar.crm2.application.ai.json;

/**
 * Root of the small JSON value hierarchy used by the AI action mapper.
 *
 * <p>The AI assistant emits a JSON document describing the action payload.
 * The application boundary parses that document and converts it into a
 * typed CRM command. We only ship a minimal, dependency-free parser
 * (no Jackson, no Gson) because:
 * <ul>
 *   <li>Jackson is not in the {@code application} module's dependency graph.</li>
 *   <li>The contract surface is flat strings / numbers / booleans / null at the
 *       boundary; the mapper only needs to read top-level fields.</li>
 *   <li>Bringing a JSON library would violate the project rule that the
 *       application layer depends on {@code domain} only.</li>
 * </ul>
 *
 * <p>The hierarchy is sealed so future code cannot add new value shapes
 * without an explicit design decision. All concrete types live in this
 * package.
 */
public sealed interface JsonValue
        permits JsonObject, JsonArray, JsonString, JsonNumber, JsonBoolean, JsonNull {

    /**
     * Returns {@code true} when this value is the {@link JsonNull} literal.
     */
    default boolean isNull() {
        return this instanceof JsonNull;
    }

    /**
     * Returns the JSON serialization of this value (canonical form).
     * Used only for diagnostics — the mapper never round-trips through
     * this method.
     */
    String toJsonString();
}