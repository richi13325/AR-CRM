package com.ar.crm2.whatsapp.application.grupo.service;

import com.ar.crm2.whatsapp.application.canal.port.out.EvolutionConectarPort;
import com.ar.crm2.whatsapp.application.canal.port.out.FindCanalByIdPort;
import com.ar.crm2.whatsapp.application.canal.port.out.MediaDescargada;
import com.ar.crm2.whatsapp.application.grupo.port.out.GrupoRepositoryPort;
import com.ar.crm2.whatsapp.application.grupo.port.out.MensajeGrupoRepositoryPort;
import com.ar.crm2.whatsapp.application.grupo.port.out.NotifyMensajeGrupoPort;
import com.ar.crm2.whatsapp.application.mensaje.port.out.MediaStoragePort;
import com.ar.crm2.whatsapp.domain.entity.CanalWhatsapp;
import com.ar.crm2.whatsapp.domain.entity.Grupo;
import com.ar.crm2.whatsapp.domain.entity.MensajeGrupo;
import com.ar.crm2.whatsapp.domain.enums.DireccionMensaje;
import com.ar.crm2.whatsapp.domain.enums.TipoMensaje;
import com.ar.crm2.whatsapp.domain.vo.CanalWhatsappId;
import com.ar.crm2.whatsapp.domain.vo.GrupoId;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Bandeja de grupos de WhatsApp (separada de los chats individuales). Espejo de
 * AmbarCRM/.../lib/services/grupos.ts.
 */
@RequiredArgsConstructor
public class GrupoService {

    private final GrupoRepositoryPort grupoPort;
    private final MensajeGrupoRepositoryPort mensajePort;
    private final NotifyMensajeGrupoPort notifyPort;
    private final EvolutionConectarPort evolutionPort;
    private final MediaStoragePort mediaStoragePort;
    private final FindCanalByIdPort findCanalPort;

    private static final int MESSAGES_PER_GRUPO = 10;

    // ── Ingesta en vivo (desde el webhook) ──────────────────────────────────
    public void ingestEntrante(UUID canalId, String grupoJid, String waMessageId,
                               TipoMensaje tipo, String contenido, String remitenteNombre,
                               String remitenteTel, LocalDateTime timestamp, Object rawMensaje) {
        if (waMessageId == null || waMessageId.isBlank()) return;
        if (mensajePort.existsByWaMessageId(waMessageId)) return;

        CanalWhatsapp canal = canalId != null
                ? findCanalPort.findById(CanalWhatsappId.from(canalId)).orElse(null) : null;

        Grupo grupo = getOrCreateGrupo(canal, canalId, grupoJid);

        String mediaUrl = descargarMediaSiAplica(canal, tipo, rawMensaje);

        MensajeGrupo mensaje = MensajeGrupo.createEntrante(
                grupo.getId(), waMessageId, tipo, contenido, mediaUrl,
                remitenteNombre, remitenteTel, timestamp);
        MensajeGrupo saved = mensajePort.save(mensaje);

        grupoPort.save(grupo.conMensajeEntrante(saved.getTimestamp()));
        notifyPort.notifyGrupo(saved);
    }

    // ── Lecturas ────────────────────────────────────────────────────────────
    public List<Grupo> getAll() {
        return grupoPort.findAll();
    }

    public List<MensajeGrupo> getMensajes(UUID grupoId) {
        return mensajePort.findByGrupoOrdenado(GrupoId.from(grupoId));
    }

    public Grupo marcarLeido(UUID grupoId) {
        Grupo grupo = grupoPort.findById(GrupoId.from(grupoId))
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado: " + grupoId));
        return grupoPort.save(grupo.marcarLeido());
    }

    // ── Importación del directorio de grupos del número ─────────────────────
    public int importar(UUID canalId) {
        CanalWhatsapp canal = findCanalPort.findById(CanalWhatsappId.from(canalId))
                .orElseThrow(() -> new IllegalArgumentException("Canal no encontrado: " + canalId));

        List<Map<String, Object>> grupos = evolutionPort.listarGruposWA(
                canal.getApiUrl(), canal.getApiKey(), canal.getInstanceName());

        int imported = 0;
        for (Map<String, Object> g : grupos) {
            try {
                String jid = (String) g.get("jid");
                if (jid == null || !jid.endsWith("@g.us")) continue;
                String nombre = (String) g.get("nombre");
                Grupo grupo = getOrCreateGrupoConNombre(canal, canalId, jid, nombre);

                List<Map<String, Object>> mensajes = evolutionPort.fetchMessages(
                        canal.getApiUrl(), canal.getApiKey(), canal.getInstanceName(), jid, MESSAGES_PER_GRUPO);
                for (Map<String, Object> msg : mensajes) {
                    try {
                        imported += importarMensajeHistorico(canal, grupo, msg);
                    } catch (Exception ignored) {}
                }
            } catch (Exception ignored) {}
        }
        return imported;
    }

