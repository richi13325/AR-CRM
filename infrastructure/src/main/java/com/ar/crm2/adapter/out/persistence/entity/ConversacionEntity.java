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

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;
}
