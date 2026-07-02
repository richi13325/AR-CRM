package com.ar.crm2.model.enums;

/**
 * Lifecycle state of an AI-suggested CRM action proposal.
 *
 * <p>State machine (driven by AiAccion domain methods):
 * <pre>
 *   PENDING ──confirmar()──► CONFIRMED ──marcarEjecutada()──► EXECUTED
 *      │                          │
 *      │                          └──marcarFallida()──────► FAILED
 *      ├──rechazar()──► REJECTED
 *      └──expirar()────► EXPIRED
 * </pre>
 *
 * <p>Only PENDING proposals are confirmable by the original requester.
 * Terminal states (REJECTED, EXPIRED, EXECUTED, FAILED) reject any further
 * transition attempts.
 */
public enum EstadoAccion {
    PENDING,
    CONFIRMED,
    REJECTED,
    EXPIRED,
    EXECUTED,
    FAILED
}