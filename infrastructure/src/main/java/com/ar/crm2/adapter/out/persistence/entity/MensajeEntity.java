package com.ar.crm2.adapter.out.persistence.entity;

import com.ar.crm2.whatsapp.domain.enums.DireccionMensaje;
import com.ar.crm2.whatsapp.domain.enums.StatusMensaje;
import com.ar.crm2.whatsapp.domain.enums.TipoMensaje;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "wa_mensaje",
    uniqueConstraints = @UniqueConstraint(name = "uk_wa_mensaje_wa_message_id", columnNames = "wa_message_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MensajeEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "conversacion_id", nullable = false)
    private String conversacionId;

    @Column(name = "wa_message_id", nullable = false, length = 100)
    private String waMessageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoMensaje tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "direccion", nullable = false, length = 20)
    private DireccionMensaje direccion;

    @Column(name = "contenido", length = 4096)
    private String contenido;

    @Column(name = "media_url", length = 1000)
    private String mediaUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusMensaje status;

    @Column(name = "enviado_por")
    private String enviadoPor;

    @Column(name = "interna", nullable = false)
    private boolean interna;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;
}
