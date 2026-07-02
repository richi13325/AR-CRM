package com.ar.crm2.adapter.out.ai.spring;

import com.ar.crm2.application.ai.port.out.GenerarEmbeddingPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RED-first tests for {@link GenerarEmbeddingStubAdapter} — the phase-1
 * deterministic placeholder for {@link GenerarEmbeddingPort}.
 *
 * <p>The stub is intentionally narrow:
 * <ul>
 *   <li>It returns a deterministic non-empty vector for every input —
 *       the SAME text MUST yield the same vector across calls (no
 *       randomness), so memory deduplication and tests stay stable.</li>
 *   <li>It distinguishes between texts (different texts produce
 *       different vectors) so the placeholder still has some signal —
 *       it is not a constant zero vector.</li>
 *   <li>It has a stable, fixed length so any downstream consumer
 *       that pre-allocates a buffer based on the embedding size does
 *       not have to handle a variable-length vector.</li>
 *   <li>No Spring AI / OpenAI / external provider is touched; phase 1
 *       has no real embedding backend wired.</li>
 * </ul>
 *
 * <p>Reflection guards pin the contract so a future change that
 * introduces a dependency on a framework type fails the test before
 * review.
 */
class GenerarEmbeddingStubAdapterTest {

    @Test
    @DisplayName("embed returns a non-null, non-empty vector of stable length")
    void embed_returnsNonEmptyVectorOfStableLength() {
        GenerarEmbeddingPort port = new GenerarEmbeddingStubAdapter();
        List<Double> vector = port.embed("hola mundo");
        assertNotNull(vector, "embed must return a non-null vector");
        assertFalse(vector.isEmpty(), "embed must return a non-empty vector");
        assertEquals(GenerarEmbeddingStubAdapter.EMBEDDING_DIMENSION, vector.size(),
            "embedding dimension must be the documented stable size so downstream "
                + "callers can pre-allocate without re-checking per call");
    }

    @Test
    @DisplayName("embed is deterministic — the same text produces the SAME vector across calls")
    void embed_isDeterministic() {
        GenerarEmbeddingPort port = new GenerarEmbeddingStubAdapter();
        List<Double> first = port.embed("cliente quiere demo");
        List<Double> second = port.embed("cliente quiere demo");
        assertEquals(first, second,
            "the stub MUST be deterministic — same text, same vector across calls. "
                + "Non-deterministic embeddings would silently break deduplication and "
                + "make tests flaky.");
    }

    @Test
    @DisplayName("embed distinguishes inputs — different texts produce different vectors")
    void embed_distinguishesInputs() {
        GenerarEmbeddingPort port = new GenerarEmbeddingStubAdapter();
        List<Double> a = port.embed("cliente quiere demo");
        List<Double> b = port.embed("cliente cancela pedido");
        assertFalse(a.equals(b),
            "different texts MUST produce different vectors — the stub is a placeholder "
                + "but it must still carry some signal so the model does not see every "
                + "memory collapse into one cluster");
    }

    @Test
    @DisplayName("embed never returns a zero vector — at least one component is non-zero")
    void embed_returnsNonZeroVector() {
        GenerarEmbeddingPort port = new GenerarEmbeddingStubAdapter();
        List<Double> vector = port.embed("cualquier texto");
        boolean anyNonZero = vector.stream().anyMatch(d -> d != 0.0);
        assertTrue(anyNonZero,
            "the stub MUST NOT return a constant zero vector — that would teach the model "
                + "to treat every input as identical. At least one component must be non-zero.");
    }

    @Test
    @DisplayName("embed handles unicode / non-ASCII text without throwing")
    void embed_handlesUnicodeText() {
        GenerarEmbeddingPort port = new GenerarEmbeddingStubAdapter();
        List<Double> vector = port.embed("ñoño — déjà vu — ñandú");
        assertNotNull(vector);
        assertEquals(GenerarEmbeddingStubAdapter.EMBEDDING_DIMENSION, vector.size(),
            "unicode input MUST yield a stable-size vector; the stub does not normalize");
    }

    @Test
    @DisplayName("embed handles empty input without throwing and still returns the stable vector shape")
    void embed_handlesEmptyInput() {
        GenerarEmbeddingPort port = new GenerarEmbeddingStubAdapter();
        List<Double> vector = port.embed("");
        assertNotNull(vector);
        assertEquals(GenerarEmbeddingStubAdapter.EMBEDDING_DIMENSION, vector.size(),
            "empty input MUST still return a stable-shape vector — the call MUST NOT throw");
    }

    @Test
    @DisplayName("Different texts produce mostly different vectors — collision rate is low")
    void embed_lowCollisionRate() {
        GenerarEmbeddingPort port = new GenerarEmbeddingStubAdapter();
        Set<List<Double>> seen = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            seen.add(port.embed("texto número " + i));
        }
        assertTrue(seen.size() >= 45,
            "across 50 distinct inputs the stub MUST distinguish at least 45 of them — "
                + "a collision rate above ~10% would mean the placeholder carries too "
                + "little signal to be useful even as a stub. Got "
                + seen.size() + " distinct vectors.");
    }

    @Test
    @DisplayName("Adapter implements GenerarEmbeddingPort and exposes a parameterless constructor")
    void adapter_isParameterlessAndImplementsPort() throws NoSuchMethodException {
        Constructor<GenerarEmbeddingStubAdapter> ctor =
            GenerarEmbeddingStubAdapter.class.getDeclaredConstructor();
        assertNotNull(ctor,
            "the stub adapter MUST expose a parameterless constructor so the boot "
                + "module can wire it as a simple @Bean factory");
        assertTrue(GenerarEmbeddingPort.class.isAssignableFrom(GenerarEmbeddingStubAdapter.class),
            "the adapter MUST implement the application output port so the AI "
                + "application layer can inject it without knowing about the stub");
    }

    @Test
    @DisplayName("Adapter declares zero non-static fields — embedding generation is stateless")
    void adapter_isStateless() {
        long instanceFieldCount = 0;
        for (Field f : GenerarEmbeddingStubAdapter.class.getDeclaredFields()) {
            if (!java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
                instanceFieldCount++;
            }
        }
        assertEquals(0L, instanceFieldCount,
            "the stub must declare zero non-static fields — embedding generation is "
                + "stateless (determinism comes from hashing the input, not from state). "
                + "A future instance field would suggest hidden state that could break "
                + "the determinism contract. The public EMBEDDING_DIMENSION constant is "
                + "allowed because it is static and final.");
    }
}