package com.ar.crm2.model.enums;

/**
 * Role of a turn inside an AI conversation.
 *
 * <p>Drives how an {@code AiMensaje} is interpreted by the model context
 * and the UI. Source-of-truth for the original WhatsApp transcript remains
 * {@code wa_mensaje}; AI messages store assistant/user interaction only.
 */
public enum RolMensajeAi {
    USER,
    ASSISTANT,
    SYSTEM,
    TOOL
}