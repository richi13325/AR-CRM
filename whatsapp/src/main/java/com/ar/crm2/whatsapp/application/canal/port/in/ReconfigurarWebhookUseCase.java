package com.ar.crm2.whatsapp.application.canal.port.in;

import java.util.UUID;

/**
 * Vuelve a registrar el webhook de Evolution para un canal YA conectado, sin
 * tener que re-escanear el QR. Útil cuando WA_WEBHOOK_BASE_URL se configura o
 * cambia después de haber vinculado el número.
 */
public interface ReconfigurarWebhookUseCase {
    void reconfigurar(UUID canalId);
}
