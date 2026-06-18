package com.ar.crm2.whatsapp.application.conversacion.port.in;

/** Cierra conversaciones sin actividad (llamado por un cron de n8n vía /api/cron/auto-resolver). */
public interface AutoResolverConversacionesUseCase {
    int resolverInactivas(int horasInactividad);
}
