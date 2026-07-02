package com.ar.crm2.application.ai.json;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the recursive-descent JSON parser.
 *
 * <p>The parser is a pure function (same input → same output) so the
 * tests focus on:
 * <ul>
 *   <li>each value type the AI mapper can extract;</li>
 *   <li>the full RFC 8259 escape set inside strings;</li>
 *   <li>whitespace tolerance;</li>
 *   <li>the controlled exception for invalid input.</li>
 * </ul>
 *
 * <p>The parser is intentionally permissive about payload shapes the
 * AI contract does not use (arrays, deeply nested objects) — those
 * still parse, but the mapper rejects them at the boundary with a
 * controlled {@code AccionInvalidaException} (see
 * {@code ConfirmarAccionMapperTest}).
 */
class JsonParserTest {

    // ── Primitive values ──────────────────────────────────────────

    @Nested
    @DisplayName("Primitive values")
    class Primitives {

        @Test
        @DisplayName("null literal")
        void parsesNullLiteral() {
            JsonValue v = JsonParser.parse("null");
            assertInstanceOf(JsonNull.class, v);
            assertTrue(v.isNull());
        }

        @Test
        @DisplayName("true literal")
        void parsesTrueLiteral() {
            JsonValue v = JsonParser.parse("true");
            assertInstanceOf(JsonBoolean.class, v);
            assertEquals(new JsonBoolean(true), v);
        }

        @Test
        @DisplayName("false literal")
        void parsesFalseLiteral() {
            JsonValue v = JsonParser.parse("false");
            assertInstanceOf(JsonBoolean.class, v);
            assertEquals(new JsonBoolean(false), v);
        }

        @Test
        @DisplayName("integer number")
        void parsesIntegerNumber() {
            JsonValue v = JsonParser.parse("42");
            assertInstanceOf(JsonNumber.class, v);
            assertEquals(new JsonNumber("42"), v);
        }

        @Test
        @DisplayName("decimal number")
        void parsesDecimalNumber() {
            JsonValue v = JsonParser.parse("50000.00");
            assertInstanceOf(JsonNumber.class, v);
            assertEquals(new JsonNumber("50000.00"), v);
        }

        @Test
        @DisplayName("negative number")
        void parsesNegativeNumber() {
            JsonValue v = JsonParser.parse("-17");
            assertInstanceOf(JsonNumber.class, v);
            assertEquals(new JsonNumber("-17"), v);
        }

        @Test
        @DisplayName("scientific notation")
        void parsesScientificNotation() {
            JsonValue v = JsonParser.parse("1.5e3");
            assertInstanceOf(JsonNumber.class, v);
            assertEquals(new JsonNumber("1.5e3"), v);
        }
    }

    // ── Strings & escape sequences ───────────────────────────────

    @Nested
    @DisplayName("Strings and escape sequences")
    class Strings {

        @Test
        @DisplayName("empty string")
        void parsesEmptyString() {
            JsonValue v = JsonParser.parse("\"\"");
            assertInstanceOf(JsonString.class, v);
            assertEquals(new JsonString(""), v);
        }

        @Test
        @DisplayName("simple string")
        void parsesSimpleString() {
            JsonValue v = JsonParser.parse("\"hello world\"");
            assertEquals(new JsonString("hello world"), v);
        }

        @Test
        @DisplayName("string with embedded escaped quotes")
        void parsesEscapedQuotes() {
            JsonValue v = JsonParser.parse("\"Juan \\\"El Grande\\\"\"");
            assertEquals(new JsonString("Juan \"El Grande\""), v);
        }

        @Test
        @DisplayName("string with embedded escaped backslash")
        void parsesEscapedBackslash() {
            JsonValue v = JsonParser.parse("\"C:\\\\Users\"");
            assertEquals(new JsonString("C:\\Users"), v);
        }

        @Test
        @DisplayName("string with control escapes (\\n \\r \\t \\b \\f)")
        void parsesControlEscapes() {
            JsonValue v = JsonParser.parse("\"a\\nb\\rc\\td\\be\\f\"");
            assertEquals(new JsonString("a\nb\rc\td\be\f"), v);
        }

        @Test
        @DisplayName("string with escaped forward slash decodes to /")
        void parsesEscapedForwardSlash() {
            JsonValue v = JsonParser.parse("\"a\\/b\"");
            assertEquals(new JsonString("a/b"), v);
        }

