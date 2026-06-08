package com.ar.crm2.adapter.out.persistence.entity;

import com.ar.crm2.model.enums.TipoEtiqueta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * JPA entity for the global Etiqueta (tag) catalog.
 *
 * <p>Maps to the {@code etiquetas} table:
 * <ul>
 *   <li>{@code id} is stored as VARCHAR (UUID string) to match the project convention.</li>
 *   <li>{@code nombre} + {@code tipo_etiqueta} are enforced as a unique pair
 *       at the database level ({@code uk_etiquetas_nombre_tipo}) — the application
 *       layer performs the pre-check via the {@code ExistsEtiquetaByNombreAndTipoPort}
 *       adapter, but the DB constraint is the final safety net.</li>
 *   <li>{@code color} is stored upper-cased to match the domain normalization.</li>
 * </ul>
 */
@Entity
@Table(
    name = "etiquetas",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_etiquetas_nombre_tipo",
        columnNames = {"nombre", "tipo_etiqueta"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
public class EtiquetaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    @EqualsAndHashCode.Include
    private String id;

    @Column(name = "nombre", length = 50, nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_etiqueta", length = 20, nullable = false)
    private TipoEtiqueta tipoEtiqueta;

    @Column(name = "color", length = 7, nullable = false)
    private String color;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;
}
