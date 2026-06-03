package com.ar.crm2.adapter.out.persistence.entity;

import com.ar.crm2.model.enums.TipoFicha;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * JPA entity for Ficha persistence.
 * Maps to the 'fichas' table with all documented columns.
 * Uses String id to match database convention (UUID stored as VARCHAR).
 */
@Entity
@Table(name = "fichas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FichaEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "columna_id", nullable = false)
    private String columnaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_ficha", nullable = false, length = 50)
    private TipoFicha tipoFicha;

    @Column(name = "trato_id")
    private String tratoId;

    @Column(name = "tarea_id")
    private String tareaId;

    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;
}