package com.ar.crm2.adapter.out.ai;

import com.ar.crm2.application.ai.port.out.dto.AiToolContext;

/**
 * Infrastructure-owned holder that exposes the current
 * {@link AiToolContext} for AI tool invocations on the same thread
 * that called {@code ChatClient.prompt().call()}.
 *
 * <p><b>Purpose.</b> The trusted tool scope (actor / tenant / conv ids)
 * MUST be resolved at tool-invocation time and MUST NOT travel through
 * the model's payload — that would defeat the safety boundary. The
 * production wiring populates the holder on the request thread
 * (typically from a filter/interceptor wrapping
 * {@code ChatClient.call()}) and clears it on the way out. The
 * {@link AiToolContextAdapter} reads from the same holder.
 *
 * <p><b>Why an interface and not a concrete class.</b> The holder is
 * the smallest infrastructure-owned seam for the trusted scope: tests
 * inject hand-rolled fakes (see {@code AiToolContextAdapterTest} and
 * {@code ThreadLocalAiToolContextHolderTest}); future infra flows
 * (e.g. MDC-backed, request-attribute-backed) can ship alternative
 * implementations without touching the application layer.
 *
 * <p><b>Thread model.</b> Spring AI invokes the {@code @Tool} methods
 * on the same thread that called {@code ChatClient.call()}, so the
 * default {@link ThreadLocalAiToolContextHolder} implementation is
 * sufficient for the current flow. Anything that switches to async /
 * reactive tool invocation MUST revisit the holder strategy first.
 */
public interface AiToolContextHolder {

    /**
     * Returns the context bound to the current thread, or {@code null}
     * if no context is bound.
     *
     * @return the current {@link AiToolContext}, or {@code null}
     */
    AiToolContext current();

    /**
     * Binds {@code context} as the current thread's AI tool context.
     * A subsequent call MUST overwrite any previously bound value on
     * the same thread.
     *
     * @param context the trusted scope to expose to AI tool calls
     */
    void set(AiToolContext context);

    /**
     * Removes the current thread's binding. After this call,
     * {@link #current()} MUST return {@code null} on the same thread.
     */
    void clear();
}
