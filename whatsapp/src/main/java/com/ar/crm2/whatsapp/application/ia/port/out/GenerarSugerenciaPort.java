package com.ar.crm2.whatsapp.application.ia.port.out;

/** Puerto al proveedor de IA (ej. Anthropic). Recibe el historial formateado y devuelve la sugerencia. */
public interface GenerarSugerenciaPort {
    String generar(String historial);
}
