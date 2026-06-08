package com.ar.crm2.adapter.out.persistence.entity;

import com.ar.crm2.adapter.out.persistence.repository.EtiquetaRepository;
import com.ar.crm2.model.enums.TipoEtiqueta;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for {@link EtiquetaEntity} and {@link EtiquetaRepository}
 * against H2 in-memory database. Validates:
 * <ul>
 *   <li>Round-trip persist + findById</li>
 *   <li>Id-aware existence check
 *       ({@code existsByNombreAndTipoEtiquetaAndIdNot}) — the contract that
 *       backs the application {@code ExistsEtiquetaByNombreAndTipoPort}
 *       so self-edits are not falsely rejected</li>
 *   <li>{@code uk_etiquetas_nombre_tipo} constraint enforcement at the
 *       database level (final safety net behind the application pre-check)</li>
 *   <li>{@code findByTipoEtiqueta} filter</li>
 * </ul>
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
class EtiquetaEntityMappingIT {

    @Autowired
    private EtiquetaRepository repository;

    private EtiquetaEntity buildEntity(String id, String nombre, TipoEtiqueta tipo, String color) {
        return EtiquetaEntity.builder()
            .id(id)
            .nombre(nombre)
            .tipoEtiqueta(tipo)
            .color(color)
            .creadoEn(LocalDateTime.now())
            .build();
    }

    // ── save + findById round trip ─────────────────────────────────

    @Test
    void saveAndFindById_shouldRoundTrip() {
        String id = UUID.randomUUID().toString();
        EtiquetaEntity entity = buildEntity(id, "Urgent", TipoEtiqueta.TAREA, "#FF0000");

        repository.saveAndFlush(entity);

        Optional<EtiquetaEntity> found = repository.findById(id);
        assertTrue(found.isPresent());
        assertEquals("Urgent", found.get().getNombre());
        assertEquals(TipoEtiqueta.TAREA, found.get().getTipoEtiqueta());
        assertEquals("#FF0000", found.get().getColor());
    }

    // ── existsByNombreAndTipoEtiquetaAndIdNot (id-aware) ──────────

    @Test
    void existsByNombreAndTipoEtiquetaAndIdNot_shouldReturnFalseWhenNoOtherRowSharesTuple() {
        // Note: two rows CANNOT share (nombre, tipo) because uk_etiquetas_nombre_tipo
        // forbids it. So with the editing row excluded, the existence check must
        // return false — the only way to get a true result is with no excludeId
        // and an existing matching row (covered in the next test).
        String id = UUID.randomUUID().toString();
        repository.saveAndFlush(buildEntity(id, "Urgent", TipoEtiqueta.TAREA, "#FF0000"));

        assertFalse(repository.existsByNombreAndTipoEtiquetaAndIdNot(
            "Urgent", TipoEtiqueta.TAREA, id));
    }

    @Test
    void existsByNombreAndTipoEtiquetaAndIdNot_shouldReturnFalseWhenOnlyExcludedRowMatches() {
        String id = UUID.randomUUID().toString();
        repository.saveAndFlush(buildEntity(id, "Urgent", TipoEtiqueta.TAREA, "#FF0000"));

        // Exclude the only row → false (this is the "self-edit" / "recolor" case)
        assertFalse(repository.existsByNombreAndTipoEtiquetaAndIdNot(
            "Urgent", TipoEtiqueta.TAREA, id));
    }

    @Test
    void existsByNombreAndTipoEtiquetaAndIdNot_shouldReturnTrueWhenAnyRowMatchesWithoutExclude() {
        String id = UUID.randomUUID().toString();
        repository.saveAndFlush(buildEntity(id, "Urgent", TipoEtiqueta.TAREA, "#FF0000"));

        // excludeId = null: a JPA derived query translates to IS NULL, which
        // still returns true if ANY row matches the (nombre, tipo) tuple.
        assertTrue(repository.existsByNombreAndTipoEtiquetaAndIdNot(
            "Urgent", TipoEtiqueta.TAREA, null));
    }

    @Test
    void existsByNombreAndTipoEtiquetaAndIdNot_shouldReturnFalseForDifferentTipo() {
        String id = UUID.randomUUID().toString();
        repository.saveAndFlush(buildEntity(id, "Urgent", TipoEtiqueta.TAREA, "#FF0000"));

        // Different tipo → no match
        assertFalse(repository.existsByNombreAndTipoEtiquetaAndIdNot(
            "Urgent", TipoEtiqueta.TRATO, id));
    }

    @Test
    void existsByNombreAndTipoEtiquetaAndIdNot_shouldReturnFalseWhenNoRowMatches() {
        // Empty catalog
        assertFalse(repository.existsByNombreAndTipoEtiquetaAndIdNot(
            "Anything", TipoEtiqueta.TAREA, null));
    }

    // ── findByTipoEtiqueta (list filter) ───────────────────────────

    @Test
    void findByTipoEtiqueta_shouldReturnOnlyMatchingRows() {
        repository.saveAndFlush(buildEntity(UUID.randomUUID().toString(), "A", TipoEtiqueta.TAREA, "#FFFFFF"));
        repository.saveAndFlush(buildEntity(UUID.randomUUID().toString(), "B", TipoEtiqueta.TRATO, "#000000"));
        repository.saveAndFlush(buildEntity(UUID.randomUUID().toString(), "C", TipoEtiqueta.TAREA, "#FF0000"));

        List<EtiquetaEntity> tareas = repository.findByTipoEtiqueta(TipoEtiqueta.TAREA);

        assertEquals(2, tareas.size());
        assertTrue(tareas.stream().allMatch(e -> e.getTipoEtiqueta() == TipoEtiqueta.TAREA));
    }

    @Test
    void findByTipoEtiqueta_shouldReturnEmptyListWhenNoMatch() {
        repository.saveAndFlush(buildEntity(UUID.randomUUID().toString(), "A", TipoEtiqueta.TAREA, "#FFFFFF"));

        assertTrue(repository.findByTipoEtiqueta(TipoEtiqueta.TRATO).isEmpty());
    }

    // ── DB unique constraint safety net ────────────────────────────

    @Test
    void save_withDuplicateNombreAndTipo_shouldThrowDataIntegrityViolation() {
        repository.saveAndFlush(buildEntity(UUID.randomUUID().toString(), "Urgent", TipoEtiqueta.TAREA, "#FF0000"));

        EtiquetaEntity duplicate = buildEntity(UUID.randomUUID().toString(), "Urgent", TipoEtiqueta.TAREA, "#0000FF");

        assertThrows(DataIntegrityViolationException.class,
            () -> repository.saveAndFlush(duplicate));
    }

    @Test
    void save_withSameNombreInDifferentTipo_shouldNotViolateConstraint() {
        repository.saveAndFlush(buildEntity(UUID.randomUUID().toString(), "Urgent", TipoEtiqueta.TAREA, "#FF0000"));

        EtiquetaEntity trato = buildEntity(UUID.randomUUID().toString(), "Urgent", TipoEtiqueta.TRATO, "#0000FF");

        EtiquetaEntity saved = repository.saveAndFlush(trato);

        assertEquals(TipoEtiqueta.TRATO, saved.getTipoEtiqueta());
    }
}
