package com.ar.crm2.adapter.out.ai.policy;

import com.ar.crm2.application.ai.exception.AiAssistantException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RED-first tests for {@link ConfiguredToolCallBudgetPolicy} — the
 * default implementation of {@link ToolCallBudgetPolicy} that reads
 * the configured {@code maxToolCallsPerTurn} once and answers budget
 * assertions against it.
 *
 * <p>What these tests pin:
 * <ul>
 *   <li>Constructor rejects {@code maxToolCallsPerTurn <= 0} so a
 *       misconfigured deploy cannot disable the safety limit.</li>
 *   <li>{@link ConfiguredToolCallBudgetPolicy#maxToolCallsPerTurn()}
 *       returns the configured value verbatim.</li>
 *   <li>{@link ConfiguredToolCallBudgetPolicy#assertWithinBudget(int)}
 *       is a no-op for counts in {@code [0, max]}.</li>
 *   <li>Exceeding the budget by exactly one throws
 *       {@link AiAssistantException} with a message that names the
 *       configured limit and the offending count so the adapter can
 *       surface it to the caller.</li>
 *   <li>Negative counts are rejected — they would silently bypass
 *       the budget and let the tool-calling loop run unbounded.</li>
 * </ul>
 */
class ConfiguredToolCallBudgetPolicyTest {

    @Test
    @DisplayName("constructor rejects maxToolCallsPerTurn <= 0 with IllegalArgumentException")
    void constructor_rejectsNonPositiveBudget() {
        IllegalArgumentException zero = assertThrows(IllegalArgumentException.class,
            () -> new ConfiguredToolCallBudgetPolicy(0));
        assertTrue(zero.getMessage().contains("maxToolCallsPerTurn"),
            "exception message must name the parameter; got: " + zero.getMessage());

        IllegalArgumentException negative = assertThrows(IllegalArgumentException.class,
            () -> new ConfiguredToolCallBudgetPolicy(-1));
        assertTrue(negative.getMessage().contains("maxToolCallsPerTurn"),
            "exception message must name the parameter; got: " + negative.getMessage());
    }

    @Test
    @DisplayName("maxToolCallsPerTurn returns the configured value verbatim")
    void maxToolCallsPerTurn_returnsConfiguredValue() {
        ConfiguredToolCallBudgetPolicy policy = new ConfiguredToolCallBudgetPolicy(5);
        assertEquals(5, policy.maxToolCallsPerTurn(),
            "maxToolCallsPerTurn() must return the configured value so adapters can "
                + "log it for traceability");

        ConfiguredToolCallBudgetPolicy policyOne = new ConfiguredToolCallBudgetPolicy(1);
        assertEquals(1, policyOne.maxToolCallsPerTurn());
    }

    @Test
    @DisplayName("assertWithinBudget accepts counts in [0, max] — budget is inclusive")
    void assertWithinBudget_acceptsCountsWithinBudget() {
        ConfiguredToolCallBudgetPolicy policy = new ConfiguredToolCallBudgetPolicy(5);

        assertDoesNotThrow(() -> policy.assertWithinBudget(0),
            "0 tool calls must be within budget (the loop hasn't started)");
        assertDoesNotThrow(() -> policy.assertWithinBudget(1),
            "1 tool call must be within budget for a budget of 5");
        assertDoesNotThrow(() -> policy.assertWithinBudget(4),
            "4 tool calls must be within budget for a budget of 5");
        assertDoesNotThrow(() -> policy.assertWithinBudget(5),
            "5 tool calls must be within budget — the budget is inclusive");
    }

    @Test
    @DisplayName("assertWithinBudget throws AiAssistantException when count exceeds the budget")
    void assertWithinBudget_throwsAiAssistantExceptionWhenBudgetExceeded() {
        ConfiguredToolCallBudgetPolicy policy = new ConfiguredToolCallBudgetPolicy(5);

        AiAssistantException ex = assertThrows(AiAssistantException.class,
            () -> policy.assertWithinBudget(6));
        assertTrue(ex.getMessage().contains("6"),
            "exception message must name the offending count; got: " + ex.getMessage());
        assertTrue(ex.getMessage().contains("5"),
            "exception message must name the configured budget; got: " + ex.getMessage());
        assertTrue(ex.getMessage().toLowerCase().contains("tool")
                && ex.getMessage().toLowerCase().contains("budget"),
            "exception message must mention the tool-call budget contract; got: "
                + ex.getMessage());
    }

    @Test
    @DisplayName("assertWithinBudget rejects negative counts with IllegalArgumentException")
    void assertWithinBudget_rejectsNegativeCount() {
        ConfiguredToolCallBudgetPolicy policy = new ConfiguredToolCallBudgetPolicy(5);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> policy.assertWithinBudget(-1));
        assertTrue(ex.getMessage().contains("toolCallCount"),
            "exception message must name the parameter; got: " + ex.getMessage());
    }
}