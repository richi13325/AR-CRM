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
        return Conversacion.builder()
                .id(ConversacionId.create())
                .canalId(canalId)
                .contactoId(null)
                .numeroTelefono(numeroTelefono)
                .nombreContacto(nombreContacto)
                .estado(EstadoConversacion.ABIERTA)
                .asignadoA(null)
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
            LocalDateTime creadoEn,
            LocalDateTime actualizadoEn
    ) {
        return Conversacion.builder()
                .id(id)
                .canalId(canalId)
                .contactoId(contactoId)
                .numeroTelefono(numeroTelefono)
                .nombreContacto(nombreContacto)
                .estado(estado)
                .asignadoA(asignadoA)
                .creadoEn(creadoEn)
                .actualizadoEn(actualizadoEn)
                .build();
    }

    public Conversacion asignarAgente(UsuarioId usuarioId) {
        DomainAssert.notNull(usuarioId, "usuarioId");
        return Conversacion.builder()
                .id(this.id)
                .canalId(this.canalId)
                .contactoId(this.contactoId)
                .numeroTelefono(this.numeroTelefono)
                .nombreContacto(this.nombreContacto)
                .estado(this.estado)
                .asignadoA(usuarioId)
                .creadoEn(this.creadoEn)
                .actualizadoEn(LocalDateTime.now())
                .build();
    }

    public Conversacion vincularContacto(ContactoId contactoId) {
        DomainAssert.notNull(contactoId, "contactoId");
        return Conversacion.builder()
                .id(this.id)
                .canalId(this.canalId)
                .contactoId(contactoId)
                .numeroTelefono(this.numeroTelefono)
                .nombreContacto(this.nombreContacto)
                .estado(this.estado)
                .asignadoA(this.asignadoA)
                .creadoEn(this.creadoEn)
                .actualizadoEn(LocalDateTime.now())
                .build();
    }

    public Conversacion cambiarEstado(EstadoConversacion nuevoEstado) {
        DomainAssert.notNull(nuevoEstado, "estado");
        return Conversacion.builder()
                .id(this.id)
                .canalId(this.canalId)
                .contactoId(this.contactoId)
                .numeroTelefono(this.numeroTelefono)
                .nombreContacto(this.nombreContacto)
                .estado(nuevoEstado)
                .asignadoA(this.asignadoA)
                .creadoEn(this.creadoEn)
                .actualizadoEn(LocalDateTime.now())
                .build();
    }
}
