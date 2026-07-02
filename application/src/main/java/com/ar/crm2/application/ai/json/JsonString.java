package com.ar.crm2.application.ai.json;

/**
 * JSON string literal with all standard escape sequences decoded.
 *
 * <p>The parser honours the full RFC 8259 escape set (double-quote,
 * backslash, forward-slash, backspace, form-feed, line-feed,
 * carriage-return, tab, and the 4-digit hex Unicode escape). The
 * forward-slash escape is accepted but decodes to a regular slash —
 * preserved because the AI model may emit it.
 */
public record JsonString(String value) implements JsonValue {

    public JsonString {
        if (value == null) {
            throw new IllegalArgumentException("JsonString.value must not be null");
        }
    }

    @Override
    public String toJsonString() {
        StringBuilder sb = new StringBuilder(value.length() + 2);
        sb.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
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
        sb.append('"');
        return sb.toString();
    }
}