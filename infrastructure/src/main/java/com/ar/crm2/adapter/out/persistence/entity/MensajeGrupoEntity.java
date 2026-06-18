package com.ar.crm2.adapter.out.persistence.entity;

import com.ar.crm2.whatsapp.domain.enums.DireccionMensaje;
import com.ar.crm2.whatsapp.domain.enums.StatusMensaje;
import com.ar.crm2.whatsapp.domain.enums.TipoMensaje;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "wa_mensaje_grupo",
    uniqueConstraints = @UniqueConstraint(name = "uk_wa_mensaje_grupo_wa_message_id", columnNames = "wa_message_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MensajeGrupoEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "grupo_id", nullable = false)
    private String grupoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "direccion", nullable = false, length = 20)
    private DireccionMensaje direccion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoMensaje tipo;

    @Column(name = "contenido", length = 4096)
    private String contenido;

    @Column(name = "media_url", length = 1000)
    private String mediaUrl;

    @Column(name = "remitente", length = 200)
    private String remitente;

    @Column(name = "remitente_tel", length = 50)
    private String remitenteTel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusMensaje status;

    @Column(name = "wa_message_id", length = 100)
    private String waMessageId;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;
}
