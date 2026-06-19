package com.ar.crm2.adapter.in.rest;

import com.ar.crm2.adapter.in.rest.dto.request.ReceiveWebhookRequest;
import com.ar.crm2.adapter.in.rest.dto.response.MensajeWaResponse;
import com.ar.crm2.whatsapp.application.grupo.service.GrupoService;
import com.ar.crm2.whatsapp.application.mensaje.command.ReceiveMensajeCommand;
import com.ar.crm2.whatsapp.application.mensaje.port.in.ReceiveMensajeUseCase;
import com.ar.crm2.whatsapp.domain.entity.Mensaje;
import com.ar.crm2.whatsapp.domain.enums.TipoMensaje;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/wa/webhook")
@RequiredArgsConstructor
public class WhatsappWebhookController {

    private static final ZoneId MEXICO_ZONE = ZoneId.of("America/Mexico_City");

    private final ReceiveMensajeUseCase receiveMensajeUseCase;
    private final GrupoService grupoService;
    private final com.ar.crm2.whatsapp.application.mensaje.service.ActualizarStatusMensajeService actualizarStatusMensajeService;

    @PostMapping
    public ResponseEntity<?> receive(@Valid @RequestBody ReceiveWebhookRequest request) {
        try {
            ReceiveMensajeCommand command = new ReceiveMensajeCommand(
                    request.canalId(),
                    request.waMessageId(),
                    request.numeroTelefono(),
                    request.nombreContacto(),
                    request.tipo(),
                    request.contenido(),
                    request.mediaUrl(),
                    null,
                    false,
                    LocalDateTime.now(MEXICO_ZONE)
            );
            Mensaje mensaje = receiveMensajeUseCase.receive(command);
            return ResponseEntity.ok(MensajeWaResponse.fromDomain(mensaje));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    /**
     * Endpoint que Evolution API llama directamente (configurado vía
     * EvolutionConectarPort.configurarWebhook). Recibe el payload crudo de
     * Baileys/Evolution para MESSAGES_UPSERT y lo traduce a ReceiveMensajeCommand.
     * Otros eventos (ej. MESSAGES_UPDATE, CONNECTION_UPDATE) se ignoran por ahora.
     */
    @PostMapping("/evolution")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> receiveFromEvolution(
            @RequestParam UUID canalId,
            @RequestBody Map<String, Object> payload
    ) {
        String event = String.valueOf(payload.get("event")).toUpperCase().replace(".", "_");

        Object data = payload.get("data");
        List<Map<String, Object>> items = data instanceof List<?> l
                ? (List<Map<String, Object>>) l
                : data instanceof Map<?, ?> m ? List.of((Map<String, Object>) m) : List.of();

        log.info("Webhook Evolution canal={} event={} items={}", canalId, event, items.size());

        // Acuses de estado (enviado/entregado/leído) de mensajes salientes.
        if (event.contains("MESSAGES_UPDATE")) {
            for (Map<String, Object> it : items) {
                try {
                    procesarActualizacionEstado(it);
                } catch (Exception e) {
                    log.warn("Error procesando MESSAGES_UPDATE: {}", e.getMessage());
                }
            }
            return ResponseEntity.ok().build();
        }

        if (!event.contains("MESSAGES_UPSERT")) {
            return ResponseEntity.ok().build();
        }

        for (Map<String, Object> msg : items) {
            try {
                if (esGrupo(msg)) procesarGrupoEntrante(canalId, msg);
                else procesarMensajeEntrante(canalId, msg);
            } catch (IllegalStateException e) {
                log.debug("Mensaje ya procesado (idempotencia): {}", e.getMessage());
            } catch (Exception e) {
                log.warn("Error procesando mensaje de MESSAGES_UPSERT (canal={}): {}", canalId, e.getMessage(), e);
            }
        }

        return ResponseEntity.ok().build();
    }

    @SuppressWarnings("unchecked")
    private void procesarActualizacionEstado(Map<String, Object> it) {
        Map<String, Object> key = (Map<String, Object>) it.get("key");
        String waMessageId = key != null && key.get("id") != null
                ? key.get("id").toString()
                : (it.get("keyId") != null ? it.get("keyId").toString()
                : (it.get("id") != null ? it.get("id").toString() : null));

        Object status = it.get("status");
        if (status == null && it.get("update") instanceof Map<?, ?> upd) status = upd.get("status");
        if (waMessageId == null || status == null) return;

        actualizarStatusMensajeService.actualizar(waMessageId, status.toString());
    }

    @SuppressWarnings("unchecked")
    private void procesarMensajeEntrante(UUID canalId, Map<String, Object> msg) {
        Map<String, Object> key = (Map<String, Object>) msg.get("key");
        if (key == null) {
            log.debug("Mensaje sin 'key', se descarta: {}", msg);
            return;
        }

        // fromMe = el dueño lo mandó desde su propio celular → se guarda como SALIENTE.
        // (Si lo mandó desde el CRM, la idempotencia por waMessageId evita duplicarlo.)
        boolean esSaliente = Boolean.TRUE.equals(key.get("fromMe"));

        String remoteJid = (String) key.get("remoteJid");
        // Bandeja individual: solo personas (@s.whatsapp.net). Grupos (@g.us) y alias @lid
        // (sin teléfono resoluble) quedan fuera por ahora; los grupos tendrán su bandeja.
        if (remoteJid == null || com.ar.crm2.whatsapp.application.shared.JidUtils.limpiarTelefono(remoteJid) == null) {
            log.info("Mensaje descartado por remoteJid no resoluble: fromMe={} remoteJid={}", esSaliente, remoteJid);
            return;
        }

        String waMessageId = (String) key.get("id");
        if (waMessageId == null || waMessageId.isBlank()) {
            log.debug("Mensaje sin id, se descarta: key={}", key);
            return;
        }
        log.info("Procesando mensaje fromMe={} remoteJid={} waMessageId={}", esSaliente, remoteJid, waMessageId);

        String nombreContacto = (String) msg.get("pushName");
        TipoMensaje tipo = inferirTipo(msg);
        String contenido = extractText(msg);

        ReceiveMensajeCommand command = new ReceiveMensajeCommand(
                canalId, waMessageId, remoteJid, nombreContacto, tipo, contenido, null, msg, esSaliente,
                extraerTimestamp(msg)
        );
        receiveMensajeUseCase.receive(command);
    }

    @SuppressWarnings("unchecked")
    private boolean esGrupo(Map<String, Object> msg) {
        Map<String, Object> key = (Map<String, Object>) msg.get("key");
        if (key == null) return false;
        String jid = (String) key.get("remoteJid");
        return jid != null && jid.endsWith("@g.us");
    }

    @SuppressWarnings("unchecked")
    private void procesarGrupoEntrante(UUID canalId, Map<String, Object> msg) {
        Map<String, Object> key = (Map<String, Object>) msg.get("key");
        if (key == null) return;
        if (Boolean.TRUE.equals(key.get("fromMe"))) return; // mensajes propios al grupo no entran a la bandeja

        String grupoJid = (String) key.get("remoteJid");
        String waMessageId = (String) key.get("id");
        if (grupoJid == null || waMessageId == null || waMessageId.isBlank()) return;

        String remitenteTel = key.get("participant") != null
                ? ((String) key.get("participant")).split("@")[0] : null;
        String remitenteNombre = (String) msg.get("pushName");
        TipoMensaje tipo = inferirTipo(msg);
        String contenido = extractText(msg);

        grupoService.ingestEntrante(canalId, grupoJid, waMessageId, tipo, contenido,
                remitenteNombre, remitenteTel, extraerTimestamp(msg), msg);
    }

    private LocalDateTime extraerTimestamp(Map<String, Object> msg) {
        Object ts = msg.get("messageTimestamp");
        if (ts == null) return LocalDateTime.now(MEXICO_ZONE);
        try {
            return LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(ts.toString())), MEXICO_ZONE);
        } catch (Exception e) {
            return LocalDateTime.now(MEXICO_ZONE);
        }
    }

