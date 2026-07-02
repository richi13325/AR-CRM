package com.ar.crm2.adapter.out.persistence.ai.entity;

import com.ar.crm2.model.enums.EstadoAccion;
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
 * JPA entity for the AI action proposal table.
 *
 * <p>This is the persistence projection of the rich domain
 * {@code com.ar.crm2.model.entity.ia.AiAccion}. The class name in code
 * is {@code AiAccionJpaEntity} (PR2 sample); the database table is
 * {@code ai_accion} — chosen to match the domain class name and the
 * PR1 naming-cleanup decision (no legacy {@code ai_accion_propuesta}
 * table exists yet, so no backward-compat constraint applies; the
 * design.md reference is updated in the same change).
 *
 * <p>The entity is intentionally framework-only: no Spring AI, no
 * JPA callbacks, no business logic. All state-machine and ownership
 * invariants live in the domain entity and the application services.
 */
@Entity
@Table(name = "ai_accion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiAccionJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "empresa_id", length = 36, nullable = false)
    private String empresaId;

    @Column(name = "solicitada_por", length = 36, nullable = false)
    private String solicitadaPor;

    @Column(name = "wa_conversacion_id", length = 64, nullable = false)
    private String waConversacionId;

    /**
     * Optional audit-link to the specific WhatsApp message that
     * triggered the proposal. Nullable: many AI drafts come from
     * conversation-level context rather than a single message.
     */
    @Column(name = "wa_mensaje_id", length = 64)
    private String waMensajeId;

    /**
     * Required audit-link to the AI conversation that staged the
     * proposal. Always populated by the application dispatcher so the
     * audit trail can reconstruct which AI turn produced the proposal.
     */
    @Column(name = "ai_conversacion_id", length = 36, nullable = false)
    private String aiConversacionId;

    @Column(name = "tipo_accion", length = 50, nullable = false)
    private String tipoAccion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20, nullable = false)
    private EstadoAccion estado;

    @Column(name = "payload_json", columnDefinition = "TEXT", nullable = false)
    private String payloadJson;

    @Column(name = "rationale", columnDefinition = "TEXT", nullable = false)
    private String rationale;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "resultado_entidad_id", length = 64)
    private String resultadoEntidadId;

    @Column(name = "error_reason", columnDefinition = "TEXT")
    private String errorReason;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;
}