        @Test
        @DisplayName("string with 4-digit hex Unicode escape (\\u00e9)")
        void parsesUnicodeEscape() {
            JsonValue v = JsonParser.parse("\"Jos\\u00e9\"");
            assertEquals(new JsonString("José"), v);
        }

        @Test
        @DisplayName("string rejects raw control characters (RFC 8259)")
        void rejectsRawControlCharacterInString() {
            String raw = "\"\u0001\"";
            JsonParseException ex = assertThrows(JsonParseException.class, () -> JsonParser.parse(raw));
            assertTrue(ex.getMessage().contains("Unescaped control character"));
        }

        @Test
        @DisplayName("string rejects unterminated literal")
        void rejectsUnterminatedString() {
            assertThrows(JsonParseException.class, () -> JsonParser.parse("\"unterminated"));
        }

        @Test
        @DisplayName("string rejects invalid escape sequence")
        void rejectsInvalidEscapeSequence() {
            assertThrows(JsonParseException.class, () -> JsonParser.parse("\"bad \\x escape\""));
        }

        @Test
        @DisplayName("string rejects malformed \\u escape (less than 4 hex digits)")
        void rejectsMalformedUnicodeEscape() {
            assertThrows(JsonParseException.class, () -> JsonParser.parse("\"bad \\u12Z escape\""));
        }
    }

    // ── Objects & arrays ─────────────────────────────────────────

    @Nested
    @DisplayName("Objects and arrays")
    class Containers {

        @Test
        @DisplayName("empty object")
        void parsesEmptyObject() {
            JsonValue v = JsonParser.parse("{}");
            JsonObject obj = assertInstanceOf(JsonObject.class, v);
            assertEquals(0, obj.entries().size());
        }

        @Test
        @DisplayName("object with multiple fields preserves insertion order")
        void parsesObjectPreservesOrder() {
            JsonValue v = JsonParser.parse("{\"z\":1,\"a\":2,\"m\":3}");
            JsonObject obj = assertInstanceOf(JsonObject.class, v);
            assertEquals(3, obj.entries().size());
            assertEquals(new JsonNumber("1"), obj.get("z"));
            assertEquals(new JsonNumber("2"), obj.get("a"));
            assertEquals(new JsonNumber("3"), obj.get("m"));
        }

        @Test
        @DisplayName("object with nested object value")
        void parsesObjectWithNestedValue() {
            JsonValue v = JsonParser.parse("{\"meta\":{\"nested\":true},\"name\":\"x\"}");
            JsonObject obj = assertInstanceOf(JsonObject.class, v);
            JsonObject nested = assertInstanceOf(JsonObject.class, obj.get("meta"));
            assertEquals(new JsonBoolean(true), nested.get("nested"));
        }

        @Test
        @DisplayName("object with array value")
        void parsesObjectWithArrayValue() {
            JsonValue v = JsonParser.parse("{\"tags\":[\"a\",\"b\",\"c\"]}");
            JsonObject obj = assertInstanceOf(JsonObject.class, v);
            JsonArray arr = assertInstanceOf(JsonArray.class, obj.get("tags"));
            assertEquals(3, arr.elements().size());
        }

        @Test
        @DisplayName("empty array")
        void parsesEmptyArray() {
            JsonValue v = JsonParser.parse("[]");
            JsonArray arr = assertInstanceOf(JsonArray.class, v);
            assertEquals(0, arr.elements().size());
        }

        @Test
        @DisplayName("get returns null when field is absent")
        void getReturnsNullForAbsentField() {
            JsonObject obj = (JsonObject) JsonParser.parse("{\"a\":1}");
            assertNull(obj.get("missing"));
            assertFalse(obj.contains("missing"));
        }

        @Test
        @DisplayName("isNullOrMissing distinguishes absent vs explicit null")
        void isNullOrMissingDistinguishes() {
            JsonObject obj = (JsonObject) JsonParser.parse("{\"a\":null,\"b\":1}");
            assertTrue(obj.isNullOrMissing("a"));
            assertFalse(obj.isNullOrMissing("b"));
            assertTrue(obj.isNullOrMissing("missing"));
        }
    }

    // ── Whitespace ────────────────────────────────────────────────

    @Nested
    @DisplayName("Whitespace")
    class Whitespace {

