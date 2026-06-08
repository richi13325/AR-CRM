package com.ar.crm2.adapter.out.persistence.entity;

import com.ar.crm2.model.enums.TipoFicha;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * JPA entity for Ficha persistence.
 * Maps to the 'fichas' table with all documented columns.
 * Uses String id to match database convention (UUID stored as VARCHAR).
 *
 * <p>Owns {@link FichaEtiquetaEntity} children via a {@code @OneToMany} with
 * {@code cascade=ALL} and {@code orphanRemoval=true} so the relation lifecycle
 * is bound to the parent Ficha aggregate.
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

    /**
     * Child etiqueta relations. The aggregate owns the list: when a Ficha
     * is removed or replaced, the relation rows are removed or orphan-deleted
     * by Hibernate. To clear all tags, set the list to a new empty list and
     * save — orphanRemoval will DELETE the previous rows.
     */
    @OneToMany(mappedBy = "ficha", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private java.util.List<FichaEtiquetaEntity> etiquetas = new java.util.ArrayList<>();
}