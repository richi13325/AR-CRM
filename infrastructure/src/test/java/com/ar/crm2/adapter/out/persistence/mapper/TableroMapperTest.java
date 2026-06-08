package com.ar.crm2.adapter.out.persistence.mapper;

import com.ar.crm2.adapter.out.persistence.entity.ColumnaEntity;
import com.ar.crm2.adapter.out.persistence.entity.ColumnaTableroEntity;
import com.ar.crm2.adapter.out.persistence.entity.TableroEntity;
import com.ar.crm2.adapter.out.persistence.repository.ColumnaRepository;
import com.ar.crm2.model.entity.Columna;
import com.ar.crm2.model.entity.ColumnaTablero;
import com.ar.crm2.model.entity.Tablero;
import com.ar.crm2.model.enums.TipoColumna;
import com.ar.crm2.model.enums.TipoTablero;
import com.ar.crm2.model.vo.ColumnaId;
import com.ar.crm2.model.vo.TableroId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link TableroMapper}.
 *
 * Tests the stateful mapper that uses ColumnaRepository to hydrate
 * catalog Columna data when mapping ColumnaTableroEntity → ColumnaTablero.
 * Covers:
 * - Happy path with catalog hydration
 * - Missing catalog Columna → throws IllegalStateException
 * - Entity → Domain with and without child columns
 * - Domain → Entity conversion
 */
@ExtendWith(MockitoExtension.class)
class TableroMapperTest {

    @Mock
    private ColumnaRepository columnaRepository;

    private TableroMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TableroMapper(columnaRepository);
    }