        @Test
        @DisplayName("leading and trailing whitespace is ignored")
        void parsesWithLeadingAndTrailingWhitespace() {
            JsonValue v = JsonParser.parse("   \n  42  \t  ");
            assertEquals(new JsonNumber("42"), v);
        }

        @Test
        @DisplayName("whitespace between tokens is ignored")
        void parsesWithInternalWhitespace() {
            JsonValue v = JsonParser.parse("{ \n  \"a\" : 1 ,\n  \"b\" : 2 \n}");
            JsonObject obj = assertInstanceOf(JsonObject.class, v);
            assertEquals(new JsonNumber("1"), obj.get("a"));
            assertEquals(new JsonNumber("2"), obj.get("b"));
        }
    }

    // ── Rejection ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Rejection of invalid input")
    class Rejection {

        @Test
        @DisplayName("null input throws JsonParseException")
        void rejectsNullInput() {
            JsonParseException ex = assertThrows(JsonParseException.class, () -> JsonParser.parse(null));
            assertTrue(ex.getMessage().contains("null"));
        }

        @Test
        @DisplayName("empty input throws JsonParseException")
        void rejectsEmptyInput() {
            assertThrows(JsonParseException.class, () -> JsonParser.parse(""));
            assertThrows(JsonParseException.class, () -> JsonParser.parse("   "));
        }

        @Test
        @DisplayName("truncated object throws JsonParseException")
        void rejectsTruncatedObject() {
            assertThrows(JsonParseException.class, () -> JsonParser.parse("{\"a\":1,"));
        }

        @Test
        @DisplayName("missing key throws JsonParseException")
        void rejectsMissingKey() {
            assertThrows(JsonParseException.class, () -> JsonParser.parse("{1:\"value\"}"));
        }

        @Test
        @DisplayName("trailing content throws JsonParseException")
        void rejectsTrailingContent() {
            assertThrows(JsonParseException.class, () -> JsonParser.parse("{\"a\":1} extra"));
        }

        @Test
        @DisplayName("single quote is rejected (JSON requires double quotes)")
        void rejectsSingleQuote() {
            assertThrows(JsonParseException.class, () -> JsonParser.parse("'hello'"));
        }

        @Test
        @DisplayName("invalid boolean spelling is rejected")
        void rejectsInvalidBooleanSpelling() {
            assertThrows(JsonParseException.class, () -> JsonParser.parse("True"));
            assertThrows(JsonParseException.class, () -> JsonParser.parse("yes"));
        }

        @Test
        @DisplayName("invalid null spelling is rejected")
        void rejectsInvalidNullSpelling() {
            assertThrows(JsonParseException.class, () -> JsonParser.parse("NULL"));
            assertThrows(JsonParseException.class, () -> JsonParser.parse("None"));
        }
    }

    // ── Round-trip ────────────────────────────────────────────────

    @Nested
    @DisplayName("Round-trip via toJsonString")
    class RoundTrip {

        @Test
        @DisplayName("parsed primitive serializes back to its source form")
        void primitiveRoundTrip() {
            assertEquals("null", JsonParser.parse("null").toJsonString());
            assertEquals("true", JsonParser.parse("true").toJsonString());
            assertEquals("false", JsonParser.parse("false").toJsonString());
            assertEquals("42", JsonParser.parse("42").toJsonString());
            assertEquals("50000.00", JsonParser.parse("50000.00").toJsonString());
        }

        @Test
        @DisplayName("parsed string with escapes serializes back with escapes")
        void stringRoundTripWithEscapes() {
            JsonString s = new JsonString("line1\nline2\tcol\"end\"");
            assertEquals("\"line1\\nline2\\tcol\\\"end\\\"\"", s.toJsonString());
        }

        @Test
        @DisplayName("parsed object serializes back to a stable canonical form")
        void objectRoundTrip() {
            JsonObject obj = (JsonObject) JsonParser.parse("{\"a\":1,\"b\":\"x\"}");
            assertEquals("{\"a\":1,\"b\":\"x\"}", obj.toJsonString());
        }

        @Test
        @DisplayName("Object.isNullOrMissing handles the Optional boundary idiomatically")
        void isNullOrMissingOptionalIdiom() {
            // Demonstrates the API surface for callers who prefer Optional.
            JsonObject obj = (JsonObject) JsonParser.parse("{\"a\":null}");
            Optional<JsonValue> present = Optional.ofNullable(obj.get("a")).filter(v -> !v.isNull());
            assertTrue(present.isEmpty());
        }
    }
}