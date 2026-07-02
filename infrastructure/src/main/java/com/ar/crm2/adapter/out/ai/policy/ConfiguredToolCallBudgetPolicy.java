package com.ar.crm2.adapter.out.ai.policy;

import com.ar.crm2.application.ai.exception.AiAssistantException;

/**
 * Default {@link ToolCallBudgetPolicy} implementation: reads the
 * configured {@code maxToolCallsPerTurn} once and answers budget
 * assertions against it.
 *
 * <p>The bean is constructed by {@code AiWiringConfig} from the
 * {@code ai-assistant.max-tool-calls-per-turn} property (env
 * override {@code AI_MAX_TOOL_CALLS}). A defensive guard rejects
 * non-positive values at construction time so a misconfigured
 * deploy cannot silently disable the safety limit.
 *
 * <p><b>Design note.</b> Spring AI 2.0 does NOT expose a direct
 * {@code maxToolCallsPerTurn} knob on
 * {@code ChatOptions}/{@code ToolCallingChatOptions}/
 * {@code ToolCallingAdvisor}; real production enforcement therefore
 * requires a custom {@code CallAdvisor} injected into the
 * {@code ChatClient} chain. The current implementation exposes this
 * policy as a deterministic seam: the adapter calls
 * {@link #assertWithinBudget(int)} from public helper paths and the
 * contract is verifiable end-to-end through unit tests. Wiring a
 * real {@code CallAdvisor} is documented as a follow-up design
 * decision (see {@code apply-progress.md}).
 */
public final class ConfiguredToolCallBudgetPolicy implements ToolCallBudgetPolicy {

    private final int maxToolCallsPerTurn;

    /**
     * @param maxToolCallsPerTurn the configured max tool calls per
     *                            turn — MUST be {@code >= 1}.
     * @throws IllegalArgumentException when {@code maxToolCallsPerTurn <= 0}.
     */
    public ConfiguredToolCallBudgetPolicy(int maxToolCallsPerTurn) {
        if (maxToolCallsPerTurn <= 0) {
            throw new IllegalArgumentException(
                "maxToolCallsPerTurn must be >= 1; got " + maxToolCallsPerTurn
            );
        }
        this.maxToolCallsPerTurn = maxToolCallsPerTurn;
    }

    @Override
    public int maxToolCallsPerTurn() {
        return maxToolCallsPerTurn;
    }

    @Override
    public void assertWithinBudget(int toolCallCount) {
        if (toolCallCount < 0) {
            throw new IllegalArgumentException(
                "toolCallCount must be >= 0; got " + toolCallCount
            );
        }
        if (toolCallCount > maxToolCallsPerTurn) {
            throw AiAssistantException.upstreamFailure(
                "Per-turn tool-call budget exceeded: " + toolCallCount
                    + " tool calls > configured budget of " + maxToolCallsPerTurn
                    + ". The assistant MUST stop invoking @Tool adapters for this turn."
            );
        }
    }
}