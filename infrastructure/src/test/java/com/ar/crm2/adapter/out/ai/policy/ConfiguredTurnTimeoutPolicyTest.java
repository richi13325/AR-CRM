package com.ar.crm2.adapter.out.ai.policy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RED-first tests for {@link ConfiguredTurnTimeoutPolicy} — the
 * default implementation of {@link TurnTimeoutPolicy} that reads the
 * configured {@code turnTimeoutMillis} once and exposes it via
 * {@link ConfiguredTurnTimeoutPolicy#getTurnTimeoutMillis()}.
 *
 * <p>What these tests pin:
 * <ul>
 *   <li>Constructor rejects {@code turnTimeoutMillis <= 0} so a
 *       misconfigured deploy cannot disable the per-turn wall-clock
 *       limit.</li>
 *   <li>{@link ConfiguredTurnTimeoutPolicy#getTurnTimeoutMillis()}
 *       returns the configured value verbatim so the adapter can
 *       apply it to {@link java.util.concurrent.CompletableFuture#orTimeout}.</li>
 * </ul>
 */
class ConfiguredTurnTimeoutPolicyTest {

    @Test
    @DisplayName("constructor rejects turnTimeoutMillis <= 0 with IllegalArgumentException")
    void constructor_rejectsNonPositiveTimeout() {
        IllegalArgumentException zero = assertThrows(IllegalArgumentException.class,
            () -> new ConfiguredTurnTimeoutPolicy(0));
        assertTrue(zero.getMessage().contains("turnTimeoutMillis"),
            "exception message must name the parameter; got: " + zero.getMessage());

        IllegalArgumentException negative = assertThrows(IllegalArgumentException.class,
            () -> new ConfiguredTurnTimeoutPolicy(-1));
        assertTrue(negative.getMessage().contains("turnTimeoutMillis"),
            "exception message must name the parameter; got: " + negative.getMessage());
    }

    @Test
    @DisplayName("getTurnTimeoutMillis returns the configured value verbatim")
    void getTurnTimeoutMillis_returnsConfiguredValue() {
        ConfiguredTurnTimeoutPolicy policy = new ConfiguredTurnTimeoutPolicy(25_000L);
        assertEquals(25_000L, policy.getTurnTimeoutMillis(),
            "getTurnTimeoutMillis() must return the configured value so the adapter "
                + "can wrap the chat call with CompletableFuture.orTimeout");

        ConfiguredTurnTimeoutPolicy policyOne = new ConfiguredTurnTimeoutPolicy(1L);
        assertEquals(1L, policyOne.getTurnTimeoutMillis());
    }
}