package com.ar.crm2.whatsapp.application.mensaje.port.out;

/**
 * Almacena el media entrante de WhatsApp en disco local y lo sirve por /api/media/{archivo}
 * SOLO para mostrarlo dentro del CRM (a WhatsApp se manda base64 directo, Evolution nunca
 * necesita alcanzar esta URL). Espejo de AmbarCRM/.../lib/storage.ts.
 */
public interface MediaStoragePort {

    /** Guarda un base64 (con o sin prefijo data:) y devuelve la URL servible. */
    String guardarBase64(String base64, String mime);

    /** Lee un archivo del directorio de uploads; null si no existe. */
    byte[] leer(String nombre);

    /** Content-type a partir de la extensión del nombre de archivo. */
    String mimeDeArchivo(String nombre);
}
