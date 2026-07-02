package com.ar.crm2.adapter.out.ai;

import com.ar.crm2.application.ai.port.out.dto.AiToolContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * RED-first tests for {@link ThreadLocalAiToolContextHolder}.
 *
 * <p>The holder is the infrastructure-owned mechanism that lets the
 * production wiring bind the trusted {@link AiToolContext} to the
 * current thread before the AI tool is invoked (typically from a
 * filter/interceptor around {@code ChatClient.call()}) and clear it
 * afterwards. The {@link AiToolContextAdapter} reads from the same
 * holder; the holder is what keeps the trusted scope off the model
 * payload.
 *
 * <p>Coverage targets:
 * <ul>
 *   <li>Happy path: {@code current()} returns the bound context.</li>
 *   <li>Initial state: {@code current()} returns {@code null} when
 *       nothing was bound on this thread.</li>
 *   <li>{@code clear()} removes the bound context.</li>
 *   <li>Thread isolation: a value bound on thread A MUST NOT leak
 *       into thread B; this is the whole point of using a
 *       {@code ThreadLocal} and the safety property the resolver
 *       depends on.</li>
 * </ul>
 */
class ThreadLocalAiToolContextHolderTest {

    private static final UUID ACTOR = UUID.fromString("aaaaaaaa-1111-2222-3333-111111111111");
    private static final UUID EMPRESA = UUID.fromString("bbbbbbbb-1111-2222-3333-111111111111");
    private static final UUID AI_CONV = UUID.fromString("cccccccc-1111-2222-3333-111111111111");

    private ThreadLocalAiToolContextHolder holder;

    @BeforeEach
    void setUp() {
        holder = new ThreadLocalAiToolContextHolder();
    }

    @AfterEach
    void tearDown() {
        // Defensive cleanup so a leaked thread-local in a pooled test
        // thread never contaminates another test class.
        holder.clear();
    }

    @Test
    @DisplayName("current() returns null when no context was bound on this thread")
    void current_isNullWhenUnset() {
        assertNull(holder.current(),
            "an empty holder must return null — the adapter will refuse to stage "
                + "an unscoped proposal");
    }

    @Test
    @DisplayName("set(...) + current() round-trips the bound context on the same thread")
    void set_thenCurrent_returnsSameInstance() {
        AiToolContext bound = new AiToolContext(ACTOR, EMPRESA, AI_CONV, "wa-1");
        holder.set(bound);

        assertSame(bound, holder.current(),
            "current() must return the exact instance that was bound — no copy, "
                + "no projection");
    }

    @Test
    @DisplayName("clear() removes the bound context so current() returns null again")
    void clear_removesBoundContext() {
        holder.set(new AiToolContext(ACTOR, EMPRESA, AI_CONV, "wa-2"));
        holder.clear();

        assertNull(holder.current(),
            "after clear(), current() must return null — leaving the bound value "
                + "would risk cross-request contamination in a pooled thread");
    }

    @Test
    @DisplayName("set(...) overrides a previously bound context on the same thread")
    void set_overridesPreviousValue() {
        AiToolContext first = new AiToolContext(ACTOR, EMPRESA, AI_CONV, "wa-first");
        AiToolContext second = new AiToolContext(ACTOR, EMPRESA, AI_CONV, "wa-second");

        holder.set(first);
        holder.set(second);

        assertSame(second, holder.current(),
            "a later set(...) must replace the previous value — the latest binding "
                + "wins because it reflects the current request scope");
        assertNotSame(first, holder.current(),
            "current() must NOT return the superseded instance");
    }

    @Test
    @DisplayName("A value bound on one thread is invisible to another thread (ThreadLocal isolation)")
    void current_isIsolatedPerThread() throws InterruptedException {
        AiToolContext mainThreadCtx = new AiToolContext(
            ACTOR, EMPRESA, AI_CONV, "wa-main-thread"
        );
        holder.set(mainThreadCtx);

        CountDownLatch childHasRun = new CountDownLatch(1);
        CountDownLatch parentMayCheck = new CountDownLatch(1);
        AtomicReference<AiToolContext> childObserved = new AtomicReference<>();
        AtomicReference<AiToolContext> childAfterOwnSet = new AtomicReference<>();

        Thread child = new Thread(() -> {
            // The child thread must NOT see the parent thread's binding.
            childObserved.set(holder.current());

            // The child thread can independently bind its own context
            // without disturbing the parent.
            AiToolContext childCtx = new AiToolContext(
                ACTOR, EMPRESA, AI_CONV, "wa-child-thread"
            );
            holder.set(childCtx);
            childAfterOwnSet.set(holder.current());

            childHasRun.countDown();
            try {
                parentMayCheck.await(2, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }, "ai-tool-context-holder-isolation-test");

        child.start();
        childHasRun.await(2, TimeUnit.SECONDS);
        parentMayCheck.countDown();
        child.join(2_000);

        assertNull(childObserved.get(),
            "a child thread MUST NOT see the parent thread's binding — that would "
                + "leak actor/tenant scope across requests");
        assertEquals("wa-child-thread", childAfterOwnSet.get().waConversacionId(),
            "the child thread's own set(...) must take effect on the child thread only");

        // The parent thread's binding must still be intact after the child
        // manipulated its own ThreadLocal.
        assertSame(mainThreadCtx, holder.current(),
            "the parent thread's binding MUST be unaffected by the child thread's "
                + "set/clear — ThreadLocal isolation must hold in both directions");
    }
}
