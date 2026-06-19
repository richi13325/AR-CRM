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
                .creadoEn(LocalDateTime.now())
                .build();
    }

    public static Grupo reconstitute(
            GrupoId id, CanalWhatsappId canalId, String jid, String nombre,
            int noLeidos, LocalDateTime ultimoMensajeAt, LocalDateTime creadoEn) {
        return Grupo.builder()
                .id(id).canalId(canalId).jid(jid).nombre(nombre)
                .noLeidos(noLeidos).ultimoMensajeAt(ultimoMensajeAt).creadoEn(creadoEn)
                .build();
    }

    /** Registra un mensaje entrante: incrementa no leídos y actualiza la última actividad. */
    public Grupo conMensajeEntrante(LocalDateTime timestamp) {
        return toBuilder().noLeidos(this.noLeidos + 1).ultimoMensajeAt(timestamp).build();
    }

    /** Registra un mensaje saliente: actualiza la última actividad sin tocar no leídos. */
    public Grupo conMensajeSaliente(LocalDateTime timestamp) {
        return toBuilder().ultimoMensajeAt(timestamp).build();
    }

    public Grupo marcarLeido() {
        return toBuilder().noLeidos(0).build();
    }

    private GrupoBuilder toBuilder() {
        return Grupo.builder()
                .id(this.id).canalId(this.canalId).jid(this.jid).nombre(this.nombre)
                .noLeidos(this.noLeidos).ultimoMensajeAt(this.ultimoMensajeAt).creadoEn(this.creadoEn);
    }
}
