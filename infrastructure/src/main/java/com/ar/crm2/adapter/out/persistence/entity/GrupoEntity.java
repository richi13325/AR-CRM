package com.ar.crm2.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "wa_grupo", uniqueConstraints = @UniqueConstraint(name = "uk_wa_grupo_jid", columnNames = "jid"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrupoEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "canal_id")
    private String canalId;

    @Column(name = "jid", nullable = false, length = 100)
    private String jid;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "no_leidos", nullable = false)
    private int noLeidos;

    @Column(name = "ultimo_mensaje_at")
    private LocalDateTime ultimoMensajeAt;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;
}
