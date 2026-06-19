package com.ar.crm2.whatsapp.domain.entity;

import com.ar.crm2.shared.DomainAssert;
import com.ar.crm2.whatsapp.domain.enums.DireccionMensaje;
import com.ar.crm2.whatsapp.domain.enums.StatusMensaje;
import com.ar.crm2.whatsapp.domain.enums.TipoMensaje;
import com.ar.crm2.whatsapp.domain.vo.GrupoId;
import com.ar.crm2.whatsapp.domain.vo.MensajeGrupoId;
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
public class MensajeGrupo {

    @EqualsAndHashCode.Include
    private final MensajeGrupoId id;

    private final GrupoId grupoId;
    private final DireccionMensaje direccion;
    private final TipoMensaje tipo;
    private final String contenido;
    private final String mediaUrl;
    private final String remitente;        // nombre de quien escribió en el grupo
    private final String remitenteTel;
    private final StatusMensaje status;
    private final String waMessageId;      // UNIQUE para idempotencia
    private final LocalDateTime timestamp;
    private final LocalDateTime creadoEn;

    public static MensajeGrupo createEntrante(
            GrupoId grupoId, String waMessageId, TipoMensaje tipo, String contenido,
            String mediaUrl, String remitente, String remitenteTel, LocalDateTime timestamp) {
        DomainAssert.notNull(grupoId, "grupoId");
        DomainAssert.notBlank(waMessageId, "waMessageId");
        DomainAssert.notNull(tipo, "tipo");
        return MensajeGrupo.builder()
                .id(MensajeGrupoId.create())
                .grupoId(grupoId)
                .direccion(DireccionMensaje.ENTRANTE)
                .tipo(tipo)
                .contenido(contenido)
                .mediaUrl(mediaUrl)
                .remitente(remitente)
                .remitenteTel(remitenteTel)
                .status(StatusMensaje.ENTREGADO)
                .waMessageId(waMessageId)
                .timestamp(timestamp != null ? timestamp : LocalDateTime.now())
                .creadoEn(LocalDateTime.now())
                .build();
    }

    /** Mensaje que el agente envía al grupo desde el CRM (sin remitente individual). */
    public static MensajeGrupo createSaliente(
            GrupoId grupoId, String waMessageId, TipoMensaje tipo, String contenido,
            String mediaUrl, LocalDateTime timestamp) {
        DomainAssert.notNull(grupoId, "grupoId");
        DomainAssert.notBlank(waMessageId, "waMessageId");
        DomainAssert.notNull(tipo, "tipo");
        return MensajeGrupo.builder()
                .id(MensajeGrupoId.create())
                .grupoId(grupoId)
                .direccion(DireccionMensaje.SALIENTE)
                .tipo(tipo)
                .contenido(contenido)
                .mediaUrl(mediaUrl)
                .remitente(null)
                .remitenteTel(null)
                .status(StatusMensaje.ENVIADO)
                .waMessageId(waMessageId)
                .timestamp(timestamp != null ? timestamp : LocalDateTime.now())
                .creadoEn(LocalDateTime.now())
                .build();
    }

    public static MensajeGrupo reconstitute(
            MensajeGrupoId id, GrupoId grupoId, DireccionMensaje direccion, TipoMensaje tipo,
            String contenido, String mediaUrl, String remitente, String remitenteTel,
            StatusMensaje status, String waMessageId, LocalDateTime timestamp, LocalDateTime creadoEn) {
        return MensajeGrupo.builder()
                .id(id).grupoId(grupoId).direccion(direccion).tipo(tipo)
                .contenido(contenido).mediaUrl(mediaUrl).remitente(remitente).remitenteTel(remitenteTel)
                .status(status).waMessageId(waMessageId).timestamp(timestamp).creadoEn(creadoEn)
                .build();
    }
}
