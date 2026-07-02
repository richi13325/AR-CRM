package com.ar.crm2.model.enums;

/**
 * Discriminator for AI-suggested CRM action proposals.
 *
 * <p>Each value is paired with a typed payload record at the application
 * boundary (see design.md §2.2). The domain only knows the discriminator;
 * schema validation lives at the application layer per
 * {@code TipoAccion}.
 */
public enum TipoAccion {
    CREATE_CONTACTO,
    CREATE_TRATO,
    CREATE_TAREA,
    MOVE_KANBAN_FICHA
}