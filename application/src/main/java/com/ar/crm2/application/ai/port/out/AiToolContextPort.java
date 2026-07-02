package com.ar.crm2.application.ai.port.out;

import com.ar.crm2.application.ai.port.out.dto.AiToolContext;

/**
 * Outbound port for resolving the trusted AI tool context.
 *
 * <p>AI tools are invoked by the model inside the same thread that
 * called {@code ChatClient.prompt().call()}; the resolver reads the
 * {@link AiToolContext} from whatever source the wiring chose
 * (MDC/ThreadLocal by convention for PR 4) so the tool never has to
 * receive actor/tenant identity through the model's payload — that
 * would defeat the safety boundary.
 *
 * <p>This interface lives in {@code application.ai.port.out} because
 * it is consumed by the application service that coordinates the
 * staging use case. The concrete implementation lives in
 * infrastructure and is wired manually in {@code AiWiringConfig} (PR 4);
 * tests inject their own in-memory implementation.
 *
 * <p>Implementations MUST throw {@link IllegalStateException} if the
 * context is missing — the use case will surface the error to the
 * caller so a tool call without scope cannot accidentally stage an
 * unscoped proposal.
 */
public interface AiToolContextPort {

    /**
     * Resolves the current tool context.
     *
     * @return non-null {@link AiToolContext} with actor/tenant/conv ids
     * @throws IllegalStateException if no context is available — the
     *         use case MUST refuse to stage a proposal without one
     */
    AiToolContext resolve();
}
