package com.ar.crm2.model.enums;

/**
 * Origin of an AI memory record.
 *
 * <p>Used by the assistant context loader to weight and label memories.
 * Memories are always private to the requester scope (see
 * {@link VisibilidadMemoria}); the origin only affects provenance.
 */
public enum OrigenMemoria {
    /** Memory extracted during the initial chat analysis flow. */
    CHAT_ANALYSIS,
    /** Memory extracted during a follow-up turn. */
    FOLLOW_UP,
    /** Memory inserted by an explicit user or operator action. */
    MANUAL,
    /** Memory derived from a confirmed AI action proposal. */
    PROPOSAL
}