package com.ar.crm2.adapter.out.ai;

import com.ar.crm2.application.ai.port.out.AiToolContextPort;
import com.ar.crm2.application.ai.port.out.dto.AiToolContext;
import lombok.RequiredArgsConstructor;

/**
 * Production output adapter for the application-owned
 * {@link AiToolContextPort}.
 *
 * <p>The adapter reads the trusted {@link AiToolContext} from the
 * current thread via the injected {@link AiToolContextHolder}. The
 * default holder is {@link ThreadLocalAiToolContextHolder}; tests
 * inject a hand-rolled fake holder.
 *
 * <p><b>Safety boundary.</b> the trusted scope (actor / tenant /
 * conv ids) MUST come from infrastructure-resolved state, never from
 * the model's payload. This adapter exists precisely so the
 * {@code ProponerAccionUseCase} can resolve that scope without the
 * model being able to spoof it.
 *
 * <p><b>Failure mode.</b> when no context is bound to the current
 * thread the adapter throws {@link IllegalStateException}. The use
 * case will refuse to stage a proposal in that case so a tool call
 * without scope cannot accidentally stage an unscoped proposal —
 * matching the contract documented on {@link AiToolContextPort}.
 *
 * <p><b>Wiring.</b> the adapter is registered as an explicit
 * {@code @Bean} in the boot module's {@code WiringConfig} (the
 * project's composition root), consistent with every other
 * infrastructure output adapter. The holder is wired once as a
 * singleton so the same instance backs every tool invocation.
 */
@RequiredArgsConstructor
public class AiToolContextAdapter implements AiToolContextPort {

    private final AiToolContextHolder holder;

    @Override
    public AiToolContext resolve() {
        AiToolContext ctx = holder.current();
        if (ctx == null) {
            throw new IllegalStateException(
                "No AiToolContext bound on the current thread — the AI tool was "
                    + "invoked outside the request-scoped AI flow. The use case "
                    + "refuses to stage a proposal without trusted actor/tenant/conv scope."
            );
        }
        return ctx;
    }
}