    @SuppressWarnings("unchecked")
    private TipoMensaje inferirTipo(Map<String, Object> msg) {
        Object message = msg.get("message");
        if (!(message instanceof Map<?, ?> m)) return TipoMensaje.TEXTO;
        if (m.containsKey("imageMessage")) return TipoMensaje.IMAGEN;
        if (m.containsKey("audioMessage")) return TipoMensaje.AUDIO;
        if (m.containsKey("videoMessage")) return TipoMensaje.VIDEO;
        if (m.containsKey("documentMessage")) return TipoMensaje.DOCUMENTO;
        return TipoMensaje.TEXTO;
    }

    @SuppressWarnings("unchecked")
    private String extractText(Map<String, Object> msg) {
        Object message = msg.get("message");
        if (message instanceof Map<?, ?> m) {
            Object conv = m.get("conversation");
            if (conv instanceof String s && !s.isBlank()) return s;
            Object ext = m.get("extendedTextMessage");
            if (ext instanceof Map<?, ?> e) {
                Object text = e.get("text");
                if (text instanceof String s && !s.isBlank()) return s;
            }
            Object img = m.get("imageMessage");
            if (img instanceof Map<?, ?> i) {
                Object c = i.get("caption");
                if (c instanceof String s && !s.isBlank()) return s;
            }
        }
        return "[Media]";
    }
}
