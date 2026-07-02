package com.ar.crm2.model.enums;

/**
 * Visibility scope of an AI memory record.
 *
 * <p>Memories are NEVER company-wide or global — they are always
 * private to the (actorUsuarioId, empresaId) scope, and additionally
 * scoped to either a single WhatsApp conversation OR a single contact.
 *
 * <p>This enum drives which foreign key (waConversacionId OR contactoId)
 * is required at construction time.
 */
public enum VisibilidadMemoria {
    /** Memory scoped to one WhatsApp conversation. */
    CONVERSACION_SCOPED,
    /** Memory scoped to one contact (cross-conversation). */
    CONTACTO_SCOPED
}