package com.ar.crm2.adapter.out.persistence.ai.entity;

import com.ar.crm2.model.enums.OrigenMemoria;
import com.ar.crm2.model.enums.VisibilidadMemoria;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * JPA entity for the AI memory table.
 *
 * <p>Persistence projection of the rich domain
 * {@code com.crm2.model.entity.ia.AiMemoria}. Each row stores ONE
 * atomic memory scoped to a requester / company / chat-or-contact
 * (per design.md §Domain). No global or company-wide memory is
 * stored in this table.
 */
@Entity
@Table(name = "ai_memoria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiMemoriaJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "actor_usuario_id", length = 36, nullable = false)
    private String actorUsuarioId;

    @Column(name = "empresa_id", length = 36, nullable = false)
    private String empresaId;

    @Column(name = "wa_conversacion_id", length = 64)
    private String waConversacionId;

    @Column(name = "contacto_id", length = 36)
    private String contactoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibilidad", length = 30, nullable = false)
    private VisibilidadMemoria visibilidad;

    @Column(name = "contenido", columnDefinition = "TEXT", nullable = false)
    private String contenido;

    @Enumerated(EnumType.STRING)
    @Column(name = "origen_tipo", length = 30, nullable = false)
    private OrigenMemoria origenTipo;

    @Column(name = "origen_id", length = 64)
    private String origenId;

    @Column(name = "version", nullable = false)
    private long version;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "superseded_by", length = 36)
    private String supersededBy;

    @Column(name = "superseded", nullable = false)
    private boolean superseded;

    @Column(name = "expirada", nullable = false)
    private boolean expirada;
}
