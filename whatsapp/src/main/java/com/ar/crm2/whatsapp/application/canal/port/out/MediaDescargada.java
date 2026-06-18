package com.ar.crm2.whatsapp.application.canal.port.out;

/** Contenido real (base64) de un media de WhatsApp ya descargado de Evolution. */
public record MediaDescargada(String base64, String mime) {}
