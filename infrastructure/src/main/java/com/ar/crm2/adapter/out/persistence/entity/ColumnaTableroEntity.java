package com.ar.crm2.adapter.out.persistence.entity;

import com.ar.crm2.model.enums.TipoEstadoColumnaTableroTarea;
import com.ar.crm2.model.enums.TipoEstadoColumnaTableroTrato;
import com.ar.crm2.model.enums.TipoTablero;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * JPA entity representing a column assignment within a Tablero aggregate.
 *
 * <p>Stores only the contextual data (limiteWip, nota, estado, order) and a
 * reference to the catalog Columna via {@code columnaId}. The catalog data
 * (nombre, color, tipoColumna) is resolved by the mapper on read.
 *
 * <p>This entity is a child of TableroEntity and must not exist independently.
 * Lifecycle is bound to the owning Tablero aggregate via cascade=ALL.
 */
@Entity
@Table(name = "columnas_tablero", uniqueConstraints = {
    @UniqueConstraint(name = "uk_columnas_tablero_tablero_columna", columnNames = {"tablero_id", "columna_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ColumnaTableroEntity {

    @Id
    @Column(name = "id")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tablero_id", nullable = false)
    private TableroEntity tablero;

    /**
     * Reference to the catalog Columna.
     * Stored as a String to match the database convention (UUID stored as VARCHAR).
     */
    @Column(name = "columna_id", nullable = false)
    private String columnaId;

    /**
     * Board type (TAREAS or TRATOS) — stored redundantly for validation without
     * requiring a catalog lookup on read.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_tablero", nullable = false)
    private TipoTablero tipoTablero;

    @Column(name = "limite_wip", nullable = false)
    private Integer limiteWip;

    @Column(name = "nota", length = 500)
    private String nota;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_tarea")
    private TipoEstadoColumnaTableroTarea estadoTarea;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_trato")
    private TipoEstadoColumnaTableroTrato estadoTrato;

    @Column(name = "total_valor_estimado", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalValorEstimado;

    @Column(name = "orden", nullable = false)
    private Integer orden;
}