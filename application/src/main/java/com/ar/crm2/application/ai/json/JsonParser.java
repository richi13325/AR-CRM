package com.ar.crm2.application.ai.json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Dependency-free, recursive-descent JSON parser used by
 * {@code ConfirmarAccionMapper}.
 *
 * <p><b>Why we ship our own parser.</b> The application module's
 * dependency graph does NOT include Jackson (or any other JSON
 * library) and adding one would violate the project rule that the
 * application layer depends on {@code domain} only. The contract
 * surface is small (flat top-level fields of primitive types) so the
 * cost of a hand-rolled parser is bounded.
 *
 * <p><b>What it handles.</b>
 * <ul>
 *   <li>Objects ({@code {"key": value, ...}}) — preserves insertion order.</li>
 *   <li>Arrays ({@code [value, ...]}) — not used by the current
 *       contract but accepted so the parser does not crash on
 *       unexpected payloads.</li>
 *   <li>Strings with the full RFC 8259 escape set:
 *       double-quote, backslash, forward-slash, backspace, form-feed,
 *       line-feed, carriage-return, tab, and the 4-digit hex Unicode
 *       escape sequence.</li>
 *   <li>Numbers (integer, decimal, optional exponent) — preserved as
 *       raw tokens so the mapper can apply its own precision policy.</li>
 *   <li>{@code true}, {@code false}, {@code null} literals.</li>
 *   <li>Arbitrary whitespace between tokens (incl. inside strings
 *       only via the standard escapes).</li>
 * </ul>
 *
 * <p><b>What it rejects.</b> Anything that is not a valid JSON
 * document per RFC 8259. Rejection raises {@link JsonParseException}
 * (unchecked); the mapper converts that into
 * {@code AccionInvalidaException.forInvalidInput(...)} at its boundary.
 */
public final class JsonParser {

    private final String src;
    private int pos;

    private JsonParser(String src) {
        this.src = src;
        this.pos = 0;
    }

    /**
     * Parses the supplied JSON document and returns the root value.
     *
     * @throws JsonParseException when the input is not valid JSON
     */
    public static JsonValue parse(String text) {
        if (text == null) {
            throw new JsonParseException("JSON input is null");
        }
        JsonParser p = new JsonParser(text);
        p.skipWhitespace();
        JsonValue root = p.parseValue();
        p.skipWhitespace();
        if (p.pos < p.src.length()) {
            throw new JsonParseException(
                    "Unexpected trailing content at position " + p.pos
            );
        }
        return root;
    }

    // ── Value dispatcher ──────────────────────────────────────────

    private JsonValue parseValue() {
        skipWhitespace();
        if (pos >= src.length()) {
            throw new JsonParseException("Unexpected end of input");
        }
        char c = src.charAt(pos);
        return switch (c) {
            case '{' -> parseObject();
            case '[' -> parseArray();
            case '"' -> new JsonString(parseString());
            case 't', 'f' -> parseBoolean();
            case 'n' -> parseNullLiteral();
            default -> {
                if (c == '-' || (c >= '0' && c <= '9')) {
                    yield new JsonNumber(parseNumber());
                }
                throw new JsonParseException(
                        "Unexpected character '" + c + "' at position " + pos
                );
            }
        };
    }

    // ── Object ────────────────────────────────────────────────────

    private JsonObject parseObject() {
        expect('{');
        Map<String, JsonValue> entries = new LinkedHashMap<>();
        skipWhitespace();
        if (pos < src.length() && src.charAt(pos) == '}') {
            pos++;
            return new JsonObject(entries);
        }
        while (true) {
            skipWhitespace();
            if (pos >= src.length() || src.charAt(pos) != '"') {
                throw new JsonParseException(
                        "Expected string key at position " + pos
                );
            }
            String key = parseString();
            skipWhitespace();
            expect(':');
            JsonValue value = parseValue();
            entries.put(key, value);
            skipWhitespace();
            if (pos >= src.length()) {
                throw new JsonParseException(
                        "Unexpected end of input inside object"
                );
            }
            char c = src.charAt(pos);
            if (c == ',') {
                pos++;
                continue;
            }
            if (c == '}') {
                pos++;
                return new JsonObject(entries);
            }
            throw new JsonParseException(
                    "Expected ',' or '}' at position " + pos + ", got '" + c + "'"
            );
        }
    }

    // ── Array ─────────────────────────────────────────────────────

