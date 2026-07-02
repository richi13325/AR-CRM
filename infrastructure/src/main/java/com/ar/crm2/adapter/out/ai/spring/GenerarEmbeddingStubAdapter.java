package com.ar.crm2.adapter.out.ai.spring;

import com.ar.crm2.application.ai.port.out.GenerarEmbeddingPort;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Phase-1 deterministic placeholder for {@link GenerarEmbeddingPort}.
 *
 * <p>Phase 1 ships NO real embedding backend (no Spring AI embedding
 * model, no OpenAI embeddings, no vector store). The AI assistant
 * pipeline that would normally use this port (e.g. memory retrieval
 * beyond the deterministic filters) is intentionally out of scope —
 * see {@code ai-memory/spec.md} §"Defer embeddings and semantic
 * retrieval safely" and {@code proposal.md} §"Out of Scope (Phase 1)".
 *
 * <p>The contract that downstream callers depend on is preserved:
 * <ul>
 *   <li><b>Deterministic</b> — the SAME text yields the SAME vector
 *       across calls and across JVM restarts (the hash is SHA-256 of
 *       the UTF-8 bytes, stable everywhere).</li>
 *   <li><b>Stable size</b> — every vector is exactly
 *       {@link #EMBEDDING_DIMENSION} doubles, so callers can
 *       pre-allocate buffers without re-checking.</li>
 *   <li><b>Distinguishes inputs</b> — different texts produce
 *       different vectors (SHA-256 avalanche property); the placeholder
 *       still carries enough signal to support basic memory
 *       dedup tests.</li>
 *   <li><b>Non-zero</b> — the stub never returns a constant zero
 *       vector, so the model never treats two unrelated inputs as
 *       identical.</li>
 *   <li><b>Stateless</b> — no instance fields, no caches, no IO. A
 *       future field would suggest hidden state that could break the
 *       determinism contract.</li>
 * </ul>
 *
 * <p><b>Phase 2 swap-in.</b> the port interface is the contract; phase
 * 2 will provide a real implementation (Spring AI embedding model or
 * a third-party vector service) that drops in behind the same port.
 */
public class GenerarEmbeddingStubAdapter implements GenerarEmbeddingPort {

    /**
     * Stable vector length exposed to callers. Documented publicly so
     * downstream code that pre-allocates buffers knows what to expect.
     */
    public static final int EMBEDDING_DIMENSION = 32;

    @Override
    public List<Double> embed(String texto) {
        // Hash the text. SHA-256 is universally available in the JDK
        // (java.security) and is deterministic across JVMs and
        // restarts. Empty / null inputs are tolerated — we just hash
        // an empty byte array.
        String safe = texto == null ? "" : texto;
        byte[] digest = sha256(safe.getBytes(StandardCharsets.UTF_8));

        List<Double> vector = new ArrayList<>(EMBEDDING_DIMENSION);
        for (int i = 0; i < EMBEDDING_DIMENSION; i++) {
            // Map each dimension to a derived byte from the SHA-256
            // digest. We XOR-fold 8 bytes per dimension so each
            // dimension has more entropy than a single byte.
            int idx = (i * 8) % digest.length;
            int combined = 0;
            for (int j = 0; j < 8; j++) {
                combined ^= (digest[(idx + j) % digest.length] & 0xFF) << (j * 2);
            }
            // Project the 16-bit signed value into [-1.0, 1.0]. The
            // division gives us roughly that range with reasonable
            // spread — enough signal for a placeholder.
            double value = (combined % 1000) / 1000.0;
            vector.add(value);
        }
        return vector;
    }

    private static byte[] sha256(byte[] input) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(input);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is mandated by every Java SE distribution; if
            // it is somehow unavailable the JVM is unusable and the
            // embedding cannot proceed.
            throw new IllegalStateException(
                "SHA-256 unavailable — cannot generate placeholder embedding", e);
        }
    }
}