    // ── Helpers ─────────────────────────────────────────────────────────────
    private Grupo getOrCreateGrupo(CanalWhatsapp canal, UUID canalId, String jid) {
        return grupoPort.findByJid(jid).orElseGet(() -> {
            String nombre = jid;
            if (canal != null) {
                String info = evolutionPort.infoGrupo(canal.getApiUrl(), canal.getApiKey(), canal.getInstanceName(), jid);
                if (info != null && !info.isBlank()) nombre = info;
            }
            return grupoPort.save(Grupo.create(canalId != null ? CanalWhatsappId.from(canalId) : null, jid, nombre));
        });
    }

    private Grupo getOrCreateGrupoConNombre(CanalWhatsapp canal, UUID canalId, String jid, String nombre) {
        return grupoPort.findByJid(jid).orElseGet(() ->
                grupoPort.save(Grupo.create(canalId != null ? CanalWhatsappId.from(canalId) : null, jid, nombre)));
    }

    private String descargarMediaSiAplica(CanalWhatsapp canal, TipoMensaje tipo, Object rawMensaje) {
        if (canal == null || tipo == TipoMensaje.TEXTO || rawMensaje == null) return null;
        MediaDescargada media = evolutionPort.descargarMedia(
                canal.getApiUrl(), canal.getApiKey(), canal.getInstanceName(), rawMensaje);
        return media != null ? mediaStoragePort.guardarBase64(media.base64(), media.mime()) : null;
    }

    @SuppressWarnings("unchecked")
    private int importarMensajeHistorico(CanalWhatsapp canal, Grupo grupo, Map<String, Object> msg) {
        Map<String, Object> key = (Map<String, Object>) msg.get("key");
        if (key == null) return 0;
        String waMessageId = (String) key.get("id");
        if (waMessageId == null || waMessageId.isBlank()) return 0;
        if (mensajePort.existsByWaMessageId(waMessageId)) return 0;

        boolean fromMe = Boolean.TRUE.equals(key.get("fromMe"));
        TipoMensaje tipo = inferirTipo(msg);
        String contenido = truncar(extractText(msg), 4000);
        String mediaUrl = descargarMediaSiAplica(canal, tipo, msg);
        String remitenteTel = key.get("participant") != null
                ? ((String) key.get("participant")).split("@")[0] : null;

        MensajeGrupo mensaje = MensajeGrupo.reconstitute(
                com.ar.crm2.whatsapp.domain.vo.MensajeGrupoId.create(),
                grupo.getId(),
                fromMe ? DireccionMensaje.SALIENTE : DireccionMensaje.ENTRANTE,
                tipo, contenido, mediaUrl,
                (String) msg.get("pushName"), remitenteTel,
                fromMe ? com.ar.crm2.whatsapp.domain.enums.StatusMensaje.ENVIADO
                       : com.ar.crm2.whatsapp.domain.enums.StatusMensaje.ENTREGADO,
                waMessageId, extraerTimestamp(msg), LocalDateTime.now());
        mensajePort.save(mensaje);
        return 1;
    }

    @SuppressWarnings("unchecked")
    private TipoMensaje inferirTipo(Map<String, Object> msg) {
        Object message = msg.get("message");
        if (!(message instanceof Map<?, ?> m)) return TipoMensaje.TEXTO;
        if (m.containsKey("imageMessage")) return TipoMensaje.IMAGEN;
        if (m.containsKey("audioMessage")) return TipoMensaje.AUDIO;
        if (m.containsKey("videoMessage")) return TipoMensaje.VIDEO;
        if (m.containsKey("documentMessage")) return TipoMensaje.DOCUMENTO;
        if (m.containsKey("stickerMessage")) return TipoMensaje.STICKER;
        return TipoMensaje.TEXTO;
    }

    @SuppressWarnings("unchecked")
    private String extractText(Map<String, Object> msg) {
        Object message = msg.get("message");
        if (message instanceof Map<?, ?> m) {
            Object c = m.get("conversation");
            if (c instanceof String s && !s.isBlank()) return s;
            Object ext = m.get("extendedTextMessage");
            if (ext instanceof Map<?, ?> e && e.get("text") instanceof String s && !s.isBlank()) return s;
            Object img = m.get("imageMessage");
            if (img instanceof Map<?, ?> i && i.get("caption") instanceof String s && !s.isBlank()) return s;
        }
        return null;
    }

    private LocalDateTime extraerTimestamp(Map<String, Object> msg) {
        Object ts = msg.get("messageTimestamp");
        if (ts == null) return LocalDateTime.now();
        try {
            long secs = Long.parseLong(ts.toString());
            return LocalDateTime.ofInstant(java.time.Instant.ofEpochSecond(secs), ZoneId.systemDefault());
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    private String truncar(String texto, int max) {
        return texto != null && texto.length() > max ? texto.substring(0, max) : texto;
    }
}
