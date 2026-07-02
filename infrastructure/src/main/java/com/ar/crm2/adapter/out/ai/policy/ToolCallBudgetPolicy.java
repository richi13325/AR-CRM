package com.ar.crm2.adapter.out.ai.policy;

import com.ar.crm2.application.ai.exception.AiAssistantException;

/**
 * Per-turn tool-call budget policy.
 *
 * <p>The assistant's tool-calling loop is allowed up to
 * {@link #maxToolCallsPerTurn()} tool executions in a single user turn.
 * Once the budget is exhausted, the adapter refuses further tool
 * execution by raising {@link AiAssistantException} — the framework
 * may keep generating, but no additional {@code @Tool} callback will
 * be invoked.
 *
 * <p><b>Why a dedicated policy collaborator?</b> the value comes from
 * configuration ({@code ai-assistant.max-tool-calls-per-turn},
 * env-override {@code AI_MAX_TOOL_CALLS}). Extracting it as an
 * interface keeps the adapter free of Spring {@code @Value}
 * injection and lets tests substitute a hand-rolled policy that
 * asserts the contract without booting a Spring context.
 *
 * <p><b>Production enforcement.</b> Spring AI 2.0 does NOT expose a
 * direct {@code maxToolCallsPerTurn} knob on
 * {@code ChatOptions}/{@code ToolCallingChatOptions}/
 * {@code ToolCallingAdvisor}. Real production enforcement therefore
 * requires a custom {@code CallAdvisor} injected into the
 * {@code ChatClient} chain (see design decision memo). The current
 * implementation exposes this policy as a deterministic test seam:
 * the adapter consults {@link #assertWithinBudget(int)} from public
 * helper paths and the policy contract is verifiable end-to-end
 * through unit tests.
 */
public interface ToolCallBudgetPolicy {

    /**
     * Returns the configured max-tool-calls-per-turn budget. Used for
     * trace / metric labels.
     *
     * @return the configured budget (always >= 1).
     */
    int maxToolCallsPerTurn();

    /**
     * Asserts that a hypothetical {@code toolCallCount} tool calls
     * would still be within budget. Throws
     * {@link AiAssistantException} once the budget is exceeded so the
     * adapter can surface a controlled 502-style error to the REST
     * layer.
     *
     * @param toolCallCount the cumulative number of tool calls the
     *                      adapter has dispatched in the current turn.
     *                      Must be non-negative.
     * @throws AiAssistantException when {@code toolCallCount} exceeds
     *                              the configured budget.
     */
    void assertWithinBudget(int toolCallCount);
}