    private JsonArray parseArray() {
        expect('[');
        List<JsonValue> elements = new ArrayList<>();
        skipWhitespace();
        if (pos < src.length() && src.charAt(pos) == ']') {
            pos++;
            return new JsonArray(elements);
        }
        while (true) {
            elements.add(parseValue());
            skipWhitespace();
            if (pos >= src.length()) {
                throw new JsonParseException(
                        "Unexpected end of input inside array"
                );
            }
            char c = src.charAt(pos);
            if (c == ',') {
                pos++;
                continue;
            }
            if (c == ']') {
                pos++;
                return new JsonArray(elements);
            }
            throw new JsonParseException(
                    "Expected ',' or ']' at position " + pos + ", got '" + c + "'"
            );
        }
    }

    // ── String ────────────────────────────────────────────────────

    private String parseString() {
        expect('"');
        StringBuilder sb = new StringBuilder();
        while (pos < src.length()) {
            char c = src.charAt(pos++);
            if (c == '"') {
                return sb.toString();
            }
            if (c == '\\') {
                if (pos >= src.length()) {
                    throw new JsonParseException("Unterminated escape sequence");
                }
                char esc = src.charAt(pos++);
                switch (esc) {
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case '/' -> sb.append('/');
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case 'u' -> {
                        if (pos + 4 > src.length()) {
                            throw new JsonParseException(
                                    "Invalid \\u escape: expected 4 hex digits"
                            );
                        }
                        int code;
                        try {
                            code = Integer.parseInt(src.substring(pos, pos + 4), 16);
                        } catch (NumberFormatException ex) {
                            throw new JsonParseException(
                                    "Invalid \\u escape at position " + (pos - 2)
                            );
                        }
                        sb.append((char) code);
                        pos += 4;
                    }
                    default -> throw new JsonParseException(
                            "Invalid escape '\\" + esc + "' at position " + (pos - 1)
                    );
                }
            } else {
                // RFC 8259 forbids raw control characters (< 0x20) inside strings.
                if (c < 0x20) {
                    throw new JsonParseException(
                            "Unescaped control character 0x"
                                    + Integer.toHexString(c)
                                    + " in string at position " + (pos - 1)
                    );
                }
                sb.append(c);
            }
        }
        throw new JsonParseException("Unterminated string literal");
    }

    // ── Number ────────────────────────────────────────────────────

    private String parseNumber() {
        int start = pos;
        if (src.charAt(pos) == '-') {
            pos++;
        }
        // Integer part: at least one digit.
        if (pos >= src.length() || !isDigit(src.charAt(pos))) {
            throw new JsonParseException(
                    "Invalid number at position " + start
            );
        }
        while (pos < src.length() && isDigit(src.charAt(pos))) {
            pos++;
        }
        // Fractional part.
        if (pos < src.length() && src.charAt(pos) == '.') {
            pos++;
            if (pos >= src.length() || !isDigit(src.charAt(pos))) {
                throw new JsonParseException(
                        "Invalid number fraction at position " + pos
                );
            }
            while (pos < src.length() && isDigit(src.charAt(pos))) {
                pos++;
            }
        }
        // Exponent part.
        if (pos < src.length() && (src.charAt(pos) == 'e' || src.charAt(pos) == 'E')) {
            pos++;
            if (pos < src.length() && (src.charAt(pos) == '+' || src.charAt(pos) == '-')) {
                pos++;
            }
            if (pos >= src.length() || !isDigit(src.charAt(pos))) {
                throw new JsonParseException(
                        "Invalid number exponent at position " + pos
                );
            }
            while (pos < src.length() && isDigit(src.charAt(pos))) {
                pos++;
            }
        }
        return src.substring(start, pos);
    }

    // ── Booleans / null ──────────────────────────────────────────

    private JsonBoolean parseBoolean() {
        if (src.startsWith("true", pos)) {
            pos += 4;
            return new JsonBoolean(true);
        }
        if (src.startsWith("false", pos)) {
            pos += 5;
            return new JsonBoolean(false);
        }
        throw new JsonParseException(
                "Invalid boolean literal at position " + pos
        );
    }

    private JsonNull parseNullLiteral() {
        if (src.startsWith("null", pos)) {
            pos += 4;
            return JsonNull.INSTANCE;
        }
        throw new JsonParseException(
                "Invalid null literal at position " + pos
        );
    }

    // ── Whitespace + low-level helpers ───────────────────────────

    private void skipWhitespace() {
        while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) {
            pos++;
        }
    }

    private void expect(char expected) {
        if (pos >= src.length() || src.charAt(pos) != expected) {
            throw new JsonParseException(
                    "Expected '" + expected + "' at position " + pos
            );
        }
        pos++;
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
}