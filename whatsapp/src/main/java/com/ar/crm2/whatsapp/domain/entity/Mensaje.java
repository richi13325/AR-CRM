package com.ar.crm2.whatsapp.domain.entity;

import com.ar.crm2.model.vo.UsuarioId;
import com.ar.crm2.shared.DomainAssert;
import com.ar.crm2.whatsapp.domain.enums.DireccionMensaje;
import com.ar.crm2.whatsapp.domain.enums.StatusMensaje;
import com.ar.crm2.whatsapp.domain.enums.TipoMensaje;
import com.ar.crm2.whatsapp.domain.vo.ConversacionId;
import com.ar.crm2.whatsapp.domain.vo.MensajeId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class Mensaje {

    @EqualsAndHashCode.Include
    private final MensajeId id;

    private final ConversacionId conversacionId;
    private final String waMessageId;       // ID de Evolution API — UNIQUE para idempotencia
    private final TipoMensaje tipo;
    private final DireccionMensaje direccion;
    private final String contenido;         // texto del mensaje
    private final String mediaUrl;          // nullable — solo para mensajes de media
    private final StatusMensaje status;
    private final UsuarioId enviadoPor;     // nullable — null en mensajes ENTRANTES
    private final boolean interna;          // true = nota interna (no se envía a WhatsApp)
    private final LocalDateTime creadoEn;

    public static Mensaje createEntrante(
            ConversacionId conversacionId,
            String waMessageId,
            TipoMensaje tipo,
            String contenido,
            String mediaUrl,
            LocalDateTime creadoEn
    ) {
        DomainAssert.notNull(conversacionId, "conversacionId");
        DomainAssert.notBlank(waMessageId, "waMessageId");
        DomainAssert.notNull(tipo, "tipo");

        return Mensaje.builder()
                .id(MensajeId.create())
                .conversacionId(conversacionId)
                .waMessageId(waMessageId)
                .tipo(tipo)
                .direccion(DireccionMensaje.ENTRANTE)
                .contenido(contenido)
                .mediaUrl(mediaUrl)
                .status(StatusMensaje.ENTREGADO)
                .enviadoPor(null)
                .interna(false)
                .creadoEn(creadoEn != null ? creadoEn : LocalDateTime.now())
                .build();
    }

    public static Mensaje createSaliente(
            ConversacionId conversacionId,
            String waMessageId,
            TipoMensaje tipo,
            String contenido,
            String mediaUrl,
            UsuarioId enviadoPor
    ) {
        return createSaliente(conversacionId, waMessageId, tipo, contenido, mediaUrl, enviadoPor, false);
    }

    public static Mensaje createSaliente(
            ConversacionId conversacionId,
            String waMessageId,
            TipoMensaje tipo,
            String contenido,
            String mediaUrl,
            UsuarioId enviadoPor,
            boolean interna
    ) {
        DomainAssert.notNull(conversacionId, "conversacionId");
        DomainAssert.notBlank(waMessageId, "waMessageId");
        DomainAssert.notNull(tipo, "tipo");
        DomainAssert.notNull(enviadoPor, "enviadoPor");

        return Mensaje.builder()
                .id(MensajeId.create())
                .conversacionId(conversacionId)
                .waMessageId(waMessageId)
                .tipo(tipo)
                .direccion(DireccionMensaje.SALIENTE)
                .contenido(contenido)
                .mediaUrl(mediaUrl)
                .status(StatusMensaje.ENVIADO)
                .enviadoPor(enviadoPor)
                .interna(interna)
                .creadoEn(LocalDateTime.now())
                .build();
    }

    public static Mensaje reconstitute(
            MensajeId id,
            ConversacionId conversacionId,
            String waMessageId,
            TipoMensaje tipo,
            DireccionMensaje direccion,
            String contenido,
            String mediaUrl,
            StatusMensaje status,
            UsuarioId enviadoPor,
            LocalDateTime creadoEn
    ) {
        return reconstitute(id, conversacionId, waMessageId, tipo, direccion, contenido,
                mediaUrl, status, enviadoPor, false, creadoEn);
    }

    public static Mensaje reconstitute(
            MensajeId id,
            ConversacionId conversacionId,
            String waMessageId,
            TipoMensaje tipo,
            DireccionMensaje direccion,
            String contenido,
            String mediaUrl,
            StatusMensaje status,
            UsuarioId enviadoPor,
            boolean interna,
            LocalDateTime creadoEn
    ) {
        return Mensaje.builder()
                .id(id)
                .conversacionId(conversacionId)
                .waMessageId(waMessageId)
                .tipo(tipo)
                .direccion(direccion)
                .contenido(contenido)
                .mediaUrl(mediaUrl)
                .status(status)
                .enviadoPor(enviadoPor)
                .interna(interna)
                .creadoEn(creadoEn)
                .build();
    }

    public Mensaje actualizarStatus(StatusMensaje nuevoStatus) {
        DomainAssert.notNull(nuevoStatus, "status");
        return Mensaje.builder()
                .id(this.id)
                .conversacionId(this.conversacionId)
                .waMessageId(this.waMessageId)
                .tipo(this.tipo)
                .direccion(this.direccion)
                .contenido(this.contenido)
                .mediaUrl(this.mediaUrl)
                .status(nuevoStatus)
                .enviadoPor(this.enviadoPor)
                .interna(this.interna)
                .creadoEn(this.creadoEn)
                .build();
    }
}
