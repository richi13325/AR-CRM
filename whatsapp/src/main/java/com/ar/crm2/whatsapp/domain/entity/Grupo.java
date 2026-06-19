package com.ar.crm2.whatsapp.domain.entity;

import com.ar.crm2.shared.DomainAssert;
import com.ar.crm2.whatsapp.domain.vo.CanalWhatsappId;
import com.ar.crm2.whatsapp.domain.vo.GrupoId;
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
public class Grupo {

    @EqualsAndHashCode.Include
    private final GrupoId id;

    private final CanalWhatsappId canalId;     // nullable
    private final String jid;                  // identificador WhatsApp del grupo (@g.us)
    private final String nombre;
    private final int noLeidos;
    private final LocalDateTime ultimoMensajeAt; // nullable
    private final String ultimoMensajeTexto;   // nullable — preview para la bandeja
    private final LocalDateTime creadoEn;

    public static Grupo create(CanalWhatsappId canalId, String jid, String nombre) {
        DomainAssert.notBlank(jid, "jid");
        return Grupo.builder()
                .id(GrupoId.create())
                .canalId(canalId)
                .jid(jid)
                .nombre(nombre != null && !nombre.isBlank() ? nombre : jid)
                .noLeidos(0)
                .ultimoMensajeAt(null)
                .ultimoMensajeTexto(null)
                .creadoEn(LocalDateTime.now())
                .build();
    }

    public static Grupo reconstitute(
            GrupoId id, CanalWhatsappId canalId, String jid, String nombre,
            int noLeidos, LocalDateTime ultimoMensajeAt, String ultimoMensajeTexto, LocalDateTime creadoEn) {
        return Grupo.builder()
                .id(id).canalId(canalId).jid(jid).nombre(nombre)
                .noLeidos(noLeidos).ultimoMensajeAt(ultimoMensajeAt)
                .ultimoMensajeTexto(ultimoMensajeTexto).creadoEn(creadoEn)
                .build();
    }

    /** Registra un mensaje entrante: incrementa no leídos y actualiza la última actividad y preview. */
    public Grupo conMensajeEntrante(LocalDateTime timestamp, String preview) {
        return toBuilder().noLeidos(this.noLeidos + 1).ultimoMensajeAt(timestamp)
                .ultimoMensajeTexto(recortar(preview)).build();
    }

    /** Registra un mensaje saliente: actualiza la última actividad y preview sin tocar no leídos. */
    public Grupo conMensajeSaliente(LocalDateTime timestamp, String preview) {
        return toBuilder().ultimoMensajeAt(timestamp).ultimoMensajeTexto(recortar(preview)).build();
    }

    public Grupo marcarLeido() {
        return toBuilder().noLeidos(0).build();
    }

    private static String recortar(String texto) {
        if (texto == null) return null;
        return texto.length() > 200 ? texto.substring(0, 200) : texto;
    }

    private GrupoBuilder toBuilder() {
        return Grupo.builder()
                .id(this.id).canalId(this.canalId).jid(this.jid).nombre(this.nombre)
                .noLeidos(this.noLeidos).ultimoMensajeAt(this.ultimoMensajeAt)
                .ultimoMensajeTexto(this.ultimoMensajeTexto).creadoEn(this.creadoEn);
    }
}
