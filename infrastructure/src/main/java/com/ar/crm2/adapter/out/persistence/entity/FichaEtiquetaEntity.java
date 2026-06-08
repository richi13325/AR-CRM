package com.ar.crm2.adapter.out.persistence.entity;

import com.ar.crm2.model.enums.TipoEtiqueta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity for the FichaEtiqueta owned relation.
 *
 * <p>Maps to the {@code fichas_etiquetas} join table. Mirrors the
 * {@code ColumnaTableroEntity} pattern: an owned child of {@link FichaEntity}
 * with cascade and orphan removal. Identity is the relation id (UUID string);
 * uniqueness is enforced at the database level by
 * {@code uk_fichas_etiquetas_ficha_etiqueta} (ficha_id, etiqueta_id) so the
 * same Etiqueta cannot be linked twice to the same Ficha.
 *
 * <p><b>Identity strategy</b>: the row owns its own generated UUID as its
 * technical id. The catalog {@code etiquetaId} is stored in its own
 * {@code etiqueta_id} column so the row can still reference the Etiqueta
 * catalog. The row id MUST NOT be a copy of the catalog id — it is a
 * technical id, not a domain identity. This contract is enforced by
 * {@code FichaMapperTest#toEntity_childRowId_isGeneratedUuidNotEqualToEtiquetaId}
 * and is the same strategy used by
 * {@code TableroMapper#toEntity} for {@code ColumnaTableroEntity}.
 *
 * <p>{@code tipo_etiqueta} is stored redundantly as a snapshot of the
 * catalog Etiqueta's type at the time the relation was created, so the
 * aggregate can reconstitute without an extra catalog lookup.
 */
@Entity
@Table(
    name = "fichas_etiquetas",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_fichas_etiquetas_ficha_etiqueta",
        columnNames = {"ficha_id", "etiqueta_id"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
public class FichaEtiquetaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    @EqualsAndHashCode.Include
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ficha_id", nullable = false)
    private FichaEntity ficha;

    @Column(name = "etiqueta_id", length = 36, nullable = false)
    private String etiquetaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_etiqueta", length = 20, nullable = false)
    private TipoEtiqueta tipoEtiqueta;
}
