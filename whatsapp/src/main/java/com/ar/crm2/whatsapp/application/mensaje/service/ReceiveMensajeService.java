package com.ar.crm2.whatsapp.application.mensaje.service;

import com.ar.crm2.model.vo.ContactoId;
import com.ar.crm2.whatsapp.application.ajustes.port.out.AjustesWaPort;
import com.ar.crm2.whatsapp.application.canal.port.out.EvolutionConectarPort;
import com.ar.crm2.whatsapp.application.canal.port.out.FindCanalByIdPort;
import com.ar.crm2.whatsapp.application.canal.port.out.MediaDescargada;
import com.ar.crm2.whatsapp.application.canal.port.out.UpsertContactoPort;
import com.ar.crm2.whatsapp.application.conversacion.port.in.GetOrCreateConversacionUseCase;
import com.ar.crm2.whatsapp.application.conversacion.port.out.SaveConversacionPort;
import com.ar.crm2.whatsapp.application.conversacion.port.out.SugerirResponsablePort;
import com.ar.crm2.whatsapp.application.mensaje.command.ReceiveMensajeCommand;
import com.ar.crm2.whatsapp.application.mensaje.port.in.ReceiveMensajeUseCase;
import com.ar.crm2.whatsapp.application.mensaje.port.out.ExistsMensajeByWaMessageIdPort;
import com.ar.crm2.whatsapp.application.mensaje.port.out.FindMensajesByConversacionIdPort;
import com.ar.crm2.whatsapp.application.mensaje.port.out.MediaStoragePort;
import com.ar.crm2.whatsapp.application.mensaje.port.out.NotifyBotPort;
import com.ar.crm2.whatsapp.application.mensaje.port.out.NotifyNewMensajePort;
import com.ar.crm2.whatsapp.application.mensaje.port.out.SaveMensajePort;
import com.ar.crm2.whatsapp.application.mensaje.port.out.SendWhatsappMessagePort;
import com.ar.crm2.whatsapp.application.shared.JidUtils;
import com.ar.crm2.whatsapp.domain.entity.AjustesWa;
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
import java.util.UUID;

@RequiredArgsConstructor
public class ReceiveMensajeService implements ReceiveMensajeUseCase {

    private final ExistsMensajeByWaMessageIdPort existsPort;
    private final GetOrCreateConversacionUseCase getOrCreateConversacion;
    private final SaveMensajePort saveMensajePort;
    private final NotifyNewMensajePort notifyPort;
    private final FindCanalByIdPort findCanalPort;
    private final SaveConversacionPort saveConversacionPort;
    private final UpsertContactoPort upsertContactoPort;
    private final EvolutionConectarPort evolutionPort;
    private final MediaStoragePort mediaStoragePort;
    private final AjustesWaPort ajustesPort;
    private final SendWhatsappMessagePort sendWhatsappPort;
    private final FindMensajesByConversacionIdPort findMensajesPort;
    private final SugerirResponsablePort sugerirResponsablePort;
    private final NotifyBotPort notifyBotPort;

    @Override
    public Mensaje receive(ReceiveMensajeCommand command) {
        // Idempotencia: si ya procesamos este mensaje de Evolution API, lo ignoramos
        if (existsPort.existsByWaMessageId(command.waMessageId())) {
            throw new IllegalStateException("Mensaje ya procesado: " + command.waMessageId());
        }

        CanalWhatsapp canal = findCanalPort.findById(CanalWhatsappId.from(command.canalId())).orElse(null);

        Conversacion conversacion = getOrCreateConversacion.getOrCreate(
                command.canalId(),
                command.numeroTelefono(),
                command.nombreContacto()
        );

        // ¿Primer mensaje de esta conversación? (antes de guardar)
        boolean conversacionNueva = findMensajesPort.findByConversacionId(conversacion.getId()).isEmpty();

        conversacion = vincularContactoSiFalta(conversacion, command, canal);
        // El pushName solo identifica al contacto en mensajes ENTRANTES (en los salientes
        // es el nombre del propio dueño), así que solo renombramos con entrantes.
        if (!command.esSaliente()) conversacion = actualizarNombreSiMejora(conversacion, command);

        String mediaUrl = resolverMedia(command, canal);
        // La columna contenido es VARCHAR(4096); textos extendidos de WhatsApp
        // (citas largas, etc.) pueden excederlo y tumbar el insert.
        String contenido = truncar(command.contenido(), 4000);
        String preview = previewDe(command.tipo(), contenido);

        // Mensajes que el dueño manda desde su propio celular llegan con fromMe=true:
        // se guardan como SALIENTES para que también se vean en el CRM.
        if (command.esSaliente()) {
            LocalDateTime creadoEn = command.timestamp() != null ? command.timestamp() : LocalDateTime.now();
            Mensaje mensaje = Mensaje.reconstitute(
                    com.ar.crm2.whatsapp.domain.vo.MensajeId.create(), conversacion.getId(),
                    command.waMessageId(), command.tipo(), DireccionMensaje.SALIENTE,
                    contenido, mediaUrl, StatusMensaje.ENVIADO, null, creadoEn);
            Mensaje saved = saveMensajePort.save(mensaje);
            notifyPort.notify(saved);
            saveConversacionPort.save(conversacion.registrarMensajeSaliente(preview, saved.getCreadoEn()));
            return saved;
        }

        conversacion = asignarResponsableSiAplica(conversacion, conversacionNueva);

        Mensaje mensaje = Mensaje.createEntrante(
                conversacion.getId(),
                command.waMessageId(),
                command.tipo(),
                contenido,
                mediaUrl,
                command.timestamp()
        );

        Mensaje saved = saveMensajePort.save(mensaje);
        notifyPort.notify(saved);

        // Mueve la conversación al tope de la bandeja y sube el contador de no leídos.
        Conversacion actualizada = conversacion.registrarMensajeEntrante(preview, saved.getCreadoEn());
        saveConversacionPort.save(actualizada);

        enviarBienvenidaSiAplica(canal, conversacion, conversacionNueva);
        if (actualizada.isBotActivo()) notifyBotPort.notificarMensajeEntrante(actualizada, saved, canal);
        return saved;
    }

