package com.ar.crm2.adapter.out.ai.policy;

/**
 * Per-turn wall-clock timeout policy.
 *
 * <p>The AI adapter MUST abort a turn that exceeds the configured
 * wall-clock budget. The policy returns the budget in milliseconds
 * so the adapter can wrap the synchronous {@code ChatClient.call()}
 * with {@link java.util.concurrent.CompletableFuture#orTimeout
 * CompletableFuture.orTimeout(...)}.
 *
 * <p><b>Why a dedicated policy collaborator?</b> the value comes from
 * configuration ({@code ai-assistant.turn-timeout-ms},
 * env-override {@code AI_TURN_TIMEOUT_MS}). Extracting it as an
 * interface keeps the adapter free of Spring {@code @Value}
 * injection and lets tests substitute a hand-rolled policy that
 * returns a deterministic duration.
 *
 * <p><b>Two-layer timeout.</b> this policy covers the adapter-level
 * wall-clock turn timeout. The provider-level HTTP timeout
 * ({@code spring.ai.openai.chat.options.timeout}) is a separate
 * Spring AI auto-config layer. Configuring both with
 * {@code turn-timeout-ms < spring.ai.openai.chat.options.timeout}
 * ensures the adapter observes the abort first and surfaces a
 * controlled error.
 */
public interface TurnTimeoutPolicy {

    /**
     * Returns the configured per-turn wall-clock timeout in
     * milliseconds. Must be {@code >= 1}.
     *
     * @return the turn timeout in milliseconds.
     */
    long getTurnTimeoutMillis();
}