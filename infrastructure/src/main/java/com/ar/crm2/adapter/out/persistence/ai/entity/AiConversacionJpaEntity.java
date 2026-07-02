package com.ar.crm2.adapter.out.persistence.ai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * JPA entity for the AI conversation table.
 *
 * <p>Persistence projection of the rich domain
 * {@code com.ar.crm2.model.entity.ia.AiConversacion}. The class is
 * intentionally framework-only: no Spring AI, no business logic, no
 * callbacks. All state and ownership invariants live in the domain
 * entity and the application services.
 */
@Entity
@Table(name = "ai_conversacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiConversacionJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "empresa_id", length = 36, nullable = false)
    private String empresaId;

    @Column(name = "actor_usuario_id", length = 36, nullable = false)
    private String actorUsuarioId;

    @Column(name = "wa_conversacion_id", length = 64, nullable = false)
    private String waConversacionId;

    @Column(name = "contacto_id", length = 36)
    private String contactoId;

    @Column(name = "archivada", nullable = false)
    private boolean archivada;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;
}
