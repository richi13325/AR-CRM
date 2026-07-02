package com.ar.crm2.adapter.out.ai.policy;

/**
 * Default {@link TurnTimeoutPolicy} implementation: reads the
 * configured {@code turnTimeoutMillis} once and exposes it via
 * {@link #getTurnTimeoutMillis()}.
 *
 * <p>The bean is constructed by {@code AiWiringConfig} from the
 * {@code ai-assistant.turn-timeout-ms} property (env override
 * {@code AI_TURN_TIMEOUT_MS}). A defensive guard rejects non-positive
 * values at construction time so a misconfigured deploy cannot
 * silently disable the per-turn wall-clock limit.
 */
public final class ConfiguredTurnTimeoutPolicy implements TurnTimeoutPolicy {

    private final long turnTimeoutMillis;

    /**
     * @param turnTimeoutMillis the configured per-turn wall-clock
     *                          timeout — MUST be {@code >= 1}.
     * @throws IllegalArgumentException when {@code turnTimeoutMillis <= 0}.
     */
    public ConfiguredTurnTimeoutPolicy(long turnTimeoutMillis) {
        if (turnTimeoutMillis <= 0) {
            throw new IllegalArgumentException(
                "turnTimeoutMillis must be >= 1; got " + turnTimeoutMillis
            );
        }
        this.turnTimeoutMillis = turnTimeoutMillis;
    }

    @Override
    public long getTurnTimeoutMillis() {
        return turnTimeoutMillis;
    }
}