    // Solo COMPLETA el nombre cuando la conversación aún tiene el número (o nada) como nombre.
    // No pisa un nombre ya "real" — así un rename manual no se revierte con el pushName entrante.
    private Conversacion actualizarNombreSiMejora(Conversacion conversacion, ReceiveMensajeCommand command) {
        String nombre = command.nombreContacto();
        if (nombre == null || nombre.isBlank() || nombre.matches("\\d+")) return conversacion;
        String actual = conversacion.getNombreContacto();
        boolean actualEsReal = actual != null && !actual.isBlank()
                && !actual.matches("\\d+") && !actual.equals(command.numeroTelefono());
        if (actualEsReal) return conversacion;
        return saveConversacionPort.save(conversacion.conNombre(nombre));
    }

    private String truncar(String texto, int max) {
        return texto != null && texto.length() > max ? texto.substring(0, max) : texto;
    }

    private String previewDe(TipoMensaje tipo, String contenido) {
        if (contenido != null && !contenido.isBlank()) return contenido;
        return switch (tipo) {
            case IMAGEN -> "📷 Imagen";
            case AUDIO -> "🎵 Audio";
            case VIDEO -> "🎬 Video";
            case DOCUMENTO -> "📄 Documento";
            case STICKER -> "Sticker";
            case UBICACION -> "📍 Ubicación";
            default -> "";
        };
    }

    // Auto-asignación round-robin del agente con menos carga (si está activada).
    private Conversacion asignarResponsableSiAplica(Conversacion conversacion, boolean conversacionNueva) {
        if (!conversacionNueva || conversacion.getAsignadoA() != null) return conversacion;
        if (!ajustesPort.get().autoAsignar()) return conversacion;
        return sugerirResponsablePort.sugerirMenosCargado()
                .map(uid -> saveConversacionPort.save(conversacion.asignarAgente(uid)))
                .orElse(conversacion);
    }

    // Bienvenida automática en el primer mensaje de un contacto (si está activada).
    private void enviarBienvenidaSiAplica(CanalWhatsapp canal, Conversacion conversacion, boolean conversacionNueva) {
        if (!conversacionNueva || canal == null) return;
        AjustesWa aj = ajustesPort.get();
        if (!aj.bienvenidaActiva() || aj.bienvenidaTexto() == null || aj.bienvenidaTexto().isBlank()) return;

        String texto = aplicarVariables(aj.bienvenidaTexto(), conversacion.getNombreContacto());
        String waId = sendWhatsappPort.send(canal, conversacion.getNumeroTelefono(), TipoMensaje.TEXTO, texto, null);

        Mensaje bienvenida = Mensaje.reconstitute(
                MensajeId.create(), conversacion.getId(),
                waId != null && !waId.isBlank() ? waId : "auto-" + UUID.randomUUID(),
                TipoMensaje.TEXTO, DireccionMensaje.SALIENTE, texto, null,
                StatusMensaje.ENVIADO, null, LocalDateTime.now());
        Mensaje savedB = saveMensajePort.save(bienvenida);
        notifyPort.notify(savedB);
    }

    private String aplicarVariables(String plantilla, String nombre) {
        return plantilla.replace("{{nombre}}", nombre != null ? nombre : "");
    }

    // El media de WhatsApp viene cifrado: lo descargamos de Evolution y lo guardamos local
    // ANTES de guardar el mensaje, para que el SSE ya emita la URL servible.
    private String resolverMedia(ReceiveMensajeCommand command, CanalWhatsapp canal) {
        if (command.mediaUrl() != null) return command.mediaUrl();
        if (canal == null || command.tipo() == TipoMensaje.TEXTO || command.rawMensaje() == null) return null;

        MediaDescargada media = evolutionPort.descargarMedia(
                canal.getApiUrl(), canal.getApiKey(), canal.getInstanceName(), command.rawMensaje());
        if (media == null) return null;
        return mediaStoragePort.guardarBase64(media.base64(), media.mime());
    }

    // Igual que en SyncChatsService: cada conversación queda vinculada a un
    // Contacto del CRM (de la misma empresa del canal), también en tiempo real.
    private Conversacion vincularContactoSiFalta(Conversacion conversacion, ReceiveMensajeCommand command, CanalWhatsapp canal) {
        if (conversacion.getContactoId() != null) return conversacion;

        String telefono = JidUtils.limpiarTelefono(command.numeroTelefono());
        if (telefono == null || canal == null) return conversacion;

        // En salientes el pushName es del dueño, no del contacto: no lo usamos como nombre.
        String nombre = command.esSaliente() ? null : command.nombreContacto();
        ContactoId contactoId = upsertContactoPort.upsertPorTelefono(
                canal.getEmpresaId(), telefono, nombre);
        if (contactoId == null) return conversacion;

        return saveConversacionPort.save(conversacion.vincularContacto(contactoId));
    }
}
