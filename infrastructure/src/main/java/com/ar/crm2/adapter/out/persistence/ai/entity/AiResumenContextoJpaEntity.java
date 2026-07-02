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
 * JPA entity for the AI context summary table.
 *
 * <p>Persistence projection of the rich domain
 * {@code com.ar.crm2.model.entity.ia.AiResumenContexto}. Facts and
 * inferences are stored as TEXT; the domain does not interpret them.
 * Source watermark is monotonic and is used by the application layer
 * to detect stale summaries.
 */
@Entity
@Table(name = "ai_resumen_contexto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiResumenContextoJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "actor_usuario_id", length = 36, nullable = false)
    private String actorUsuarioId;

    @Column(name = "empresa_id", length = 36, nullable = false)
    private String empresaId;

    @Column(name = "wa_conversacion_id", length = 64, nullable = false)
    private String waConversacionId;

    @Column(name = "contacto_id", length = 36)
    private String contactoId;

    @Column(name = "facts", columnDefinition = "TEXT", nullable = false)
    private String facts;

    @Column(name = "inferences", columnDefinition = "TEXT", nullable = false)
    private String inferences;

    @Column(name = "source_wa_mensaje_id", length = 64)
    private String sourceWaMensajeId;

    @Column(name = "source_watermark", nullable = false)
    private long sourceWatermark;

    @Column(name = "ai_conversacion_id", length = 36, nullable = false)
    private String aiConversacionId;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;
}
