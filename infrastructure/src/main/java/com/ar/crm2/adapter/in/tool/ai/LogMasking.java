package com.ar.crm2.adapter.in.tool.ai;

/**
 * Pure-function helper that masks customer PII before it reaches the
 * logging sink. The AI tool adapters route any model-supplied phone
 * number through {@link #maskPhone(String)} so log records never
 * carry a full customer phone in clear text.
 *
 * <p><b>Masking policy.</b>
 * <ul>
 *   <li>{@code null} or blank input → empty string
 *       (defensive: the tool must never NPE inside a log statement).</li>
 *   <li>Length &le; 4 → {@code "***"}
 *       (no useful diagnostic signal — fully mask).</li>
 *   <li>Length 5..8 → keep first 2 + {@code "***"} + keep last 2
 *       (short codes, length-balanced mask).</li>
 *   <li>Length &gt; 8 → keep first 4 + {@code "***"} + keep last 4
 *       (E.164 typical: country + area + last-four survive for
 *       diagnostics while the middle subscriber digits are masked).</li>
 * </ul>
 *
 * <p><b>Idempotent.</b> applying {@code maskPhone} to an already-masked
 * value is a no-op (the masked output falls in the 7..13 char range
 * which never re-masks). The pure-function contract is verified by
 * {@code LogMaskingTest#maskPhone_idempotent}.
 *
 * <p><b>Why a pure function, not a log-capture test.</b> the project
 * does not depend on any log-capture test library; the previous slices
 * pinned log-message contracts through reflection or source-guard
 * tests. The masking contract is therefore owned by this pure function
 * (deterministic, no side effects, no SLF4J dependency) and the AI
 * tools are pinned to call it via source-guard tests
 * (e.g. {@code BuscarClientePorTelefonoToolTest}).
 */
public final class LogMasking {

    /** Marker used to replace the masked middle digits. */
    static final String MASK_MARKER = "***";

    /** Maximum length that fully collapses to {@code "***"}. */
    static final int VERY_SHORT_MAX_LENGTH = 4;

    /** Maximum length that triggers the short-code mask policy. */
    static final int SHORT_MAX_LENGTH = 8;

    /** Number of leading chars preserved by the short-code mask. */
    static final int SHORT_PREFIX_LENGTH = 2;

    /** Number of trailing chars preserved by the short-code mask. */
    static final int SHORT_SUFFIX_LENGTH = 2;

    /** Number of leading chars preserved by the typical E.164 mask. */
    static final int LONG_PREFIX_LENGTH = 4;

    /** Number of trailing chars preserved by the typical E.164 mask. */
    static final int LONG_SUFFIX_LENGTH = 4;

    private LogMasking() {
        // pure-function holder — no instances
    }

    /**
     * Returns a masked representation of the supplied phone number.
     *
     * <p>The output never contains the full input verbatim (for any
     * non-trivial length). See the class Javadoc for the full policy.
     *
     * @param telefono the raw phone number (may be null or blank)
     * @return the masked representation; never null
     */
    public static String maskPhone(String telefono) {
        if (telefono == null || telefono.isBlank()) {
            return "";
        }
        String trimmed = telefono.strip();
        int len = trimmed.length();
        if (len <= VERY_SHORT_MAX_LENGTH) {
            return MASK_MARKER;
        }
        if (len <= SHORT_MAX_LENGTH) {
            return trimmed.substring(0, SHORT_PREFIX_LENGTH)
                + MASK_MARKER
                + trimmed.substring(len - SHORT_SUFFIX_LENGTH);
        }
        // typical E.164 length — keep country + area + last four
        return trimmed.substring(0, LONG_PREFIX_LENGTH)
            + MASK_MARKER
            + trimmed.substring(len - LONG_SUFFIX_LENGTH);
    }
}