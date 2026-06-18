package com.ar.crm2.whatsapp.application.canal.port.out;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface EvolutionConectarPort {
    String conectarInstancia(String apiUrl, String apiKey, String instanceName);
    String consultarEstado(String apiUrl, String apiKey, String instanceName);
    List<Map<String, Object>> fetchContacts(String apiUrl, String apiKey, String instanceName);
    List<Map<String, Object>> fetchChats(String apiUrl, String apiKey, String instanceName);
    List<Map<String, Object>> fetchMessages(String apiUrl, String apiKey, String instanceName, String remoteJid, int limit);

    /**
     * Descarga el contenido real (base64) de un media entrante (cifrado en WhatsApp).
     * Devuelve null si el mensaje no tiene media o si Evolution falla.
     */
    MediaDescargada descargarMedia(String apiUrl, String apiKey, String instanceName, Object rawMessage);

    /** Nombre/asunto de un grupo (@g.us). null si no se pudo obtener. */
    String infoGrupo(String apiUrl, String apiKey, String instanceName, String grupoJid);

    /** Lista los grupos del número: cada item {jid, nombre}. */
    List<Map<String, Object>> listarGruposWA(String apiUrl, String apiKey, String instanceName);

    /**
     * Registra el webhook en Evolution para que mande los mensajes entrantes
     * en tiempo real a nuestro backend. No-op silencioso si no hay una URL
     * pública configurada (ej. dev local sin túnel expuesto).
     */
    void configurarWebhook(String apiUrl, String apiKey, String instanceName, UUID canalId);
}
