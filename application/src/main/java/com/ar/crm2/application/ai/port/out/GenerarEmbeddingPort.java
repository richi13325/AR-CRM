package com.ar.crm2.application.ai.port.out;

import java.util.List;

/**
 * Outbound port for generating vector embeddings of AI memory records.
 *
 * <p>Phase 1 ships a deterministic placeholder (no real embeddings).
 * The contract stays the same so PR 2+ can swap in a real provider
 * (Spring AI embedding model or a third-party vector service).
 */
public interface GenerarEmbeddingPort {

    /**
     * Generates the embedding vector for a single text snippet.
     *
     * @param texto the text to embed
     * @return the embedding vector (length provider-defined)
     */
    List<Double> embed(String texto);
}