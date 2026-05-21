package com.ar.crm2.adapter.out.persistence.entity;

import com.ar.crm2.model.enums.PrioridadTarea;
import com.ar.crm2.model.enums.TipoTarea;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * JPA entity for tareas persistence.
 * Maps to the 'tareas' table with all documented columns.
 * Uses String id to match database convention (UUID stored as VARCHAR).
 */
@Entity
@Table(name = "tareas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TareaEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "trato_id", nullable = false)
    private String tratoId;

    @Column(name = "responsable_id", nullable = false)
    private String responsableId;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "descripcion", length = 1000)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", length = 50)
    private TipoTarea tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridad", length = 50)
    private PrioridadTarea prioridad;

    @Column(name = "fecha_limite")
    private LocalDateTime fechaLimite;

    @Column(name = "fecha_completada")
    private LocalDateTime fechaCompletada;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;
}