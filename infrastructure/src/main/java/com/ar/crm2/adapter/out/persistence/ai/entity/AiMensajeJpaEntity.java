package com.ar.crm2.adapter.out.persistence.ai.entity;

import com.ar.crm2.model.enums.RolMensajeAi;
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
 * JPA entity for the AI message table.
 *
 * <p>Persistence projection of the rich domain
 * {@code com.ar.crm2.model.entity.ia.AiMensaje}. Tool calls are stored
 * opaquely as TEXT (no framework type leaks into domain/application).
 */
@Entity
@Table(name = "ai_mensaje")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiMensajeJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "ai_conversacion_id", length = 36, nullable = false)
    private String aiConversacionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", length = 20, nullable = false)
    private RolMensajeAi rol;

    @Column(name = "contenido", columnDefinition = "TEXT", nullable = false)
    private String contenido;

    @Column(name = "modelo", length = 100)
    private String modelo;

    @Column(name = "prompt_tokens")
    private Integer promptTokens;

    @Column(name = "completion_tokens")
    private Integer completionTokens;

    @Column(name = "latency_ms")
    private Long latencyMs;

    @Column(name = "tool_call_json", columnDefinition = "TEXT")
    private String toolCallJson;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;
}
