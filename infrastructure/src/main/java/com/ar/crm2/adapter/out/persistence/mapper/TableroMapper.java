package com.ar.crm2.adapter.out.persistence.mapper;

import com.ar.crm2.adapter.out.persistence.entity.ColumnaTableroEntity;
import com.ar.crm2.adapter.out.persistence.entity.TableroEntity;
import com.ar.crm2.adapter.out.persistence.repository.ColumnaRepository;
import com.ar.crm2.model.entity.Columna;
import com.ar.crm2.model.entity.ColumnaTablero;
import com.ar.crm2.model.entity.Tablero;
import com.ar.crm2.model.enums.TipoColumna;
import com.ar.crm2.model.vo.ColumnaId;
import com.ar.crm2.model.vo.TableroId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Mapper between persistence entities and domain entities for the Tablero aggregate.
 * Handles UUID/String conversion at the persistence boundary.
 *
 * <p>On read (Entity → Domain): resolves catalog {@link Columna} from {@link ColumnaRepository}
 * by {@code columnaId} to hydrate {@link ColumnaTablero#reconstitute} with the correct
 * {@code tipoTablero}. This avoids duplicating catalog fields in the relation entity.
 *
 * <p>On write (Domain → Entity): persists only the {@code columnaId} reference, not
 * catalog fields. The catalog remains the single source of truth for column definition.
 *
 * <p><b>Child row id strategy</b>: each {@code ColumnaTableroEntity} row
 * generated here owns its own UUID technical id. The catalog
 * {@code columnaId} is stored in its own {@code columna_id} column so the
 * row can still reference the Columna catalog. The row id MUST NOT be a
 * copy of the catalog id — it is a technical id, not a domain identity.
 *
 * <p>For NEW {@code Tablero} aggregates (no existing row in the database)
 * the row id is a freshly generated UUID. For EXISTING aggregates
 * (updates, column assignments, edits) the caller passes a map of
 * {@code columnaId → existing row id} so each child row keeps its
 * persisted primary key. This avoids PostgreSQL duplicate-key violations
 * on {@code uk_columnas_tablero_tablero_columna} when JPA treats a
 * re-saved child row as a new INSERT instead of an UPDATE.
 *
 * <p>This contract is enforced by
 * {@code TableroMapperTest#toEntity_childRowId_isGeneratedUuidNotEqualToColumnaId}
 * for new rows and
 * {@code TableroMapperTest#toEntity_existingChildRowIds_preservesPersistedRowId}
 * for updates, and is the same strategy used by {@code FichaMapper#toEntity} for
 * {@code FichaEtiquetaEntity}.
 */
public final class TableroMapper {

    private final ColumnaRepository columnaRepository;

    public TableroMapper(ColumnaRepository columnaRepository) {
        this.columnaRepository = columnaRepository;
    }

    // ── Domain → Entity ──────────────────────────────────────────────

    /**
     * Maps a domain Tablero to a persistence TableroEntity.
     * Used for save operations on NEW aggregates (no existing row to merge with).
     * Children (columnasTablero) are mapped inline with freshly generated UUIDs.
     */
    public TableroEntity toEntity(Tablero domain) {
        return toEntity(domain, java.util.Collections.emptyMap());
    }

    /**
     * Maps a domain Tablero to a persistence TableroEntity, reusing the
     * existing child row ids for updates.
     *
     * <p>For each child whose {@code columnaId} is present in
     * {@code existingChildRowIds}, the corresponding
     * {@code ColumnaTableroEntity.id} is reused (preserving the persisted
     * primary key). For children whose {@code columnaId} is NOT in the
     * map (i.e. genuinely new assignments), a fresh UUID is generated.
     *
     * @param domain                the domain aggregate to persist (non-null)
     * @param existingChildRowIds   map of {@code columnaId → existing row id}
     *                              for children that already exist in the
     *                              database; empty or null for new aggregates
     */
    public TableroEntity toEntity(Tablero domain, Map<String, String> existingChildRowIds) {
        if (domain == null) return null;

        Map<String, String> safeExisting = existingChildRowIds != null
            ? existingChildRowIds
            : java.util.Collections.emptyMap();

        TableroEntity entity = TableroEntity.builder()
            .id(domain.getId().value().toString())
            .nombre(domain.getNombre())
            .descripcion(domain.getDescripcion())
            .tipoTablero(domain.getTipoTablero())
            .creadoEn(domain.getCreadoEn())
            .build();

        List<ColumnaTableroEntity> childEntities = new ArrayList<>(domain.getColumnasTablero().size());
        int orden = 0;
        for (ColumnaTablero ct : domain.getColumnasTablero()) {
            childEntities.add(toColumnaTableroEntity(ct, entity, orden++, safeExisting));
        }
        entity.setColumnasTablero(childEntities);

        return entity;
    }

    /**
     * Maps a domain ColumnaTablero to a persistence ColumnaTableroEntity.
     * Used for save operations within a Tablero aggregate.
     *
     * <p>Identity strategy: the row owns its own UUID as its
     * technical id. The catalog {@code columnaId} is stored in its own
     * {@code columna_id} column so the row can still reference the
     * Columna catalog. The row id MUST NOT be a copy of the catalog id
     * (it is a technical id, not a domain identity) — this matches the
     * {@code FichaMapper#toEntity} contract for {@code FichaEtiquetaEntity}.
     *
     * <p>If {@code columnaId} is present in {@code existingChildRowIds},
     * the persisted row id is reused (for updates). Otherwise a fresh
     * UUID is generated (for new assignments).
     *
     * @param orden                the position of this column in the parent's column list (used for @OrderColumn)
     * @param existingChildRowIds  map of {@code columnaId → persisted row id}
     */
    private ColumnaTableroEntity toColumnaTableroEntity(
        ColumnaTablero ct,
        TableroEntity parent,
        int orden,
        Map<String, String> existingChildRowIds
    ) {
        String columnaIdKey = ct.getColumnaId().value().toString();
        String rowId = existingChildRowIds.getOrDefault(columnaIdKey, UUID.randomUUID().toString());

        return ColumnaTableroEntity.builder()
            .id(rowId)
            .tablero(parent)
            .columnaId(columnaIdKey)
            .tipoTablero(ct.getTipoTablero())
            .limiteWip(ct.getLimiteWip())
            .nota(ct.getNota())
            .totalValorEstimado(ct.getTotalValorEstimado())
            .orden(orden)
            .build();
    }

    // ── Entity → Domain ──────────────────────────────────────────────

    /**
     * Maps a persistence TableroEntity to a domain Tablero.
     * Used for find/load operations.
     */
    public Tablero toDomain(TableroEntity entity) {
        if (entity == null) return null;

        List<ColumnaTablero> columnasTablero = new ArrayList<>(entity.getColumnasTablero().size());
        for (ColumnaTableroEntity childEntity : entity.getColumnasTablero()) {
            columnasTablero.add(toColumnaTableroDomain(childEntity));
        }

        return Tablero.reconstitute(
            TableroId.from(UUID.fromString(entity.getId())),
            entity.getNombre(),
            entity.getDescripcion(),
            columnasTablero,
            entity.getTipoTablero(),
            entity.getCreadoEn()
        );
    }

    /**
     * Maps a persistence ColumnaTableroEntity to a domain ColumnaTablero.
     * Used for load operations within a Tablero aggregate.
     *
     * <p>Hydrates the catalog Columna from {@link ColumnaRepository} to resolve
     * {@code tipoTablero} for the {@link ColumnaTablero#reconstitute} factory.
     * Returns a placeholder Columna (with id and nombre only) to satisfy the
     * new {@code columnaId + tipoTablero} signature.
     */
    private ColumnaTablero toColumnaTableroDomain(ColumnaTableroEntity entity) {
        // Hydrate catalog Columna for tipoTablero (required by ColumnaTablero.reconstitute)
        ColumnaId columnaId = ColumnaId.from(UUID.fromString(entity.getColumnaId()));
        Columna catalogColumna = columnaRepository.findById(entity.getColumnaId())
            .map(ColumnaMapper::toDomain)
            .orElseThrow(() -> new IllegalStateException(
                "Catalog Columna not found for id: " + entity.getColumnaId() +
                ". Verify that all assigned columns exist in the catalog."));

        return ColumnaTablero.reconstitute(
            columnaId,
            entity.getTipoTablero(),
            entity.getLimiteWip(),
            entity.getNota(),
            entity.getTotalValorEstimado()
        );
    }
}