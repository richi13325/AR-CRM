package com.ar.crm2.model.entity;

import com.ar.crm2.exception.EtiquetaTypeMismatchException;
import com.ar.crm2.model.enums.TipoEtiqueta;
import com.ar.crm2.model.vo.EtiquetaId;
import com.ar.crm2.shared.DomainAssert;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Owned relation object representing a Ficha's association with a global Etiqueta.
 *
 * <p>Mirrors the {@link ColumnaTablero} pattern: the catalog entity ({@link Etiqueta})
 * is the source of truth for name and color, while this relation lives inside the
 * {@link Ficha} aggregate and snapshots the {@link TipoEtiqueta} for invariant
 * reconstitution.
 *
 * <p>Identity: EtiquetaId (the catalog Etiqueta's id).
 * Equality: by all fields.
 * Lifecycle: bound to its owning Ficha aggregate.
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class FichaEtiqueta {

    @EqualsAndHashCode.Include
    private final EtiquetaId etiquetaId;

    private final TipoEtiqueta tipoEtiqueta;

    // ── Factory ──────────────────────────────────────────────────

    /**
     * Creates a new FichaEtiqueta pairing.
     *
     * @param etiquetaId  the catalog EtiquetaId (mandatory)
     * @param tipoEtiqueta the snapshot tag type (mandatory)
     */
    public static FichaEtiqueta create(EtiquetaId etiquetaId, TipoEtiqueta tipoEtiqueta) {
        DomainAssert.notNull(etiquetaId, "etiquetaId");
        DomainAssert.notNull(tipoEtiqueta, "tipoEtiqueta");
        return new FichaEtiqueta(etiquetaId, tipoEtiqueta);
    }

    /**
     * Reconstitutes a FichaEtiqueta from persistence. Skips structural validation
     * beyond null-checks (uniqueness is enforced by the aggregate owner).
     */
    public static FichaEtiqueta reconstitute(EtiquetaId etiquetaId, TipoEtiqueta tipoEtiqueta) {
        DomainAssert.notNull(etiquetaId, "etiquetaId");
        DomainAssert.notNull(tipoEtiqueta, "tipoEtiqueta");
        return new FichaEtiqueta(etiquetaId, tipoEtiqueta);
    }

    // ── Domain helpers ────────────────────────────────────────────

    /**
     * Whether this relation references the given Etiqueta by id.
     */
    public boolean matches(Etiqueta etiqueta) {
        return etiqueta != null && etiqueta.getId().equals(this.etiquetaId);
    }

    /**
     * Asserts that the snapshotted tipoEtiqueta matches the type of the given catalog Etiqueta.
     *
     * @throws EtiquetaTypeMismatchException if the types differ
     */
    public void assertTypeMatches(Etiqueta etiqueta) {
        DomainAssert.notNull(etiqueta, "etiqueta");
        if (!etiqueta.getTipoEtiqueta().equals(this.tipoEtiqueta)) {
            throw new EtiquetaTypeMismatchException();
        }
    }
}
