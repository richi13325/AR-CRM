package com.ar.crm2.adapter.out.ai;

import com.ar.crm2.application.ai.port.out.dto.AiToolContext;

/**
 * Default {@link AiToolContextHolder} backed by a
 * {@link ThreadLocal}.
 *
 * <p>Spring AI invokes {@code @Tool} methods on the same thread that
 * called {@code ChatClient.prompt().call()}, and
 * {@code OpenAiChatAdapter.generar(...)} runs the chat call
 * synchronously on the request thread (see the audit-#1 closing
 * notes in {@code apply-progress.md} Slice 12). That combination makes
 * a {@code ThreadLocal} the smallest coherent mechanism to flow the
 * request-scoped trusted context into the tool without leaking it
 * through the model's payload or across pooled threads.
 *
 * <p><b>Thread isolation.</b> a value bound on thread A is invisible
 * to thread B — the {@code ThreadLocalAiToolContextHolderTest} pins
 * this property so a future refactor cannot silently downgrade it to
 * a static field (which would mix actor/tenant scope across pooled
 * request threads).
 *
 * <p><b>Lifecycle.</b> the production wiring is responsible for
 * {@link #set(AiToolContext)} on the way in and {@link #clear()} on
 * the way out (the {@code OpenAiChatAdapter.generar(...)} try-finally
 * around {@code ChatClient.call()} enforces this). A leaked binding on
 * a pooled thread would expose one request's scope to the next —
 * {@code ThreadLocalAiToolContextHolderTest}'s {@code tearDown} also
 * clears defensively.
 *
 * <p><b>Async / reactive.</b> anything that crosses thread boundaries
 * between {@code set(...)} and the tool invocation MUST revisit this
 * strategy (e.g. an MDC-backed or reactor-context-aware holder, or a
 * {@code ScopedValue}-based carrier on JDK 21+). Wrapping
 * {@code ChatClient.call()} in
 * {@code CompletableFuture.supplyAsync(...).orTimeout(...)} — which
 * the previous {@code OpenAiChatAdapter} revision tried — would
 * silently break the binding and must NOT be reintroduced.
 */
public class ThreadLocalAiToolContextHolder implements AiToolContextHolder {

    private final ThreadLocal<AiToolContext> current = new ThreadLocal<>();

    @Override
    public AiToolContext current() {
        return current.get();
    }

    @Override
    public void set(AiToolContext context) {
        current.set(context);
    }

    @Override
    public void clear() {
        current.remove();
    }
}
