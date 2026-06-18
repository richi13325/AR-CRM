package com.ar.crm2.adapter.out.bot;

import com.ar.crm2.security.WaProperties;
import com.ar.crm2.whatsapp.application.bot.port.out.FindBotActivoParaCanalPort;
import com.ar.crm2.whatsapp.application.mensaje.port.out.NotifyBotPort;
import com.ar.crm2.whatsapp.domain.entity.Bot;
import com.ar.crm2.whatsapp.domain.entity.CanalWhatsapp;
import com.ar.crm2.whatsapp.domain.entity.Conversacion;
import com.ar.crm2.whatsapp.domain.entity.Mensaje;
import com.ar.crm2.whatsapp.domain.enums.EstadoConversacion;
import lombok.RequiredArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

/**
 * Notifica al webhook saliente del bot de n8n cuando llega un mensaje entrante.
 * Payload estilo Chatwoot {@code message_created}, igual al documentado para n8n/Evolution.
 * Best-effort: si el bot no responde o no hay bot configurado, no rompe el flujo de mensajería.
 */
@RequiredArgsConstructor
public class BotWebhookNotifier implements NotifyBotPort {

    private static final String ACCOUNT_ID = "1";

    private final WebClient.Builder webClientBuilder;
    private final FindBotActivoParaCanalPort findBotPort;
    private final WaProperties waProperties;

    @Override
    public void notificarMensajeEntrante(Conversacion conversacion, Mensaje mensaje, CanalWhatsapp canal) {
        if (canal == null) return;
        Bot bot = findBotPort.findActivoParaCanal(canal.getId()).orElse(null);
        if (bot == null) return;

        String conversacionId = conversacion.getId().value().toString();
        String baseUrl = waProperties.webhookBaseUrl() != null ? waProperties.webhookBaseUrl().replaceAll("/$", "") : "";

        Map<String, Object> sender = Map.of(
                "identifier", conversacion.getNumeroTelefono(),
                "name", conversacion.getNombreContacto() != null ? conversacion.getNombreContacto() : conversacion.getNumeroTelefono(),
                "phone_number", "+" + conversacion.getNumeroTelefono()
        );

        Map<String, Object> conversationPayload = Map.of(
                "id", conversacionId,
                "status", estadoChatwoot(conversacion.getEstado()),
                "labels", List.copyOf(conversacion.getLabels()),
                "meta", Map.of("sender", sender)
        );

        List<Map<String, Object>> attachments = mensaje.getMediaUrl() != null
                ? List.of(Map.of("file_type", "media", "data_url", mensaje.getMediaUrl()))
                : List.of();

        Map<String, Object> payload = Map.of(
                "event", "message_created",
                "message_type", "incoming",
                "content", mensaje.getContenido() != null ? mensaje.getContenido() : "",
                "conversation", conversationPayload,
                "sender", sender,
                "attachments", attachments,
                "account", Map.of("id", ACCOUNT_ID),
                "ambarcrm", Map.of(
                        "conversacionId", conversacionId,
                        "telefono", conversacion.getNumeroTelefono(),
                        "responder_url", baseUrl + "/api/v1/accounts/" + ACCOUNT_ID + "/conversations/" + conversacionId + "/messages",
                        "handoff_url", baseUrl + "/api/v1/accounts/" + ACCOUNT_ID + "/conversations/" + conversacionId + "/labels"
                )
        );

        try {
            webClientBuilder.build()
                    .post()
                    .uri(bot.getWebhookUrl())
                    .bodyValue(payload)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception ignored) {
            // Best-effort: si n8n no responde, el mensaje ya quedó guardado en el CRM.
        }
    }

    private String estadoChatwoot(EstadoConversacion estado) {
        return switch (estado) {
            case ABIERTA -> "open";
            case EN_ESPERA -> "pending";
            case CERRADA -> "resolved";
        };
    }
}
