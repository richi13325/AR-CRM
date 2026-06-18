package com.ar.crm2.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "wa_bot")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BotEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "canal_id")
    private String canalId;

    @Column(name = "webhook_url", nullable = false, length = 500)
    private String webhookUrl;

    @Column(name = "api_access_token", nullable = false, length = 100)
    private String apiAccessToken;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;
}
