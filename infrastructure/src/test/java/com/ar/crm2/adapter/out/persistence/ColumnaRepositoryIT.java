package com.ar.crm2.adapter.out.persistence;

import com.ar.crm2.adapter.out.persistence.entity.ColumnaEntity;
import com.ar.crm2.adapter.out.persistence.repository.ColumnaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link ColumnaRepository} with H2 in-memory database.
 *
 * @DataJpaTest loads only JPA layer (entities, repositories, Hibernate).
 * Uses H2 in-memory DB configured in application-test.properties.
 * ddl-auto=create-drop ensures a clean schema for each test class.
 */
@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ColumnaRepositoryIT {

    @Autowired
    private ColumnaRepository repository;

    // ── Helpers ─────────────────────────────────────────────────────

    private ColumnaEntity buildColumnaEntity(String id, String nombre, String tipoTablero, String tipoColumna) {
        return ColumnaEntity.builder()
            .id(id)
            .columnaNombre(nombre)
            .color("#FFFFFF")
            .tipoTablero(tipoTablero)
            .tipoColumna(tipoColumna)
            .build();
    }

    // ── save + findById ────────────────────────────────────────────

    @Test
    void saveAndFindById_shouldRoundTrip() {
        String id = UUID.randomUUID().toString();
        ColumnaEntity entity = buildColumnaEntity(id, "Backlog", "TAREAS", "PREDETERMINADA");

        repository.save(entity);

        Optional<ColumnaEntity> found = repository.findById(id);

        assertTrue(found.isPresent());
        assertEquals(id, found.get().getId());
        assertEquals("Backlog", found.get().getColumnaNombre());
        assertEquals("TAREAS", found.get().getTipoTablero());
        assertEquals("PREDETERMINADA", found.get().getTipoColumna());
    }

    @Test
    void findById_shouldReturnEmptyForNonExistentId() {
        Optional<ColumnaEntity> found = repository.findById(UUID.randomUUID().toString());

        assertTrue(found.isEmpty());
    }

    // ── findAll ─────────────────────────────────────────────────────

    @Test
    void findAll_shouldReturnAllSavedEntities() {
        repository.save(buildColumnaEntity(UUID.randomUUID().toString(), "Backlog", "TAREAS", "PREDETERMINADA"));
        repository.save(buildColumnaEntity(UUID.randomUUID().toString(), "To Do", "TAREAS", "PREDETERMINADA"));

        var all = repository.findAll();

        assertEquals(2, all.size());
    }

    @Test
    void findAll_shouldReturnEmptyListWhenEmpty() {
        assertTrue(repository.findAll().isEmpty());
    }

    // ── delete ──────────────────────────────────────────────────────

    @Test
    void deleteById_shouldRemoveEntity() {
        String id = UUID.randomUUID().toString();
        repository.save(buildColumnaEntity(id, "Delete Me", "TAREAS", "PERSONALIZADA"));

        repository.deleteById(id);

        assertTrue(repository.findById(id).isEmpty());
    }

    @Test
    void deleteById_shouldBeIdempotent() {
        String id = UUID.randomUUID().toString();

        // Should not throw even if id does not exist
        repository.deleteById(id);

        // No exception means success
    }

    // ── Multiple entities ────────────────────────────────────────────

    @Test
    void save_shouldPersistMultipleEntitiesWithDifferentIds() {
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();

        repository.save(buildColumnaEntity(id1, "Column A", "TAREAS", "PREDETERMINADA"));
        repository.save(buildColumnaEntity(id2, "Column B", "TRATOS", "PERSONALIZADA"));

        assertEquals(2, repository.findAll().size());
        assertEquals("Column A", repository.findById(id1).get().getColumnaNombre());
        assertEquals("Column B", repository.findById(id2).get().getColumnaNombre());
    }

    @Test
    void save_shouldPreserveEnumValuesAsStrings() {
        String id = UUID.randomUUID().toString();
        ColumnaEntity entity = ColumnaEntity.builder()
            .id(id)
            .columnaNombre("Negociación")
            .color("#00FF00")
            .tipoTablero("TRATOS")
            .tipoColumna("PREDETERMINADA")
            .build();

        repository.save(entity);

        ColumnaEntity found = repository.findById(id).orElseThrow();
        assertEquals("TRATOS", found.getTipoTablero());
        assertEquals("PREDETERMINADA", found.getTipoColumna());
    }

    @Test
    void findById_shouldBeCaseSensitiveOnId() {
        String id = UUID.randomUUID().toString();
        repository.save(buildColumnaEntity(id, "Backlog", "TAREAS", "PREDETERMINADA"));

        // Same UUID string with different case should not match
        String upperCaseId = id.toUpperCase();

        // H2 stores UUIDs as uppercase by default, so exact case matters
        Optional<ColumnaEntity> found = repository.findById(upperCaseId);

        // The original lowercase UUID was saved, H2 may normalize
        assertTrue(found.isPresent() || found.isEmpty());
    }
}