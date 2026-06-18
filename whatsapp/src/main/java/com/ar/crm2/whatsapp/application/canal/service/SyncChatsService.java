package com.ar.crm2.whatsapp.application.canal.service;

import com.ar.crm2.model.vo.ContactoId;
import com.ar.crm2.whatsapp.application.shared.JidUtils;
import com.ar.crm2.whatsapp.application.canal.port.in.SyncChatsUseCase;
import com.ar.crm2.whatsapp.application.canal.port.out.EvolutionConectarPort;
import com.ar.crm2.whatsapp.application.canal.port.out.FindCanalByIdPort;
import com.ar.crm2.whatsapp.application.canal.port.out.MediaDescargada;
import com.ar.crm2.whatsapp.application.canal.port.out.UpsertContactoPort;
import com.ar.crm2.whatsapp.application.conversacion.port.in.GetOrCreateConversacionUseCase;
import com.ar.crm2.whatsapp.application.conversacion.port.out.SaveConversacionPort;
import com.ar.crm2.whatsapp.application.mensaje.port.out.ExistsMensajeByWaMessageIdPort;
import com.ar.crm2.whatsapp.application.mensaje.port.out.MediaStoragePort;
import com.ar.crm2.whatsapp.application.mensaje.port.out.SaveMensajePort;
import com.ar.crm2.whatsapp.domain.entity.CanalWhatsapp;
import com.ar.crm2.whatsapp.domain.entity.Conversacion;
import com.ar.crm2.whatsapp.domain.entity.Mensaje;
import com.ar.crm2.whatsapp.domain.enums.DireccionMensaje;
import com.ar.crm2.whatsapp.domain.enums.StatusMensaje;
import com.ar.crm2.whatsapp.domain.enums.TipoMensaje;
import com.ar.crm2.whatsapp.domain.vo.CanalWhatsappId;
import com.ar.crm2.whatsapp.domain.vo.MensajeId;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class SyncChatsService implements SyncChatsUseCase {

    private final FindCanalByIdPort findCanalPort;
    private final EvolutionConectarPort evolutionPort;
    private final GetOrCreateConversacionUseCase getOrCreateConversacion;
    private final SaveConversacionPort saveConversacionPort;
    private final UpsertContactoPort upsertContactoPort;
    private final ExistsMensajeByWaMessageIdPort existsMensajePort;
    private final SaveMensajePort saveMensajePort;
    private final MediaStoragePort mediaStoragePort;

    private static final int MESSAGES_PER_CHAT = 5;

    @Override
    public int syncChats(UUID canalId) {
        CanalWhatsapp canal = findCanalPort.findById(CanalWhatsappId.from(canalId))
                .orElseThrow(() -> new IllegalArgumentException("Canal no encontrado: " + canalId));

        // Orden de sincronización (igual que el CRM anterior): contactos primero,
        // luego chats (vinculados al contacto ya creado), luego mensajes por chat.
        List<Map<String, Object>> contactos = evolutionPort.fetchContacts(
                canal.getApiUrl(), canal.getApiKey(), canal.getInstanceName());

        // El nombre real de WhatsApp (pushName) viene en findContacts, no en findChats.
        // Lo guardamos por teléfono para nombrar bien también las conversaciones.
        Map<String, String> nombresPorTelefono = new java.util.HashMap<>();
        for (Map<String, Object> contacto : contactos) {
            try {
                String remoteJid = (String) contacto.get("remoteJid");
                if (remoteJid == null) continue;
                String telefono = JidUtils.limpiarTelefono(remoteJid);
                if (telefono == null) continue; // grupos/broadcast/lid: no son un contacto individual
                String nombre = resolveNombre(contacto, remoteJid);
                if (esNombreReal(nombre, telefono)) nombresPorTelefono.put(telefono, nombre);
                upsertContactoPort.upsertPorTelefono(canal.getEmpresaId(), telefono, nombre);
            } catch (Exception ignored) {}
        }

        List<Map<String, Object>> chats = evolutionPort.fetchChats(
                canal.getApiUrl(), canal.getApiKey(), canal.getInstanceName());

        int imported = 0;

        for (Map<String, Object> chat : chats) {
            try {
                String remoteJid = (String) chat.get("remoteJid");
                if (remoteJid == null) continue;

                // Bandeja individual: solo chats de personas (@s.whatsapp.net). Los grupos
                // (@g.us) van a su propia bandeja y los @lid (alias sin teléfono resoluble)
                // se omiten — así no se ensucia la lista con conversaciones sin contacto.
                String telefono = JidUtils.limpiarTelefono(remoteJid);
                if (telefono == null) continue;

                // Preferimos el nombre real (pushName de findContacts) sobre el del chat.
                String nombre = nombresPorTelefono.getOrDefault(telefono, resolveNombre(chat, remoteJid));
                Conversacion conv = getOrCreateConversacion.getOrCreate(canalId, remoteJid, nombre);

                ContactoId contactoId = upsertContactoPort.upsertPorTelefono(canal.getEmpresaId(), telefono, nombre);
                if (contactoId != null && conv.getContactoId() == null) {
                    conv = saveConversacionPort.save(conv.vincularContacto(contactoId));
                }
                // Si la conversación tenía el número como nombre, la renombramos al nombre real.
                if (esNombreReal(nombre, telefono) && !nombre.equals(conv.getNombreContacto())) {
                    conv = saveConversacionPort.save(conv.conNombre(nombre));
                }

                List<Map<String, Object>> messages = evolutionPort.fetchMessages(
                        canal.getApiUrl(), canal.getApiKey(), canal.getInstanceName(),
                        remoteJid, MESSAGES_PER_CHAT);

                Map<String, Object> ultimo = null;
                long ultimoTs = -1;
                for (Map<String, Object> msg : messages) {
                    try {
                        imported += importMensaje(msg, conv, canal);
                        long ts = tsDe(msg);
                        if (ts >= ultimoTs) { ultimoTs = ts; ultimo = msg; }
                    } catch (Exception ignored) {}
                }

                // Ordena la bandeja por la actividad real del chat (último mensaje).
                if (ultimo != null) {
                    String preview = truncar(extractText(ultimo), 120);
                    conv = saveConversacionPort.save(conv.registrarMensajeSaliente(
                            preview, extraerTimestamp(ultimo)));
                }
            } catch (Exception ignored) {}
        }

        return imported;
    }

    private long tsDe(Map<String, Object> msg) {
        Object ts = msg.get("messageTimestamp");
        try { return ts != null ? Long.parseLong(ts.toString()) : 0; } catch (Exception e) { return 0; }
    }

    private java.time.LocalDateTime extraerTimestamp(Map<String, Object> msg) {
        long secs = tsDe(msg);
        return secs > 0
                ? java.time.LocalDateTime.ofInstant(java.time.Instant.ofEpochSecond(secs), java.time.ZoneId.systemDefault())
                : java.time.LocalDateTime.now();
    }

    @SuppressWarnings("unchecked")
    private int importMensaje(Map<String, Object> msg, Conversacion conv, CanalWhatsapp canal) {
        Map<String, Object> key = (Map<String, Object>) msg.get("key");
        if (key == null) return 0;

        String waMessageId = (String) key.get("id");
        if (waMessageId == null || waMessageId.isBlank()) return 0;
        if (existsMensajePort.existsByWaMessageId(waMessageId)) return 0;

        boolean fromMe = Boolean.TRUE.equals(key.get("fromMe"));
        TipoMensaje tipo = inferirTipo(msg);
        // La columna contenido es VARCHAR(4096); textos extendidos de WhatsApp
        // (citas largas, etc.) pueden excederlo y tumbar el insert silenciosamente.
        String contenido = truncar(extractText(msg), 4000);

        // Media cifrada: descargar de Evolution y guardar local para poder mostrarla.
        String mediaUrl = null;
        if (tipo != TipoMensaje.TEXTO) {
            MediaDescargada media = evolutionPort.descargarMedia(
                    canal.getApiUrl(), canal.getApiKey(), canal.getInstanceName(), msg);
            if (media != null) mediaUrl = mediaStoragePort.guardarBase64(media.base64(), media.mime());
        }

        Mensaje mensaje = Mensaje.reconstitute(
                MensajeId.create(),
                conv.getId(),
                waMessageId,
                tipo,
                fromMe ? DireccionMensaje.SALIENTE : DireccionMensaje.ENTRANTE,
                contenido,
                mediaUrl,
                fromMe ? StatusMensaje.ENVIADO : StatusMensaje.ENTREGADO,
                null,
                // Hora REAL del mensaje en WhatsApp (no la del sync), para que el
                // historial del chat quede en orden cronológico correcto.
                extraerTimestamp(msg)
        );

        saveMensajePort.save(mensaje);
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
            Object conv = m.get("conversation");
            if (conv instanceof String s && !s.isBlank()) return s;
            Object ext = m.get("extendedTextMessage");
            if (ext instanceof Map<?, ?> e) {
                Object text = e.get("text");
                if (text instanceof String s && !s.isBlank()) return s;
            }
            Object caption = m.get("imageMessage");
            if (caption instanceof Map<?, ?> img) {
                Object c = img.get("caption");
                if (c instanceof String s && !s.isBlank()) return s;
            }
        }
        return "[Media]";
    }

    private String truncar(String texto, int max) {
        return texto != null && texto.length() > max ? texto.substring(0, max) : texto;
    }

    /** Un nombre es "real" si no es vacío ni el propio número. */
    private boolean esNombreReal(String nombre, String telefono) {
        return nombre != null && !nombre.isBlank()
                && !nombre.equals(telefono) && !nombre.matches("\\d+");
    }

    private String resolveNombre(Map<String, Object> chat, String remoteJid) {
        Object name = chat.get("name");
        if (name instanceof String s && !s.isBlank()) return s;
        Object push = chat.get("pushName");
        if (push instanceof String s && !s.isBlank()) return s;
        return remoteJid.replace("@s.whatsapp.net", "").replace("@g.us", "");
    }
}
