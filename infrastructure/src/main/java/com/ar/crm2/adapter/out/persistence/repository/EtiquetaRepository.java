package com.ar.crm2.adapter.out.persistence.repository;

import com.ar.crm2.adapter.out.persistence.entity.EtiquetaEntity;
import com.ar.crm2.model.enums.TipoEtiqueta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for the global Etiqueta catalog.
 * Uses String id matching the project convention (UUID stored as VARCHAR).
 *
 * <p>Derived queries back the application's uniqueness port and the
 * type-filtered list. The id-aware uniqueness check
 * ({@link #existsByNombreAndTipoEtiquetaAndIdNot(String, TipoEtiqueta, String)})
 * mirrors the {@code ExistsEtiquetaByNombreAndTipoPort.exists(nombre, tipo, excludeId)}
 * contract: pass {@code null} as {@code excludeId} for the create flow and
 * the editing id for the edit flow so the row being updated is ignored.
 */
@Repository
public interface EtiquetaRepository extends JpaRepository<EtiquetaEntity, String> {

    /**
     * Returns true if an Etiqueta row exists with the given name and type.
     * Used by the create flow (with {@code excludeId == null}).
     */
    boolean existsByNombreAndTipoEtiqueta(String nombre, TipoEtiqueta tipoEtiqueta);

    /**
     * Returns true if an Etiqueta row exists with the given name and type
     * AND its id is not the supplied {@code excludeId}.
     *
     * <p>Used by the edit flow: the row being edited matches the (nombre, tipo)
     * tuple against itself, so a naive existsByNombreAndTipo would return
     * true and falsely reject valid recolor-only or idempotent-rename
     * operations. Excluding the current id scopes the lookup correctly.
     *
     * <p>Spring Data accepts a {@code null} argument by translating it to
     * {@code IS NULL} in the generated query, which still works correctly
     * here because no row can have a null id.
     */
    boolean existsByNombreAndTipoEtiquetaAndIdNot(
        String nombre,
        TipoEtiqueta tipoEtiqueta,
        String excludeId
    );

    /**
     * Finds all Etiquetas whose {@code tipoEtiqueta} matches the given type.
     * Used by the application's {@code FindAllEtiquetasPort.findAll(tipo)}.
     */
    List<EtiquetaEntity> findByTipoEtiqueta(TipoEtiqueta tipoEtiqueta);
}
