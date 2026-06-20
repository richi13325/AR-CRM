package com.ar.crm2.adapter.out.ia;

import com.ar.crm2.whatsapp.application.ia.port.out.GenerarSugerenciaPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

/**
 * Genera sugerencias de respuesta con la API de Anthropic (Claude).
 * Requiere ANTHROPIC_API_KEY. El modelo es configurable (anthropic.model).
 */
@Slf4j
@RequiredArgsConstructor
public class AnthropicSugerenciaAdapter implements GenerarSugerenciaPort {

    private static final String SYSTEM_PROMPT =
            "Eres un asistente de atención al cliente por WhatsApp. A partir del historial " +
            "de la conversación, redacta UNA respuesta breve, cordial y profesional en español " +
            "que el agente pueda enviar al cliente. Devuelve solo el texto del mensaje, sin comillas " +
            "ni explicaciones.";

    private final WebClient.Builder webClientBuilder;
    private final String apiKey;
    private final String model;

    @Override
    @SuppressWarnings("unchecked")
    public String generar(String historial) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Falta ANTHROPIC_API_KEY para sugerir respuestas con IA");
        }
        WebClient client = webClientBuilder
                .baseUrl("https://api.anthropic.com")
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", "2023-06-01")
                .defaultHeader("content-type", "application/json")
                .build();

        Map<?, ?> resp = client.post()
                .uri("/v1/messages")
                .bodyValue(Map.of(
                        "model", model,
                        "max_tokens", 400,
                        "system", SYSTEM_PROMPT,
                        "messages", List.of(Map.of(
                                "role", "user",
                                "content", "Historial de la conversación:\n\n" + historial
                                        + "\n\nRedacta la próxima respuesta del agente."))))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (resp == null) throw new IllegalStateException("Respuesta vacía de Anthropic");
        Object content = resp.get("content");
        if (content instanceof List<?> bloques && !bloques.isEmpty()
                && bloques.get(0) instanceof Map<?, ?> primero) {
            Object text = primero.get("text");
            if (text != null) return text.toString().trim();
        }
        throw new IllegalStateException("No se pudo extraer la sugerencia de la respuesta de Anthropic");
    }
}
