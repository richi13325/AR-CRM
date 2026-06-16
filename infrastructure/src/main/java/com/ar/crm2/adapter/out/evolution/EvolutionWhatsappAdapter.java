package com.ar.crm2.adapter.out.evolution;

import com.ar.crm2.whatsapp.application.mensaje.port.out.SendWhatsappMessagePort;
import com.ar.crm2.whatsapp.domain.entity.CanalWhatsapp;
import com.ar.crm2.whatsapp.domain.enums.TipoMensaje;
import lombok.RequiredArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@RequiredArgsConstructor
public class EvolutionWhatsappAdapter implements SendWhatsappMessagePort {

    private final WebClient.Builder webClientBuilder;

    @Override
    public String send(CanalWhatsapp canal, String numeroDestino, TipoMensaje tipo, String contenido, String mediaUrl) {
        WebClient client = webClientBuilder
                .baseUrl(canal.getApiUrl())
                .defaultHeader("apikey", canal.getApiKey())
                .build();

        if (tipo == TipoMensaje.TEXTO) {
            return sendText(client, canal.getInstanceName(), numeroDestino, contenido);
        }
        if (tipo == TipoMensaje.IMAGEN || tipo == TipoMensaje.AUDIO || tipo == TipoMensaje.VIDEO || tipo == TipoMensaje.DOCUMENTO) {
            return sendMedia(client, canal.getInstanceName(), numeroDestino, tipo, mediaUrl, contenido);
        }
        throw new UnsupportedOperationException("Tipo de mensaje no soportado: " + tipo);
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
        String mediaType = tipo.name().toLowerCase();
        Map<?, ?> response = client.post()
                .uri("/message/send{type}/{instance}", capitalize(mediaType), instance)
                .bodyValue(Map.of("number", numero, "mediatype", mediaType, "media", url, "caption", caption != null ? caption : ""))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return extractMessageId(response);
    }

    @SuppressWarnings("unchecked")
    private String extractMessageId(Map<?, ?> response) {
        if (response == null) return "unknown";
        Object key = response.get("key");
        if (key instanceof Map<?, ?> keyMap) {
            Object id = keyMap.get("id");
            if (id != null) return id.toString();
        }
        return "unknown";
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
