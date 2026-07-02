package com.ar.crm2.adapter.out.ai;

import com.ar.crm2.application.ai.port.out.AiToolContextPort;
import com.ar.crm2.application.ai.port.out.dto.AiToolContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RED-first tests for {@link AiToolContextAdapter} — the production
 * infrastructure output adapter for {@link AiToolContextPort}.
 *
 * <p>The adapter is the seam that keeps the AI tool side from
 * receiving actor/tenant/conv scope through the model's payload. The
 * application {@code ProponerAccionUseCase} calls
 * {@link AiToolContextPort#resolve()} during a tool invocation; this
 * adapter returns whatever {@link AiToolContextHolder} currently
 * exposes, or fails fast with {@link IllegalStateException} when no
 * context was bound.
 *
 * <p>Coverage targets:
 * <ul>
 *   <li>Happy path: returns the {@link AiToolContext} the holder
 *       exposes — instance-equality, no copy.</li>
 *   <li>Empty context: throws {@link IllegalStateException} when the
 *       holder returns {@code null} so the use case refuses to stage
 *       an unscoped proposal (no silent default).</li>
 *   <li>Architecture guard: implements the application-owned
 *       {@link AiToolContextPort}; takes exactly one constructor
 *       argument ({@link AiToolContextHolder}).</li>
 *   <li>Adapter does not introduce a setter, a default context, or a
 *       static cache — the resolver is a one-way read.</li>
 * </ul>
 */
class AiToolContextAdapterTest {

    private static final UUID ACTOR = UUID.fromString("aaaaaaaa-1111-2222-3333-222222222222");
    private static final UUID EMPRESA = UUID.fromString("bbbbbbbb-1111-2222-3333-222222222222");
    private static final UUID AI_CONV = UUID.fromString("cccccccc-1111-2222-3333-222222222222");

    /** Hand-rolled fake holder — avoids a ThreadLocal in the unit test. */
    private static final class FakeHolder implements AiToolContextHolder {
        AiToolContext bound;
        @Override
        public AiToolContext current() { return bound; }
        @Override
        public void set(AiToolContext context) { this.bound = context; }
        @Override
        public void clear() { this.bound = null; }
    }

    private FakeHolder holder;
    private AiToolContextAdapter adapter;

    @BeforeEach
    void setUp() {
        holder = new FakeHolder();
        adapter = new AiToolContextAdapter(holder);
    }

    @Test
    @DisplayName("resolve() returns the same AiToolContext instance the holder exposes")
    void resolve_returnsHolderContext() {
        AiToolContext bound = new AiToolContext(ACTOR, EMPRESA, AI_CONV, "wa-conv-77");
        holder.set(bound);

        AiToolContext resolved = adapter.resolve();

        assertNotNull(resolved, "resolver must return a non-null context when the holder has one");
        assertSame(bound, resolved,
            "resolver must return the exact instance the holder exposes — no copy, no projection");
        assertEquals(ACTOR, resolved.actorUsuarioId());
        assertEquals(EMPRESA, resolved.empresaId());
        assertEquals(AI_CONV, resolved.aiConversacionId());
        assertEquals("wa-conv-77", resolved.waConversacionId());
    }

    @Test
    @DisplayName("resolve() throws IllegalStateException when the holder has no context — no silent default")
    void resolve_throwsWhenHolderIsEmpty() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> adapter.resolve(),
            "missing context must surface as IllegalStateException so the use case "
                + "refuses to stage an unscoped proposal");
        assertNotNull(ex.getMessage(),
            "the IllegalStateException must carry an explanatory message — operators "
                + "need to diagnose why the tool ran outside the AI flow");
        assertTrue(ex.getMessage().toLowerCase().contains("context")
                || ex.getMessage().toLowerCase().contains("aitool"),
            "the error message must mention the missing context, not the holder or "
                + "the adapter internals; got: " + ex.getMessage());
    }

    @Test
    @DisplayName("resolve() picks up changes made to the holder after construction (no caching)")
    void resolve_doesNotCache() {
        AiToolContext first = new AiToolContext(ACTOR, EMPRESA, AI_CONV, "wa-first");
        holder.set(first);
        assertSame(first, adapter.resolve(), "first resolve must return the first bound context");

        AiToolContext second = new AiToolContext(ACTOR, EMPRESA, AI_CONV, "wa-second");
        holder.set(second);
        assertSame(second, adapter.resolve(),
            "second resolve must return the second bound context — the adapter "
                + "must NOT cache the result across calls");
    }

    @Test
    @DisplayName("Adapter implements the application-owned AiToolContextPort")
    void implementsAiToolContextPort() {
        assertTrue(AiToolContextPort.class.isAssignableFrom(AiToolContextAdapter.class),
            "AiToolContextAdapter must implement AiToolContextPort — the application "
                + "service depends on the port, not on the adapter type");
    }

    @Test
    @DisplayName("Constructor takes exactly one AiToolContextHolder collaborator — no Spring / framework types")
    void constructor_hasSingleHolderCollaborator() {
        Constructor<?>[] ctors = AiToolContextAdapter.class.getDeclaredConstructors();
        assertEquals(1, ctors.length,
            "AiToolContextAdapter must expose exactly one constructor (Lombok @RequiredArgsConstructor)");

        Class<?>[] params = ctors[0].getParameterTypes();
        assertEquals(1, params.length,
            "AiToolContextAdapter must take exactly one parameter — the AiToolContextHolder");
        assertEquals(AiToolContextHolder.class, params[0],
            "the single collaborator must be AiToolContextHolder — the adapter must NOT "
                + "take Spring, ThreadLocal, ActorContext, or any concrete holder type");
    }
}
