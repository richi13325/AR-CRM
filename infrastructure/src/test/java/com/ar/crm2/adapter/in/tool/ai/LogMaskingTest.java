package com.ar.crm2.adapter.in.tool.ai;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * RED-first tests for {@link LogMasking} — the pure-function PII
 * masking helper used by the AI tool adapters so log records never
 * carry customer phone numbers in clear text.
 *
 * <p><b>Masking policy.</b>
 * <ul>
 *   <li>{@code null} or blank input → empty string.</li>
 *   <li>Length &le; 4 → {@code "***"} (no useful signal to preserve).</li>
 *   <li>Length &le; 8 → keep first 2 + {@code "***"} + keep last 2.</li>
 *   <li>Length &gt; 8 → keep first 6 + {@code "***"} + keep last 4
 *       (preserves country/area code + last-four for diagnostics).</li>
 * </ul>
 *
 * <p><b>Why a deterministic helper, not a log-capture test.</b>
 * the project does not depend on any log-capture test library
 * (e.g. {@code log-captor}, {@code logback-test}, or a custom
 * {@code ListAppender}); the prior slices pinned log-message
 * contracts through reflection/source-guard tests rather than by
 * intercepting the SLF4J/Logback appender. The masking helper is
 * therefore tested as a pure function (same input → same output, no
 * side effects), and {@code BuscarClientePorTelefonoToolTest} pins
 * that the tool actually routes the phone through the helper at the
 * source level. See Slice 13 apply-progress for the rationale.
 */
class LogMaskingTest {

    @Test
    @DisplayName("maskPhone returns empty string for null input (defensive: never NullPointerException)")
    void maskPhone_null_returnsEmpty() {
        String out = LogMasking.maskPhone(null);
        assertEquals("", out,
            "null input must collapse to an empty string so log statements never throw");
    }

    @Test
    @DisplayName("maskPhone returns empty string for blank input (whitespace only)")
    void maskPhone_blank_returnsEmpty() {
        assertAll(
            () -> assertEquals("", LogMasking.maskPhone(""),
                "empty string must collapse to empty"),
            () -> assertEquals("", LogMasking.maskPhone("   "),
                "whitespace-only input must collapse to empty")
        );
    }

    @Test
    @DisplayName("maskPhone on a 4-char-or-less input collapses to '***' (no useful signal)")
    void maskPhone_veryShort_returnsStarsOnly() {
        assertAll(
            () -> assertEquals("***", LogMasking.maskPhone("1")),
            () -> assertEquals("***", LogMasking.maskPhone("12")),
            () -> assertEquals("***", LogMasking.maskPhone("123")),
            () -> assertEquals("***", LogMasking.maskPhone("1234"))
        );
    }

    @Test
    @DisplayName("maskPhone on a 5..8 char input keeps first 2 and last 2 with '***' between")
    void maskPhone_short_keepsFirstTwoAndLastTwo() {
        assertAll(
            () -> assertEquals("12***45", LogMasking.maskPhone("12345")),
            () -> assertEquals("12***67", LogMasking.maskPhone("1234567")),
            () -> assertEquals("12***78", LogMasking.maskPhone("12345678"))
        );
    }

    @Test
    @DisplayName("maskPhone on a typical E.164 input keeps first 4 and last 4 with '***' between")
    void maskPhone_typicalE164_keepsFirstFourAndLastFour() {
        assertAll(
            () -> assertEquals("+549***5555", LogMasking.maskPhone("+5491155555555"),
                "Argentina E.164: country + area + last-four must survive"),
            () -> assertEquals("+141***1234", LogMasking.maskPhone("+14155551234"),
                "US E.164: country + area + last-four must survive"),
            () -> assertEquals("+346***8901", LogMasking.maskPhone("+34611678901"),
                "Spain E.164: country + area + last-four must survive")
        );
    }

    @Test
    @DisplayName("maskPhone on >12 char input caps at first-4/last-4 (so the masked length stays bounded)")
    void maskPhone_longerThanMax_keepsFirstFourAndLastFour() {
        // even absurdly long input must collapse to first-4/last-4 so the log line stays readable
        assertEquals("1234***cdef", LogMasking.maskPhone("1234567890abcdef"));
    }

    @Test
    @DisplayName("maskPhone output NEVER contains the full input verbatim (PII invariant)")
    void maskPhone_outputDoesNotContainFullInput() {
        String[] samples = {
            "+5491155555555",
            "+14155551234",
            "+34611678901",
            "12345",
            "1234567890abcdef"
        };
        for (String s : samples) {
            String out = LogMasking.maskPhone(s);
            assertNotNull(out);
            assertFalse(out.equals(s),
                "masked output for '" + s + "' must NOT equal the input verbatim");
            // For inputs longer than the long mask (4 + 3 + 4 = 11 chars),
            // the output length must be strictly smaller than the input length
            // (proves information is destroyed).
            if (s.length() > 11) {
                assertTrue(out.length() < s.length(),
                    "masked output for '" + s + "' must be shorter than the input");
            }
        }
    }

    @Test
    @DisplayName("maskPhone is idempotent under repeated application (deterministic pure function)")
    void maskPhone_idempotent() {
        String once = LogMasking.maskPhone("+5491155555555");
        String twice = LogMasking.maskPhone(once);
        String thrice = LogMasking.maskPhone(twice);
        assertEquals(once, twice,
            "masking an already-masked value must be a no-op (idempotent)");
        assertEquals(twice, thrice,
            "masking an already-masked value must be a no-op (idempotent)");
    }
}