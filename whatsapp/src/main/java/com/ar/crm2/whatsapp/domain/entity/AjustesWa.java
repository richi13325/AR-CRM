package com.ar.crm2.whatsapp.domain.entity;

/**
 * Ajustes de automatización del módulo WhatsApp (fila única). Espejo simplificado
 * del modelo Ajustes de AmbarCRM. Inmutable; se reemplaza completo al guardar.
 */
public record AjustesWa(
        boolean autoAsignar,
        boolean bienvenidaActiva,
        String bienvenidaTexto,
        boolean horarioActivo,
        String horarioInicio,      // "HH:mm"
        String horarioFin,         // "HH:mm"
        String horarioDias,        // CSV de días 1-7 (lun-dom), ej "1,2,3,4,5"
        String fueraHorarioTexto,
        boolean csatActivo,
        String csatTexto           // pregunta de la encuesta de satisfacción
) {
    public static AjustesWa defaults() {
        return new AjustesWa(
                false,
                false, "¡Hola! Gracias por escribirnos. En breve te atenderemos.",
                false, "09:00", "18:00", "1,2,3,4,5",
                "Estamos fuera de horario de atención. Te responderemos a la brevedad.",
                false,
                "¿Cómo calificarías nuestra atención del 1 (mala) al 5 (excelente)?");
    }
}
