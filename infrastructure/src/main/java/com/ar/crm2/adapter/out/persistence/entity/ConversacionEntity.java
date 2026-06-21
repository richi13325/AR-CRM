package com.ar.crm2.adapter.out.persistence.entity;

import com.ar.crm2.whatsapp.domain.enums.EstadoConversacion;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "wa_conversacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversacionEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "canal_id", nullable = false)
    private String canalId;

    @Column(name = "contacto_id")
    private String contactoId;

    @Column(name = "numero_telefono", nullable = false, length = 30)
    private String numeroTelefono;

    @Column(name = "nombre_contacto", length = 150)
    private String nombreContacto;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoConversacion estado;

    @Column(name = "asignado_a")
    private String asignadoA;

    @Column(name = "no_leidos", nullable = false)
    private int noLeidos;

    @Column(name = "ultimo_mensaje_at")
    private LocalDateTime ultimoMensajeAt;

    @Column(name = "ultimo_mensaje_texto", length = 200)
    private String ultimoMensajeTexto;

    @Column(name = "labels", length = 500)
    private String labels;

    @Column(name = "bot_activo", nullable = false)
    private boolean botActivo;

    @Column(name = "csat_score")
    private Integer csatScore;

    @Column(name = "csat_enviado_en")
    private LocalDateTime csatEnviadoEn;

    @Column(name = "aviso_fuera_en")
    private LocalDateTime avisoFueraEn;

    @Column(name = "foto_url", length = 500)
    private String fotoUrl;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;
}
