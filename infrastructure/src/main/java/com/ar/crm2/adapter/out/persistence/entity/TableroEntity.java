package com.ar.crm2.adapter.out.persistence.entity;

import com.ar.crm2.model.enums.TipoTablero;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * JPA entity for Tablero aggregate root persistence.
 * Columns are stored as child entities via {@code @OneToMany} with cascade=ALL
 * and orphanRemoval=true to preserve aggregate boundaries.
 */
@Entity
@Table(name = "tableros")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableroEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_tablero", nullable = false)
    private TipoTablero tipoTablero;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    /**
     * Child columns persisted within the aggregate boundary.
     * Uses {@code @OrderColumn} to maintain column order.
     * Cascade=ALL ensures column lifecycle is tied to the parent Tablero.
     */
    @OneToMany(mappedBy = "tablero", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "orden")
    @Builder.Default
    private java.util.List<ColumnaTableroEntity> columnasTablero = new java.util.ArrayList<>();
}