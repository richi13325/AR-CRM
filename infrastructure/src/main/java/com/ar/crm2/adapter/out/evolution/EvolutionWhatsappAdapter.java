package com.ar.crm2.adapter.out.evolution;

import com.ar.crm2.security.WaProperties;
import com.ar.crm2.whatsapp.application.canal.port.out.EvolutionConectarPort;
import com.ar.crm2.whatsapp.application.mensaje.port.out.SendWhatsappMessagePort;
import com.ar.crm2.whatsapp.domain.entity.CanalWhatsapp;
import com.ar.crm2.whatsapp.domain.enums.TipoMensaje;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class EvolutionWhatsappAdapter implements SendWhatsappMessagePort, EvolutionConectarPort {

    private final WebClient.Builder webClientBuilder;
    private final WaProperties waProperties;

    @Override
    public String send(CanalWhatsapp canal, String numeroDestino, TipoMensaje tipo, String contenido, String mediaUrl) {
        WebClient client = buildClient(canal.getApiUrl(), canal.getApiKey());

        if (tipo == TipoMensaje.TEXTO) {
            return sendText(client, canal.getInstanceName(), numeroDestino, contenido);
        }
        if (tipo == TipoMensaje.IMAGEN || tipo == TipoMensaje.AUDIO || tipo == TipoMensaje.VIDEO || tipo == TipoMensaje.DOCUMENTO) {
            return sendMedia(client, canal.getInstanceName(), numeroDestino, tipo, mediaUrl, contenido);
        }
        throw new UnsupportedOperationException("Tipo de mensaje no soportado: " + tipo);
    }

    @Override
    public String conectarInstancia(String apiUrl, String apiKey, String instanceName) {
        WebClient client = buildClient(apiUrl, apiKey);

        // Crear instancia — el QR viene directamente en la respuesta
        try {
            Map<?, ?> createResponse = client.post()
                    .uri("/instance/create")
                    .bodyValue(Map.of("instanceName", instanceName, "qrcode", true, "integration", "WHATSAPP-BAILEYS"))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (createResponse != null) {
                Object qrcodeObj = createResponse.get("qrcode");
                if (qrcodeObj instanceof Map<?, ?> qrcode) {
                    Object base64 = qrcode.get("base64");
                    if (base64 != null && !base64.toString().isBlank()) {
                        return base64.toString();
                    }
                }
            }
        } catch (WebClientResponseException e) {
            int status = e.getStatusCode().value();
            // 409/403/400 = instancia ya existe (Evolution no es consistente con el código
            // exacto: puede responder 403 "already in use" o 400 si la creación está en
            // curso por una llamada concurrente). En esos casos pedimos el QR por separado
            // en vez de fallar.
            if (status != 409 && status != 403 && status != 400) throw e;
        }

        // Instancia ya existía — pedir QR fresh
        try {
            Map<?, ?> connectResponse = client.get()
                    .uri("/instance/connect/{instance}", instanceName)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            if (connectResponse == null) return null;
            Object base64 = connectResponse.get("base64");
            return base64 != null ? base64.toString() : null;
        } catch (Exception e) {
            log.warn("Evolution connect (QR) fallo (instance={}): {}", instanceName, e.getMessage());
            return null;
        }
    }

    @Override
    public String consultarEstado(String apiUrl, String apiKey, String instanceName) {
        WebClient client = buildClient(apiUrl, apiKey);
        try {
            Map<?, ?> response = client.get()
                    .uri("/instance/connectionState/{instance}", instanceName)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) return "close";
            Object instanceObj = response.get("instance");
            if (instanceObj instanceof Map<?, ?> inst) {
                Object state = inst.get("state");
                return state != null ? state.toString() : "close";
            }
            return "close";
        } catch (Exception e) {
            log.warn("Evolution consultarEstado fallo (instance={}): {}", instanceName, e.getMessage());
            return "close";
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> fetchContacts(String apiUrl, String apiKey, String instanceName) {
        WebClient client = buildClient(apiUrl, apiKey);
        try {
            List<?> response = client.post()
                    .uri("/chat/findContacts/{instance}", instanceName)
                    .bodyValue(Map.of())
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();
            if (response == null) return List.of();
            return (List<Map<String, Object>>) response;
        } catch (Exception e) {
            log.warn("Evolution fetchContacts fallo (instance={}): {}", instanceName, e.getMessage());
            return List.of();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> fetchChats(String apiUrl, String apiKey, String instanceName) {
        WebClient client = buildClient(apiUrl, apiKey);
        try {
            // Evolution expone /chat/findChats/{instance} como POST (no GET, devuelve 404).
            List<?> response = client.post()
                    .uri("/chat/findChats/{instance}", instanceName)
                    .bodyValue(Map.of())
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();
            if (response == null) return List.of();
            return (List<Map<String, Object>>) response;
        } catch (Exception e) {
            log.warn("Evolution fetchChats fallo (instance={}): {}", instanceName, e.getMessage());
            return List.of();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> fetchMessages(String apiUrl, String apiKey, String instanceName, String remoteJid, int limit) {
        WebClient client = buildClient(apiUrl, apiKey);
        try {
            Map<?, ?> response = client.post()
                    .uri("/chat/findMessages/{instance}", instanceName)
                    .bodyValue(Map.of(
                            "where", Map.of("key", Map.of("remoteJid", remoteJid)),
                            "limit", limit
                    ))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            if (response == null) return List.of();
            Object msgs = response.get("messages");
            if (msgs instanceof Map<?, ?> m) {
                Object records = m.get("records");
                if (records instanceof List<?>) return (List<Map<String, Object>>) records;
            }
            return List.of();
        } catch (Exception e) {
            log.warn("Evolution fetchMessages fallo (instance={}, jid={}): {}", instanceName, remoteJid, e.getMessage());
            return List.of();
        }
    }

    @Override
    public com.ar.crm2.whatsapp.application.canal.port.out.MediaDescargada descargarMedia(
            String apiUrl, String apiKey, String instanceName, Object rawMessage) {
        WebClient client = buildClient(apiUrl, apiKey);
        try {
            Map<?, ?> data = client.post()
                    .uri("/chat/getBase64FromMediaMessage/{instance}", instanceName)
                    .bodyValue(Map.of("message", soloKeyYMessage(rawMessage)))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            if (data == null) return null;
            Object b64 = data.get("base64");
            if (b64 == null || b64.toString().isBlank()) return null;
            Object mime = data.get("mimetype") != null ? data.get("mimetype") : data.get("mediaType");
            return new com.ar.crm2.whatsapp.application.canal.port.out.MediaDescargada(
                    b64.toString(), mime != null ? mime.toString() : "application/octet-stream");
        } catch (WebClientResponseException e) {
            // El body de la respuesta de Evolution dice exactamente qué rechaza (campo inválido, etc.).
            log.warn("Evolution descargarMedia fallo (instance={}): {} - respuesta: {}",
                    instanceName, e.getMessage(), e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            log.warn("Evolution descargarMedia fallo (instance={}): {}", instanceName, e.getMessage());
            return null;
        }
    }

    // getBase64FromMediaMessage de Evolution v2 valida el body de forma estricta:
    // espera el mensaje de WhatsApp ({key, message}) y rechaza con 400 si le llegan
    // campos extra del webhook (messageTimestamp, pushName, status, instanceId, ...).
    @SuppressWarnings("unchecked")
    private Object soloKeyYMessage(Object rawMessage) {
        if (rawMessage instanceof Map<?, ?> m) {
            Object key = m.get("key");
            Object message = m.get("message");
            if (key != null && message != null) {
                java.util.Map<String, Object> trimmed = new java.util.HashMap<>();
                trimmed.put("key", key);
                trimmed.put("message", message);
                return trimmed;
            }
        }
        return rawMessage;
    }

    @Override
    @SuppressWarnings("unchecked")
    public String infoGrupo(String apiUrl, String apiKey, String instanceName, String grupoJid) {
        WebClient client = buildClient(apiUrl, apiKey);
        try {
            Map<?, ?> data = client.get()
                    .uri(uri -> uri.path("/group/findGroupInfos/{instance}")
                            .queryParam("groupJid", grupoJid).build(instanceName))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            if (data == null) return null;
            Object subject = data.get("subject");
            return subject != null ? subject.toString() : null;
        } catch (Exception e) {
            log.warn("Evolution infoGrupo fallo (instance={}, grupoJid={}): {}", instanceName, grupoJid, e.getMessage());
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listarGruposWA(String apiUrl, String apiKey, String instanceName) {
        WebClient client = buildClient(apiUrl, apiKey);
        try {
            List<?> response = client.get()
                    .uri(uri -> uri.path("/group/fetchAllGroups/{instance}")
                            .queryParam("getParticipants", "false").build(instanceName))
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();
            if (response == null) return List.of();
            List<Map<String, Object>> out = new java.util.ArrayList<>();
            for (Object o : response) {
                if (o instanceof Map<?, ?> g) {
                    Object jid = g.get("id") != null ? g.get("id") : g.get("jid");
                    if (jid == null || !jid.toString().endsWith("@g.us")) continue;
                    Object subject = g.get("subject");
                    out.add(Map.of(
                            "jid", jid.toString(),
                            "nombre", subject != null ? subject.toString() : jid.toString().split("@")[0]));
                }
            }
            return out;
        } catch (Exception e) {
            log.warn("Evolution listarGruposWA fallo (instance={}): {}", instanceName, e.getMessage());
            return List.of();
        }
    }

    @Override
    public void configurarWebhook(String apiUrl, String apiKey, String instanceName, UUID canalId) {
        String baseUrl = waProperties.webhookBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) return;

        String webhookUrl = baseUrl.replaceAll("/$", "") + "/api/wa/webhook/evolution?canalId=" + canalId;
        WebClient client = buildClient(apiUrl, apiKey);
        try {
            client.post()
                    .uri("/webhook/set/{instance}", instanceName)
                    .bodyValue(Map.of("webhook", Map.of(
                            "enabled", true,
                            "url", webhookUrl,
                            "headers", Map.of(
                                    "x-api-key", waProperties.webhookApiKey(),
                                    "Content-Type", "application/json"),
                            "byEvents", false,
                            "base64", false,
                            "events", List.of("MESSAGES_UPSERT", "MESSAGES_UPDATE", "PRESENCE_UPDATE")
                    )))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (Exception e) {
            // Best-effort: si Evolution no acepta el webhook, el canal sigue
            // funcionando con sync manual; no bloqueamos la conexión por esto.
            log.warn("Evolution configurarWebhook fallo (instance={}): {} — el canal seguira sin push en vivo",
                    instanceName, e.getMessage());
        }
    }

    @Override
    public String fetchFotoPerfil(String apiUrl, String apiKey, String instanceName, String numero) {
        WebClient client = buildClient(apiUrl, apiKey);
        try {
            Map<?, ?> response = client.post()
                    .uri("/chat/fetchProfilePictureUrl/{instance}", instanceName)
                    .bodyValue(Map.of("number", numero))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            if (response == null) return null;
            Object url = response.get("profilePictureUrl");
            return url != null && !url.toString().isBlank() ? url.toString() : null;
        } catch (Exception e) {
            log.warn("Evolution fetchFotoPerfil fallo (instance={}, numero={}): {}", instanceName, numero, e.getMessage());
            return null;
        }
    }

    // findChats/findMessages devuelven payloads grandes (historial completo con
    // metadata de medios); el límite default de WebClient (256KB) los corta con
    // DataBufferLimitException, que quedaba silenciado por el catch generico.
    private static final ExchangeStrategies LARGE_BUFFER_STRATEGY = ExchangeStrategies.builder()
            .codecs(c -> c.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
            .build();

    private WebClient buildClient(String apiUrl, String apiKey) {
        String cleanUrl = apiUrl != null ? apiUrl.trim().replaceAll("/$", "") : "";
        String cleanKey = apiKey != null ? apiKey.trim() : "";
        return webClientBuilder
                .baseUrl(cleanUrl)
                .defaultHeader("apikey", cleanKey)
                .exchangeStrategies(LARGE_BUFFER_STRATEGY)
                .build();
    }

    private String sendText(WebClient client, String instance, String numero, String texto) {
        Map<?, ?> response = client.post()
                .uri("/message/sendText/{instance}", instance)
                .bodyValue(Map.of("number", numero, "text", texto))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return extractMessageId(response);
    }

    private String sendMedia(WebClient client, String instance, String numero, TipoMensaje tipo, String url, String caption) {
        // Evolution v2: endpoint único /message/sendMedia con mediatype en inglés.
        // El "media" debe ser una URL que Evolution pueda alcanzar; convertimos la URL
        // local (/api/media/xxx) a la pública del backend (WA_WEBHOOK_BASE_URL).
        Map<?, ?> response = client.post()
                .uri("/message/sendMedia/{instance}", instance)
                .bodyValue(Map.of(
                        "number", numero,
                        "mediatype", mediatypeDe(tipo),
                        "media", resolverUrlPublica(url),
                        "caption", caption != null ? caption : ""))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return extractMessageId(response);
    }

    private String mediatypeDe(TipoMensaje tipo) {
        return switch (tipo) {
            case IMAGEN, STICKER -> "image";
            case VIDEO -> "video";
            case AUDIO -> "audio";
            default -> "document";
        };
    }

    private String resolverUrlPublica(String url) {
        if (url == null) return null;
        if (url.startsWith("http://") || url.startsWith("https://")) return url;
        String base = waProperties.webhookBaseUrl();
        if (base == null || base.isBlank()) return url; // dev local sin URL pública
        return base.replaceAll("/$", "") + url;
    }

    // wa_message_id es UNIQUE NOT NULL: un literal fijo como "unknown" rompe el
    // insert del segundo mensaje que caiga en este caso (respuesta sin key.id).
    @SuppressWarnings("unchecked")
    private String extractMessageId(Map<?, ?> response) {
        if (response != null) {
            Object key = response.get("key");
            if (key instanceof Map<?, ?> keyMap) {
                Object id = keyMap.get("id");
                if (id != null) return id.toString();
            }
        }
        return "sin-id-" + UUID.randomUUID();
    }
}
