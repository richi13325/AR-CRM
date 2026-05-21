package com.ar.crm2.adapter.out.persistence.mapper;

import com.ar.crm2.adapter.out.persistence.entity.ColumnaEntity;
import com.ar.crm2.model.entity.Columna;
import com.ar.crm2.model.enums.TipoColumna;
import com.ar.crm2.model.enums.TipoTablero;
import com.ar.crm2.model.vo.ColumnaId;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ColumnaMapper}.
 * Tests the static mapper methods for entity ↔ domain conversion.
 * Covers happy paths and the null-entity failure path.
 */
class ColumnaMapperTest {

    // ── toEntity ────────────────────────────────────────────────────

    @Test
    void toEntity_shouldMapAllFields() {
        UUID id = UUID.randomUUID();
        Columna domain = Columna.reconstitute(
            ColumnaId.from(id),
            "Backlog",
            "#FF0000",
            TipoTablero.TAREAS,
            TipoColumna.PREDETERMINADA,
            false
        );

        ColumnaEntity entity = ColumnaMapper.toEntity(domain);

        assertEquals(id.toString(), entity.getId());
        assertEquals("Backlog", entity.getColumnaNombre());
        assertEquals("#FF0000", entity.getColor());
        assertEquals(TipoTablero.TAREAS.name(), entity.getTipoTablero());
        assertEquals(TipoColumna.PREDETERMINADA.name(), entity.getTipoColumna());
    }

    @Test
    void toEntity_shouldHandleNullDomain() {
        ColumnaEntity entity = ColumnaMapper.toEntity(null);

        assertNull(entity);
    }

    @Test
    void toEntity_shouldMapTipoTableroAndTipoColumnaAsEnumNames() {
        Columna domain = Columna.reconstitute(
            ColumnaId.create(),
            "To Do",
            "#00FF00",
            TipoTablero.TRATOS,
            TipoColumna.PERSONALIZADA,
            false
        );

        ColumnaEntity entity = ColumnaMapper.toEntity(domain);

        assertEquals("TRATOS", entity.getTipoTablero());
        assertEquals("PERSONALIZADA", entity.getTipoColumna());
    }

    // ── toDomain ────────────────────────────────────────────────────

    @Test
    void toDomain_shouldMapAllFields() {
        String idStr = UUID.randomUUID().toString();
        ColumnaEntity entity = ColumnaEntity.builder()
            .id(idStr)
            .columnaNombre("Sprint")
            .color("#FFFF00")
            .tipoTablero(TipoTablero.TAREAS.name())
            .tipoColumna(TipoColumna.PREDETERMINADA.name())
            .build();

        Columna domain = ColumnaMapper.toDomain(entity);

        assertEquals(UUID.fromString(idStr), domain.getId().value());
        assertEquals("Sprint", domain.getColumnanombre());
        assertEquals("#FFFF00", domain.getColor());
        assertEquals(TipoTablero.TAREAS, domain.getTipoTablero());
        assertEquals(TipoColumna.PREDETERMINADA, domain.getTipoColumna());
    }

    @Test
    void toDomain_shouldHandleNullEntity() {
        Columna domain = ColumnaMapper.toDomain(null);

        assertNull(domain);
    }

    @Test
    void toDomain_shouldThrowWhenEntityIdIsNull() {
        ColumnaEntity entity = ColumnaEntity.builder()
            .id(null)
            .columnaNombre("Backlog")
            .color("#FFFFFF")
            .tipoTablero(TipoTablero.TAREAS.name())
            .tipoColumna(TipoColumna.PREDETERMINADA.name())
            .build();

        assertThrows(IllegalArgumentException.class, () -> ColumnaMapper.toDomain(entity));
    }

    @Test
    void toDomain_shouldReconstituteWithStaleTableroId() {
        // Post-columnas-en-tableros, Columna.reconstitute no longer takes tableroId.
        // Any stale tablero_id in the DB is silently ignored at the persistence boundary.
        String idStr = UUID.randomUUID().toString();
        ColumnaEntity entity = ColumnaEntity.builder()
            .id(idStr)
            .columnaNombre("Review")
            .color("#FFFFFF")
            .tipoTablero(TipoTablero.TAREAS.name())
            .tipoColumna(TipoColumna.PREDETERMINADA.name())
            .build();

        Columna domain = ColumnaMapper.toDomain(entity);

        assertNotNull(domain);
        assertEquals("Review", domain.getColumnanombre());
    }

    @Test
    void toDomain_shouldParseTipoTableroAndTipoColumnaFromEnumNames() {
        String idStr = UUID.randomUUID().toString();
        ColumnaEntity entity = ColumnaEntity.builder()
            .id(idStr)
            .columnaNombre("Done")
            .color("#00FF00")
            .tipoTablero("TRATOS")
            .tipoColumna("PERSONALIZADA")
            .build();

        Columna domain = ColumnaMapper.toDomain(entity);

        assertEquals(TipoTablero.TRATOS, domain.getTipoTablero());
        assertEquals(TipoColumna.PERSONALIZADA, domain.getTipoColumna());
    }

    @Test
    void toDomain_shouldTreatNullColorAsDefaultWhite() {
        // Columna.reconstitute normalizes null color to "#FFFFFF"
        String idStr = UUID.randomUUID().toString();
        ColumnaEntity entity = ColumnaEntity.builder()
            .id(idStr)
            .columnaNombre("To Do")
            .color(null)
            .tipoTablero(TipoTablero.TAREAS.name())
            .tipoColumna(TipoColumna.PREDETERMINADA.name())
            .build();

        Columna domain = ColumnaMapper.toDomain(entity);

        assertEquals("#FFFFFF", domain.getColor());
    }
}