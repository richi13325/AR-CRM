package com.ar.crm2.whatsapp.application.conversacion.port.out;

public interface CsatResumenPort {
    /** Promedio (null si no hay respuestas) y total de calificaciones CSAT. */
    record CsatResumen(Double promedio, long total) {}

    CsatResumen obtener();
}
