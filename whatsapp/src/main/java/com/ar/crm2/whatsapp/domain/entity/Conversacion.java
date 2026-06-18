package com.ar.crm2.whatsapp.domain.entity;

import com.ar.crm2.model.vo.ContactoId;
import com.ar.crm2.model.vo.UsuarioId;
import com.ar.crm2.shared.DomainAssert;
import com.ar.crm2.whatsapp.domain.enums.EstadoConversacion;
import com.ar.crm2.whatsapp.domain.vo.CanalWhatsappId;
import com.ar.crm2.whatsapp.domain.vo.ConversacionId;
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
public class Conversacion {

    @EqualsAndHashCode.Include
    private final ConversacionId id;

    private final CanalWhatsappId canalId;
    private final ContactoId contactoId;         // nullable — puede ser desconocido
    private final String numeroTelefono;          // número de WhatsApp
    private final String nombreContacto;          // nombre en WhatsApp
    private final EstadoConversacion estado;
    private final UsuarioId asignadoA;            // nullable
    private final int noLeidos;
    private final LocalDateTime ultimoMensajeAt;
    private final String ultimoMensajeTexto;      // preview del último mensaje
    private final LocalDateTime creadoEn;
    private final LocalDateTime actualizadoEn;

    public static Conversacion create(
            CanalWhatsappId canalId,
            String numeroTelefono,
            String nombreContacto
    ) {
        DomainAssert.notNull(canalId, "canalId");
        DomainAssert.notBlank(numeroTelefono, "numeroTelefono");

        LocalDateTime now = LocalDateTime.now();
        return base()
                .id(ConversacionId.create())
                .canalId(canalId)
                .contactoId(null)
                .numeroTelefono(numeroTelefono)
                .nombreContacto(nombreContacto)
                .estado(EstadoConversacion.ABIERTA)
                .asignadoA(null)
                .noLeidos(0)
                .ultimoMensajeAt(now)
                .ultimoMensajeTexto(null)
                .creadoEn(now)
                .actualizadoEn(now)
                .build();
    }

    public static Conversacion reconstitute(
            ConversacionId id,
            CanalWhatsappId canalId,
            ContactoId contactoId,
            String numeroTelefono,
            String nombreContacto,
            EstadoConversacion estado,
            UsuarioId asignadoA,
            int noLeidos,
            LocalDateTime ultimoMensajeAt,
            String ultimoMensajeTexto,
            LocalDateTime creadoEn,
            LocalDateTime actualizadoEn
    ) {
        return base()
                .id(id).canalId(canalId).contactoId(contactoId)
                .numeroTelefono(numeroTelefono).nombreContacto(nombreContacto)
                .estado(estado).asignadoA(asignadoA)
                .noLeidos(noLeidos).ultimoMensajeAt(ultimoMensajeAt).ultimoMensajeTexto(ultimoMensajeTexto)
                .creadoEn(creadoEn).actualizadoEn(actualizadoEn)
                .build();
    }

    public Conversacion asignarAgente(UsuarioId usuarioId) {
        DomainAssert.notNull(usuarioId, "usuarioId");
        return toBuilder().asignadoA(usuarioId).actualizadoEn(LocalDateTime.now()).build();
    }

    public Conversacion vincularContacto(ContactoId contactoId) {
        DomainAssert.notNull(contactoId, "contactoId");
        return toBuilder().contactoId(contactoId).actualizadoEn(LocalDateTime.now()).build();
    }

    public Conversacion cambiarEstado(EstadoConversacion nuevoEstado) {
        DomainAssert.notNull(nuevoEstado, "estado");
        return toBuilder().estado(nuevoEstado).actualizadoEn(LocalDateTime.now()).build();
    }

    /** Renombra el contacto si llega un nombre mejor (no usa números crudos). */
    public Conversacion conNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) return this;
        return toBuilder().nombreContacto(nombre).build();
    }

    /** Registra un mensaje entrante: sube no leídos y mueve la conversación al tope. */
    public Conversacion registrarMensajeEntrante(String preview, LocalDateTime timestamp) {
        LocalDateTime ts = timestamp != null ? timestamp : LocalDateTime.now();
        return toBuilder()
                .noLeidos(this.noLeidos + 1)
                .ultimoMensajeAt(ts)
                .ultimoMensajeTexto(recortar(preview))
                .estado(EstadoConversacion.ABIERTA)
                .actualizadoEn(LocalDateTime.now())
                .build();
    }

    /** Registra un mensaje saliente: actualiza preview y orden, sin tocar no leídos. */
    public Conversacion registrarMensajeSaliente(String preview, LocalDateTime timestamp) {
        LocalDateTime ts = timestamp != null ? timestamp : LocalDateTime.now();
        return toBuilder()
                .ultimoMensajeAt(ts)
                .ultimoMensajeTexto(recortar(preview))
                .actualizadoEn(LocalDateTime.now())
                .build();
    }

    public Conversacion marcarLeido() {
        if (this.noLeidos == 0) return this;
        return toBuilder().noLeidos(0).build();
    }

    private static ConversacionBuilder base() {
        return Conversacion.builder();
    }

    private ConversacionBuilder toBuilder() {
        return Conversacion.builder()
                .id(this.id).canalId(this.canalId).contactoId(this.contactoId)
                .numeroTelefono(this.numeroTelefono).nombreContacto(this.nombreContacto)
                .estado(this.estado).asignadoA(this.asignadoA)
                .noLeidos(this.noLeidos).ultimoMensajeAt(this.ultimoMensajeAt).ultimoMensajeTexto(this.ultimoMensajeTexto)
                .creadoEn(this.creadoEn).actualizadoEn(this.actualizadoEn);
    }

    private static String recortar(String texto) {
        if (texto == null) return null;
        return texto.length() > 120 ? texto.substring(0, 120) : texto;
    }
}