// ── Helpers ─────────────────────────────────────────────────────

    private ColumnaEntity createCatalogColumnaEntity(UUID id, String nombre, TipoTablero tipoTablero) {
        // Catalog Columna: no board ownership, tableroId is null
        return ColumnaEntity.builder()
            .id(id.toString())
            .columnaNombre(nombre)
            .color("#FFFFFF")
            .tipoTablero(tipoTablero.name())
            .tipoColumna(TipoColumna.PREDETERMINADA.name())
            .build();
    }

    private ColumnaTableroEntity createColumnaTableroEntity(
        String id,
        String columnaId,
        TipoTablero tipoTablero,
        int limiteWip,
        int orden
    ) {
        return ColumnaTableroEntity.builder()
            .id(id)
            .columnaId(columnaId)
            .tipoTablero(tipoTablero)
            .limiteWip(limiteWip)
            .nota("Test note")
            .totalValorEstimado(BigDecimal.ZERO)
            .orden(orden)
            .build();
    }

    private TableroEntity createTableroEntity(String id, String nombre, TipoTablero tipoTablero) {
        return TableroEntity.builder()
            .id(id)
            .nombre(nombre)
            .descripcion("Test description")
            .tipoTablero(tipoTablero)
            .creadoEn(LocalDateTime.now())
            .columnasTablero(new ArrayList<>())
            .build();
    }

    // ── toEntity (Domain → Entity) ───────────────────────────────────

    @Test
    void toEntity_shouldMapAllTableroFields() {
        UUID id = UUID.randomUUID();
        Tablero domain = Tablero.reconstitute(
            TableroId.from(id),
            "Sprint Board",
            "A board for sprint planning",
            List.of(),
            TipoTablero.TAREAS,
            LocalDateTime.now()
        );

        TableroEntity entity = mapper.toEntity(domain);

        assertEquals(id.toString(), entity.getId());
        assertEquals("Sprint Board", entity.getNombre());
        assertEquals("A board for sprint planning", entity.getDescripcion());
        assertEquals(TipoTablero.TAREAS, entity.getTipoTablero());
        assertNotNull(entity.getCreadoEn());
    }

    @Test
    void toEntity_shouldHandleNullDomain() {
        TableroEntity entity = mapper.toEntity(null);

        assertNull(entity);
    }

    @Test
    void toEntity_shouldMapEmptyColumnasList() {
        UUID id = UUID.randomUUID();
        Tablero domain = Tablero.reconstitute(
            TableroId.from(id),
            "Empty Board",
            "No columns",
            List.of(),
            TipoTablero.TAREAS,
            LocalDateTime.now()
        );

        TableroEntity entity = mapper.toEntity(domain);

        assertTrue(entity.getColumnasTablero().isEmpty());
    }

    @Test
    void toEntity_shouldMapColumnaTableroChildrenWithOrden() {
        UUID tableroId = UUID.randomUUID();
        UUID columna1Id = UUID.randomUUID();
        UUID columna2Id = UUID.randomUUID();

        ColumnaTablero ct1 = ColumnaTablero.reconstitute(
            ColumnaId.from(columna1Id),
            TipoTablero.TAREAS,
            5,
            null,
            BigDecimal.ZERO
        );
        ColumnaTablero ct2 = ColumnaTablero.reconstitute(
            ColumnaId.from(columna2Id),
            TipoTablero.TAREAS,
            3,
            "A note",
            BigDecimal.ZERO
        );

        Tablero domain = Tablero.reconstitute(
            TableroId.from(tableroId),
            "Board",
            "Description",
            List.of(ct1, ct2),
            TipoTablero.TAREAS,
            LocalDateTime.now()
        );

        TableroEntity entity = mapper.toEntity(domain);

        assertEquals(2, entity.getColumnasTablero().size());
        assertEquals(0, entity.getColumnasTablero().get(0).getOrden());
        assertEquals(1, entity.getColumnasTablero().get(1).getOrden());
    }

    // ── toDomain (Entity → Domain) ──────────────────────────────────

    @Test
    void toDomain_shouldHydrateCatalogColumnaForEachChild() {
        UUID tableroId = UUID.randomUUID();
        UUID columna1Id = UUID.randomUUID();
        UUID columna2Id = UUID.randomUUID();

        TableroEntity entity = createTableroEntity(tableroId.toString(), "Sprint Board", TipoTablero.TAREAS);
        entity.getColumnasTablero().add(createColumnaTableroEntity(
            UUID.randomUUID().toString(), columna1Id.toString(), TipoTablero.TAREAS, 5, 0));
        entity.getColumnasTablero().add(createColumnaTableroEntity(
            UUID.randomUUID().toString(), columna2Id.toString(), TipoTablero.TAREAS, 3, 1));

        // Mock catalog hydration
        when(columnaRepository.findById(columna1Id.toString()))
            .thenReturn(Optional.of(createCatalogColumnaEntity(columna1Id, "Backlog", TipoTablero.TAREAS)));
        when(columnaRepository.findById(columna2Id.toString()))
            .thenReturn(Optional.of(createCatalogColumnaEntity(columna2Id, "To Do", TipoTablero.TAREAS)));

        Tablero domain = mapper.toDomain(entity);

        assertNotNull(domain);
        assertEquals("Sprint Board", domain.getNombre());
        assertEquals(2, domain.getColumnasTablero().size());
        verify(columnaRepository).findById(columna1Id.toString());
        verify(columnaRepository).findById(columna2Id.toString());
    }

    @Test
    void toDomain_shouldThrowWhenCatalogColumnaNotFound() {
        UUID tableroId = UUID.randomUUID();
        UUID missingColumnaId = UUID.randomUUID();

        TableroEntity entity = createTableroEntity(tableroId.toString(), "Board", TipoTablero.TAREAS);
        entity.getColumnasTablero().add(createColumnaTableroEntity(
            UUID.randomUUID().toString(), missingColumnaId.toString(), TipoTablero.TAREAS, 5, 0));

        when(columnaRepository.findById(missingColumnaId.toString()))
            .thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> mapper.toDomain(entity)
        );

        assertTrue(exception.getMessage().contains("Catalog Columna not found"));
        assertTrue(exception.getMessage().contains(missingColumnaId.toString()));
    }

    @Test
    void toDomain_shouldHandleEmptyColumnasList() {
        UUID tableroId = UUID.randomUUID();
        TableroEntity entity = createTableroEntity(tableroId.toString(), "Empty Board", TipoTablero.TAREAS);

        Tablero domain = mapper.toDomain(entity);

        assertNotNull(domain);
        assertEquals("Empty Board", domain.getNombre());
        assertTrue(domain.getColumnasTablero().isEmpty());
        verifyNoInteractions(columnaRepository);
    }

    @Test
    void toDomain_shouldHandleNullEntity() {
        Tablero domain = mapper.toDomain(null);

        assertNull(domain);
    }

    @Test
    void toDomain_shouldMapAllChildColumnaTableroFields() {
        UUID tableroId = UUID.randomUUID();
        UUID columnaId = UUID.randomUUID();

        TableroEntity entity = createTableroEntity(tableroId.toString(), "Board", TipoTablero.TAREAS);
        entity.getColumnasTablero().add(createColumnaTableroEntity(
            UUID.randomUUID().toString(), columnaId.toString(), TipoTablero.TAREAS, 5, 0));

        when(columnaRepository.findById(columnaId.toString()))
            .thenReturn(Optional.of(createCatalogColumnaEntity(columnaId, "Backlog", TipoTablero.TAREAS)));

        Tablero domain = mapper.toDomain(entity);

        ColumnaTablero ct = domain.getColumnasTablero().get(0);
        assertEquals(ColumnaId.from(columnaId), ct.getColumnaId());
        assertEquals(TipoTablero.TAREAS, ct.getTipoTablero()); // from child entity
        assertEquals(5, ct.getLimiteWip());
        assertEquals(BigDecimal.ZERO, ct.getTotalValorEstimado());
    }

    @Test
    void toDomain_shouldHydrateFromCatalogForTipoTablero() {
        // The child entity stores tipoTablero redundantly, but mapper still hydrates
        // the catalog Columna for complete ColumnaTablero.reconstitute call
        UUID tableroId = UUID.randomUUID();
        UUID columnaId = UUID.randomUUID();

        TableroEntity entity = createTableroEntity(tableroId.toString(), "Board", TipoTablero.TRATOS);
        entity.getColumnasTablero().add(createColumnaTableroEntity(
            UUID.randomUUID().toString(), columnaId.toString(), TipoTablero.TRATOS, 10, 0));

        ColumnaEntity catalogColumna = ColumnaEntity.builder()
            .id(columnaId.toString())
            .columnaNombre("Negociación")
            .color("#00FF00")
            .tipoTablero(TipoTablero.TRATOS.name())
            .tipoColumna(TipoColumna.PREDETERMINADA.name())
            .build();

        when(columnaRepository.findById(columnaId.toString()))
            .thenReturn(Optional.of(catalogColumna));

        Tablero domain = mapper.toDomain(entity);

        assertNotNull(domain);
        assertEquals(TipoTablero.TRATOS, domain.getTipoTablero());
        assertEquals(1, domain.getColumnasTablero().size());
        assertEquals(ColumnaId.from(columnaId), domain.getColumnasTablero().get(0).getColumnaId());
    }

    // ── Integration-like: full round-trip mapping ─────────────────

    @Test
    void toDomain_toEntity_shouldPreserveIdAndNombre() {
        UUID tableroId = UUID.randomUUID();
        UUID columnaId = UUID.randomUUID();

        ColumnaTablero ct = ColumnaTablero.reconstitute(
            ColumnaId.from(columnaId),
            TipoTablero.TAREAS,
            5,
            "Note",
            BigDecimal.ZERO
        );

        Tablero original = Tablero.reconstitute(
            TableroId.from(tableroId),
            "Round-trip Board",
            "Description",
            List.of(ct),
            TipoTablero.TAREAS,
            LocalDateTime.now()
        );

        TableroEntity entity = mapper.toEntity(original);
        assertEquals(tableroId.toString(), entity.getId());
        assertEquals("Round-trip Board", entity.getNombre());
    }

    @Test
    void toEntity_shouldSetOrdenIncrementallyForChildren() {
        UUID tableroId = UUID.randomUUID();
        UUID columna1Id = UUID.randomUUID();
        UUID columna2Id = UUID.randomUUID();
        UUID columna3Id = UUID.randomUUID();

        List<ColumnaTablero> columnas = List.of(
            ColumnaTablero.reconstitute(ColumnaId.from(columna1Id), TipoTablero.TAREAS, 5, null, BigDecimal.ZERO),
            ColumnaTablero.reconstitute(ColumnaId.from(columna2Id), TipoTablero.TAREAS, 3, null, BigDecimal.ZERO),
            ColumnaTablero.reconstitute(ColumnaId.from(columna3Id), TipoTablero.TAREAS, 7, null, BigDecimal.ZERO)
        );

        Tablero domain = Tablero.reconstitute(
            TableroId.from(tableroId),
            "Board",
            "Desc",
            columnas,
            TipoTablero.TAREAS,
            LocalDateTime.now()
        );

        TableroEntity entity = mapper.toEntity(domain);

        assertEquals(3, entity.getColumnasTablero().size());
        assertEquals(0, entity.getColumnasTablero().get(0).getOrden());
        assertEquals(1, entity.getColumnasTablero().get(1).getOrden());
        assertEquals(2, entity.getColumnasTablero().get(2).getOrden());
    }

    // ── Slice-4 corrective: child row id ownership ────────────────
    //
    // The ColumnaTableroEntity row MUST own its own generated UUID as
    // its `id`, and MUST NOT reuse/copy the catalog columnaId into the
    // row id. This mirrors the FichaMapper contract for FichaEtiquetaEntity
    // and is the explicit, consistent strategy required for both owned
    // relation rows.

    @Test
    void toEntity_childRowId_isGeneratedUuidNotEqualToColumnaId() {
        UUID tableroId = UUID.randomUUID();
        UUID columnaId = UUID.randomUUID();

        ColumnaTablero ct = ColumnaTablero.reconstitute(
            ColumnaId.from(columnaId), TipoTablero.TAREAS, 5, null, BigDecimal.ZERO
        );
        Tablero domain = Tablero.reconstitute(
            TableroId.from(tableroId), "Board", "Desc",
            List.of(ct), TipoTablero.TAREAS, LocalDateTime.now()
        );

        TableroEntity entity = mapper.toEntity(domain);

        ColumnaTableroEntity child = entity.getColumnasTablero().get(0);
        assertNotNull(child.getId(),
            "child row id must not be null — it must be a generated UUID");
        assertNotEquals(columnaId.toString(), child.getId(),
            "child row id MUST NOT be a copy of the catalog columnaId");
    }

    @Test
    void toEntity_eachChildRow_getsDistinctUuid() {
        UUID tableroId = UUID.randomUUID();
        UUID c1 = UUID.randomUUID();
        UUID c2 = UUID.randomUUID();
        UUID c3 = UUID.randomUUID();

        Tablero domain = Tablero.reconstitute(
            TableroId.from(tableroId), "Board", "Desc",
            List.of(
                ColumnaTablero.reconstitute(ColumnaId.from(c1), TipoTablero.TAREAS, 5, null, BigDecimal.ZERO),
                ColumnaTablero.reconstitute(ColumnaId.from(c2), TipoTablero.TAREAS, 3, null, BigDecimal.ZERO),
                ColumnaTablero.reconstitute(ColumnaId.from(c3), TipoTablero.TAREAS, 7, null, BigDecimal.ZERO)
            ),
            TipoTablero.TAREAS,
            LocalDateTime.now()
        );

        TableroEntity entity = mapper.toEntity(domain);

        List<String> childIds = entity.getColumnasTablero().stream()
            .map(ColumnaTableroEntity::getId)
            .toList();
        assertEquals(3, childIds.size());
        // All distinct
        assertEquals(3, childIds.stream().distinct().count(),
            "each child row must own a distinct UUID; no id is reused across siblings");
        // None of them matches a catalog id
        for (String id : childIds) {
            assertNotEquals(c1.toString(), id);
            assertNotEquals(c2.toString(), id);
            assertNotEquals(c3.toString(), id);
        }
    }

    @Test
    void toEntity_childRowId_isValidUuid() {
        UUID tableroId = UUID.randomUUID();
        UUID columnaId = UUID.randomUUID();

        Tablero domain = Tablero.reconstitute(
            TableroId.from(tableroId), "Board", "Desc",
            List.of(ColumnaTablero.reconstitute(
                ColumnaId.from(columnaId), TipoTablero.TAREAS, 5, null, BigDecimal.ZERO)),
            TipoTablero.TAREAS,
            LocalDateTime.now()
        );

        TableroEntity entity = mapper.toEntity(domain);

        String childId = entity.getColumnasTablero().get(0).getId();
        assertNotNull(childId);
        // Throws IllegalArgumentException if not a valid UUID
        UUID.fromString(childId);
    }

    @Test
    void toEntity_preservesColumnaIdAsSeparateField() {
        // The catalog linkage (columnaId) is preserved as a SEPARATE column
        // from the row id. Together with the assertion above, this proves
        // the row has its own technical id AND still references the catalog
        // Columna by its own id.
        UUID tableroId = UUID.randomUUID();
        UUID columnaId = UUID.randomUUID();

        Tablero domain = Tablero.reconstitute(
            TableroId.from(tableroId), "Board", "Desc",
            List.of(ColumnaTablero.reconstitute(
                ColumnaId.from(columnaId), TipoTablero.TAREAS, 5, null, BigDecimal.ZERO)),
            TipoTablero.TAREAS,
            LocalDateTime.now()
        );

        TableroEntity entity = mapper.toEntity(domain);

        ColumnaTableroEntity child = entity.getColumnasTablero().get(0);
        assertEquals(columnaId.toString(), child.getColumnaId());
        assertNotEquals(child.getId(), child.getColumnaId(),
            "row id and columnaId must be independent fields");
    }
}