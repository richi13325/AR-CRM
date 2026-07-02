package com.ar.crm2.application.ai.service;

import com.ar.crm2.application.ai.exception.AccionInvalidaException;
import com.ar.crm2.application.ai.json.JsonNumber;
import com.ar.crm2.application.ai.json.JsonObject;
import com.ar.crm2.application.ai.json.JsonParseException;
import com.ar.crm2.application.ai.json.JsonParser;
import com.ar.crm2.application.ai.json.JsonString;
import com.ar.crm2.application.ai.json.JsonValue;
import com.ar.crm2.application.contacto.command.CreateContactoCommand;
import com.ar.crm2.application.ficha.command.MoverColumnaFichaCommand;
import com.ar.crm2.application.tarea.command.CreateTareaCommand;
import com.ar.crm2.application.trato.command.CreateTratoCommand;
import com.ar.crm2.model.entity.ia.AiAccion;
import com.ar.crm2.model.enums.EstadoRelacion;
import com.ar.crm2.model.enums.PrioridadTarea;
import com.ar.crm2.model.enums.TipoContrato;
import com.ar.crm2.model.enums.TipoTarea;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JSON -> CRM command mapper used by {@link ConfirmarAccionService}.
 *
 * <p>Parses the JSON stored on the {@link AiAccion} directly into the
 * real CRM commands ({@link CreateContactoCommand},
 * {@link CreateTratoCommand},
 * {@link CreateTareaCommand},
 * {@link MoverColumnaFichaCommand}). Business validation lives in those
 * commands and their backing domain entities; this mapper only handles
 * JSON shape parsing (presence / type coercion) at the boundary.
 *
 * <p>JSON-shape errors throw {@link AccionInvalidaException} — the same
 * exception type the previous substring-based implementation used, so
 * the {@link ConfirmarAccionService} error contract is preserved and no
 * low-level parser exception leaks to application callers.
 *
 * <p><b>Required vs optional fields</b> (Slice 17 audit gap closing):
 * the contract declares each payload field as either REQUIRED or
 * optional. Required fields are read through
 * {@link #requiredStringField(JsonObject, String)} /
 * {@link #requiredEnumField(JsonObject, String, Class)}; the helpers
 * throw {@link AccionInvalidaException} with the controlled
 * {@code "campo obligatorio: {field}"} message when the field is absent
 * OR present with a JSON {@code null} literal — this prevents the
 * downstream command constructor's
 * {@link IllegalArgumentException} from leaking through
 * {@code ConfirmarAccionService#dispatchMutation} and being misreported
 * as the action's FAILED reason. Optional fields keep using
 * {@link #stringField(JsonObject, String)} /
 * {@link #enumField(JsonObject, String, Class)} so a JSON {@code null}
 * literal still maps to {@code null} on the command.
 *
 * <p><b>Why we ship our own parser</b>: the application module's
 * dependency graph does NOT include Jackson (or any other JSON
 * library) and adding one would violate the project rule that the
 * application layer depends on {@code domain} only. The contract
 * surface is small (flat top-level fields of primitive types) so the
 * cost of a hand-rolled recursive-descent parser is bounded.
 *
 * <p>The parser is dependency-free, handles the full RFC 8259 escape
 * set (including the 4-digit hex Unicode escape), tolerates arbitrary
 * whitespace, and rejects non-string values for fields that must be
 * strings (e.g. arrays / nested objects as a {@code nombre} value).
 */
final class ConfirmarAccionMapper {

    private ConfirmarAccionMapper() {
    }

    static CreateContactoCommand toCreateContacto(AiAccion p) {
        JsonObject root = parseOrThrow(p.getPayloadJson());
        return new CreateContactoCommand(
            uuidField(root, "empresaId"),
            requiredStringField(root, "nombre"),
            stringField(root, "correo"),
            requiredEnumField(root, "estadoRelacion", EstadoRelacion.class),
            uuidField(root, "responsableId"),
            p.getSolicitadaPor().value(),
            stringField(root, "telefono"),
            stringField(root, "cargo"),
            stringField(root, "comoNosConocio")
        );
    }

    static CreateTratoCommand toCreateTrato(AiAccion p) {
        JsonObject root = parseOrThrow(p.getPayloadJson());
        return new CreateTratoCommand(
            uuidField(root, "contactoId"),
            uuidField(root, "responsableId"),
            requiredStringField(root, "nombre"),
            decimalField(root, "valorEstimado"),
            intField(root, "probabilidad"),
            dateField(root, "fechaCierreEsperada"),
            requiredEnumField(root, "tipoContrato", TipoContrato.class)
        );
    }

    static CreateTareaCommand toCreateTarea(AiAccion p) {
        JsonObject root = parseOrThrow(p.getPayloadJson());
        return new CreateTareaCommand(
            uuidField(root, "tratoId"),
            uuidField(root, "responsableId"),
            requiredStringField(root, "titulo"),
            requiredStringField(root, "descripcion"),
            requiredEnumField(root, "tipo", TipoTarea.class),
            requiredEnumField(root, "prioridad", PrioridadTarea.class),
            dateTimeField(root, "fechaLimite")
        );
    }

    static MoverColumnaFichaCommand toMoverFicha(AiAccion p) {
        JsonObject root = parseOrThrow(p.getPayloadJson());
        return new MoverColumnaFichaCommand(
            uuidField(root, "fichaId"),
            uuidField(root, "targetColumnaId")
        );
    }

    // ── JSON helpers (package-private; only the mapper uses them) ──

    static String stringField(JsonObject root, String field) {
        return stringFromValue(valueFor(root, field), field);
    }

    static UUID uuidField(JsonObject root, String field) {
        String raw = stringFromValue(valueFor(root, field), field);
        if (raw == null) {
            throw AccionInvalidaException.forInvalidInput("campo obligatorio: " + field);
        }
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException ex) {
            throw AccionInvalidaException.forInvalidInput(
                    "UUID inválido en " + field + ": " + raw
            );
        }
    }

    /**
     * Reads a REQUIRED string field from the parsed JSON object.
     *
     * <p>Behavior:
     * <ul>
     *   <li>Returns the decoded string when the field is present
     *       with a non-null string value.</li>
     *   <li>Throws {@link AccionInvalidaException} with the message
     *       {@code "campo obligatorio: {field}"} when the field is
     *       absent OR present with a JSON {@code null} literal.</li>
     *   <li>Throws {@link AccionInvalidaException} with a controlled
     *       "valor no-cadena en {field}" message when the field is an
     *       object, array, or boolean (the contract requires the field
     *       to be a string).</li>
     * </ul>
     *
     * <p>This helper exists so the mapper boundary can translate
     * malformed payloads into {@link AccionInvalidaException} BEFORE the
     * downstream CRM command constructor throws
     * {@link IllegalArgumentException}. Without it, a payload like
     * {@code {"nombre": null}} would leak the constructor's
     * {@code IllegalArgumentException("nombre is required")} to
     * {@code ConfirmarAccionService#dispatchMutation}, where it would be
     * caught and used as the action's FAILED reason — a leaky contract.
     *
     * <p>Use this for every string field the contract declares as
     * REQUIRED. Optional string fields keep using {@link #stringField}
     * so they can still map a JSON {@code null} literal to {@code null}.
     */
    static String requiredStringField(JsonObject root, String field) {
        String raw = stringFromValue(valueFor(root, field), field);
        if (raw == null) {
            throw AccionInvalidaException.forInvalidInput("campo obligatorio: " + field);
        }
        return raw;
    }

    static Integer intField(JsonObject root, String field) {
        String raw = numberAsString(root, field);
        if (raw == null) return null;
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ex) {
            throw AccionInvalidaException.forInvalidInput(
                    "número inválido en " + field + ": " + raw
            );
        }
    }

    static BigDecimal decimalField(JsonObject root, String field) {
        String raw = numberAsString(root, field);
        if (raw == null) return null;
        try {
            return new BigDecimal(raw);
        } catch (NumberFormatException ex) {
            throw AccionInvalidaException.forInvalidInput(
                    "decimal inválido en " + field + ": " + raw
            );
        }
    }

    static LocalDate dateField(JsonObject root, String field) {
        String raw = stringFromValue(valueFor(root, field), field);
        if (raw == null) return null;
        try {
            return LocalDate.parse(raw);
        } catch (Exception ex) {
            throw AccionInvalidaException.forInvalidInput(
                    "fecha inválida en " + field + ": " + raw
            );
        }
    }

    static LocalDateTime dateTimeField(JsonObject root, String field) {
        String raw = stringFromValue(valueFor(root, field), field);
        if (raw == null) return null;
        try {
            return LocalDateTime.parse(raw);
        } catch (Exception ex) {
            throw AccionInvalidaException.forInvalidInput(
                    "fecha-hora inválida en " + field + ": " + raw
            );
        }
    }

    static <E extends Enum<E>> E enumField(JsonObject root, String field, Class<E> type) {
        String raw = stringFromValue(valueFor(root, field), field);
        if (raw == null) return null;
        try {
            return Enum.valueOf(type, raw);
        } catch (IllegalArgumentException ex) {
            throw AccionInvalidaException.forInvalidInput(
                    "valor inválido en " + field + ": " + raw
            );
        }
    }

    /**
     * Reads a REQUIRED enum field from the parsed JSON object.
     *
     * <p>Behavior mirrors {@link #requiredStringField}: returns the
     * decoded enum value when the field is present with a non-null
     * string value matching one of {@code type}'s constants; throws
     * {@link AccionInvalidaException} with {@code "campo obligatorio:
     * {field}"} when the field is absent OR present with a JSON
     * {@code null} literal; throws a controlled
     * {@code "valor inválido en {field}"} when the value is not a valid
     * enum constant.
     *
     * <p>Use this for every enum field the contract declares as
     * REQUIRED (e.g. {@code estadoRelacion}, {@code tipoContrato},
     * {@code tipo}, {@code prioridad}). Optional enum fields would
     * continue to use {@link #enumField}.
     */
    static <E extends Enum<E>> E requiredEnumField(JsonObject root, String field, Class<E> type) {
        String raw = stringFromValue(valueFor(root, field), field);
        if (raw == null) {
            throw AccionInvalidaException.forInvalidInput("campo obligatorio: " + field);
        }
        try {
            return Enum.valueOf(type, raw);
        } catch (IllegalArgumentException ex) {
            throw AccionInvalidaException.forInvalidInput(
                    "valor inválido en " + field + ": " + raw
            );
        }
    }

    // ── Internal extraction primitives ─────────────────────────────

    /**
     * Returns the value associated with {@code field}, or {@code null}
     * when the field is absent OR present with a {@link JsonNull} value.
     */
    private static JsonValue valueFor(JsonObject root, String field) {
        return root.isNullOrMissing(field) ? null : root.get(field);
    }

    /**
     * Extracts the value for {@code field} as a {@link String}.
     *
     * <ul>
     *   <li>{@code null} when the field is absent, present with a
     *       {@code null} literal, or when the document itself is
     *       {@code null}.</li>
     *   <li>The decoded string value when the field is a string.</li>
     *   <li>The raw numeric token when the field is a number (some
     *       downstream coercers expect it as a string, e.g. UUID-like
     *       values the model may emit unquoted). The mapper's UUID /
     *       int / decimal coercers reject this on the next pass when
     *       it does not match their expected shape.</li>
     *   <li>{@link AccionInvalidaException} when the field is an
     *       object, array, or boolean — the contract requires these
     *       fields to be strings.</li>
     * </ul>
     */
    private static String stringFromValue(JsonValue value, String field) {
        if (value == null) return null;
        if (value instanceof JsonString s) {
            return s.value();
        }
        if (value instanceof JsonNumber n) {
            return n.raw();
        }
        throw AccionInvalidaException.forInvalidInput(
                "valor no-cadena en " + field + ": "
                        + value.getClass().getSimpleName()
        );
    }

    /**
     * Like {@link #stringFromValue} but accepts both numeric literals
     * (preferred for int / decimal fields) and quoted numeric strings
     * (the model may emit either form).
     */
    private static String numberAsString(JsonObject root, String field) {
        JsonValue value = valueFor(root, field);
        if (value == null) return null;
        if (value instanceof JsonNumber n) {
            return n.raw();
        }
        if (value instanceof JsonString s) {
            return s.value();
        }
        throw AccionInvalidaException.forInvalidInput(
                "valor no-numérico en " + field + ": "
                        + value.getClass().getSimpleName()
        );
    }

    /**
     * Parses the supplied JSON document into a {@link JsonObject}.
     *
     * <p>Top-level non-object payloads (arrays, scalars) are rejected
     * with {@link AccionInvalidaException} — every AI action contract
     * in scope is a flat JSON object.
     *
     * <p>JSON syntax errors and unexpected trailing content raise
     * {@link AccionInvalidaException} at the application boundary so
     * no parser-level exception type leaks to callers.
     */
    private static JsonObject parseOrThrow(String json) {
        if (json == null) {
            throw AccionInvalidaException.forInvalidInput(
                    "payload JSON ausente"
            );
        }
        JsonValue root;
        try {
            root = JsonParser.parse(json);
        } catch (JsonParseException ex) {
            throw AccionInvalidaException.forInvalidInput(
                    "JSON inválido: " + ex.getMessage()
            );
        }
        if (root instanceof JsonObject obj) {
            return obj;
        }
        throw AccionInvalidaException.forInvalidInput(
                "se esperaba un objeto JSON en la raíz, se obtuvo: "
                        + root.getClass().getSimpleName()
        );
    }
}