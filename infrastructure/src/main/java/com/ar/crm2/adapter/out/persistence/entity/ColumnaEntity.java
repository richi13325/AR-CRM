package com.ar.crm2.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity for Columna persistence.
 * Maps to the 'columnas' table with all documented columns.
 * Uses String id to match database convention (UUID stored as VARCHAR).
 * No relationship to TableroEntity or ColumnaTableroEntity — this is the catalog view.
 * Note: tablero_id column still exists in the database for backward compatibility,
 * but this entity no longer maps it. Board-column relationships are handled via
 * ColumnaTableroEntity (the junction table).
 */
@Entity
@Table(name = "columnas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
public class ColumnaEntity {

    @jakarta.persistence.Id
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private String id;

    @Column(name = "columna_nombre", nullable = false, length = 80)
    private String columnaNombre;

    @Column(name = "color", length = 7)
    private String color;

    @Column(name = "tipo_tablero", nullable = false)
    private String tipoTablero;

    @Column(name = "tipo_columna", nullable = false)
    private String tipoColumna;
}