package com.ar.crm2.adapter.out.persistence.entity;

import com.ar.crm2.adapter.out.persistence.repository.EtiquetaRepository;
import com.ar.crm2.adapter.out.persistence.repository.FichaEtiquetaRepository;
import com.ar.crm2.adapter.out.persistence.repository.FichaRepository;
import com.ar.crm2.model.enums.TipoEtiqueta;
import com.ar.crm2.model.enums.TipoFicha;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration tests for the FichaEtiquetaEntity one-to-many child relation
 * and the {@code FichaEtiquetaRepository} cascade-delete query.
 *
 * <p>Validates:
 * <ul>
 *   <li>orphanRemoval deletes child rows when the parent's collection is
 *       replaced with a smaller list</li>
 *   <li>cascade ALL persists child rows together with the parent on save</li>
 *   <li>{@code FichaEtiquetaRepository.deleteByEtiquetaId} removes all
 *       relation rows referencing a given etiqueta</li>
 *   <li>{@code FichaEtiquetaRepository.countByEtiquetaId} returns the
 *       correct count for the "in use" decision</li>
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
class FichaEtiquetaEntityCascadeIT {

    @Autowired
    private FichaRepository fichaRepository;

    @Autowired
    private EtiquetaRepository etiquetaRepository;

    @Autowired
    private FichaEtiquetaRepository fichaEtiquetaRepository;

    // ── Helpers ─────────────────────────────────────────────────────

    private EtiquetaEntity buildEtiqueta(String id, String nombre, TipoEtiqueta tipo) {
        return EtiquetaEntity.builder()
            .id(id)
            .nombre(nombre)
            .tipoEtiqueta(tipo)
            .color("#FFFFFF")
            .creadoEn(LocalDateTime.now())
            .build();
    }

    private FichaEntity buildFicha(String id, String columnaId) {
        return FichaEntity.builder()
            .id(id)
            .columnaId(columnaId)
            .tipoFicha(TipoFicha.TAREA)
            .tratoId(null)
            .tareaId(UUID.randomUUID().toString())
            .actualizadoEn(Instant.now())
            .etiquetas(new ArrayList<>())
            .build();
    }

    private FichaEtiquetaEntity buildChild(String id, FichaEntity ficha, String etiquetaId, TipoEtiqueta tipo) {
        return FichaEtiquetaEntity.builder()
            .id(id)
            .ficha(ficha)
            .etiquetaId(etiquetaId)
            .tipoEtiqueta(tipo)
            .build();
    }

    // ── cascade persist ────────────────────────────────────────────

    @Test
    void save_withChildEtiquetas_shouldPersistBothParentAndChildren() {
        String fichaId = UUID.randomUUID().toString();
        String etiquetaId = UUID.randomUUID().toString();
        etiquetaRepository.saveAndFlush(buildEtiqueta(etiquetaId, "A", TipoEtiqueta.TAREA));

        FichaEntity ficha = buildFicha(fichaId, UUID.randomUUID().toString());
        ficha.getEtiquetas().add(buildChild(
            UUID.randomUUID().toString(), ficha, etiquetaId, TipoEtiqueta.TAREA));

        fichaRepository.saveAndFlush(ficha);

        FichaEntity found = fichaRepository.findById(fichaId).orElseThrow();
        assertEquals(1, found.getEtiquetas().size());
        assertEquals(etiquetaId, found.getEtiquetas().get(0).getEtiquetaId());
        assertEquals(TipoEtiqueta.TAREA, found.getEtiquetas().get(0).getTipoEtiqueta());
    }

    // ── orphan removal ─────────────────────────────────────────────

    @Test
    void replaceChildCollection_withSmallerList_shouldOrphanRemoveOldChildren() {
        String fichaId = UUID.randomUUID().toString();
        String e1 = UUID.randomUUID().toString();
        String e2 = UUID.randomUUID().toString();
        etiquetaRepository.saveAndFlush(buildEtiqueta(e1, "A", TipoEtiqueta.TAREA));
        etiquetaRepository.saveAndFlush(buildEtiqueta(e2, "B", TipoEtiqueta.TAREA));

        FichaEntity ficha = buildFicha(fichaId, UUID.randomUUID().toString());
        ficha.getEtiquetas().add(buildChild(
            UUID.randomUUID().toString(), ficha, e1, TipoEtiqueta.TAREA));
        ficha.getEtiquetas().add(buildChild(
            UUID.randomUUID().toString(), ficha, e2, TipoEtiqueta.TAREA));
        fichaRepository.saveAndFlush(ficha);

        // Replace the child collection with a single child pointing at e1
        FichaEntity loaded = fichaRepository.findById(fichaId).orElseThrow();
        String survivingChildId = loaded.getEtiquetas().get(0).getId();
        FichaEtiquetaEntity surviving = FichaEtiquetaEntity.builder()
            .id(survivingChildId)
            .ficha(loaded)
            .etiquetaId(e1)
            .tipoEtiqueta(TipoEtiqueta.TAREA)
            .build();
        loaded.getEtiquetas().clear();
        loaded.getEtiquetas().add(surviving);
        fichaRepository.saveAndFlush(loaded);

        // The ficha should now have exactly 1 child
        FichaEntity reloaded = fichaRepository.findById(fichaId).orElseThrow();
        assertEquals(1, reloaded.getEtiquetas().size());
        assertEquals(e1, reloaded.getEtiquetas().get(0).getEtiquetaId());

        // And the FichaEtiquetaRepository should also show 1 row total for this ficha
        long remaining = fichaEtiquetaRepository.findAll().stream()
            .filter(fe -> fe.getFicha().getId().equals(fichaId))
            .count();
        assertEquals(1, remaining);
    }

    // ── deleteByEtiquetaId (cascade-delete) ────────────────────────

    @Test
    void deleteByEtiquetaId_shouldRemoveAllRelationsReferencingThatEtiqueta() {
        String e1 = UUID.randomUUID().toString();
        String e2 = UUID.randomUUID().toString();
        etiquetaRepository.saveAndFlush(buildEtiqueta(e1, "A", TipoEtiqueta.TAREA));
        etiquetaRepository.saveAndFlush(buildEtiqueta(e2, "B", TipoEtiqueta.TAREA));

        // Ficha X linked to e1 and e2
        FichaEntity fichaX = buildFicha(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        fichaX.getEtiquetas().add(buildChild(
            UUID.randomUUID().toString(), fichaX, e1, TipoEtiqueta.TAREA));
        fichaX.getEtiquetas().add(buildChild(
            UUID.randomUUID().toString(), fichaX, e2, TipoEtiqueta.TAREA));
        fichaRepository.saveAndFlush(fichaX);

        // Ficha Y linked only to e1
        FichaEntity fichaY = buildFicha(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        fichaY.getEtiquetas().add(buildChild(
            UUID.randomUUID().toString(), fichaY, e1, TipoEtiqueta.TAREA));
        fichaRepository.saveAndFlush(fichaY);

        // Sanity: 3 relation rows total
        assertEquals(3, fichaEtiquetaRepository.findAll().stream()
            .filter(fe -> fe.getEtiquetaId().equals(e1) || fe.getEtiquetaId().equals(e2))
            .count());

        // Delete all relations for e1
        int deleted = fichaEtiquetaRepository.deleteByEtiquetaId(e1);
        fichaEtiquetaRepository.flush();

        assertEquals(2, deleted);

        // Only the relation for e2 should remain
        long remainingForE2 = fichaEtiquetaRepository.findAll().stream()
            .filter(fe -> fe.getEtiquetaId().equals(e2))
            .count();
        long remainingForE1 = fichaEtiquetaRepository.findAll().stream()
            .filter(fe -> fe.getEtiquetaId().equals(e1))
            .count();
        assertEquals(1, remainingForE2);
        assertEquals(0, remainingForE1);
    }

    @Test
    void deleteByEtiquetaId_withNoMatchingRows_shouldReturnZero() {
        int deleted = fichaEtiquetaRepository.deleteByEtiquetaId(UUID.randomUUID().toString());
        assertEquals(0, deleted);
    }

    // ── countByEtiquetaId ──────────────────────────────────────────

    @Test
    void countByEtiquetaId_shouldReturnNumberOfReferencingRelations() {
        String e1 = UUID.randomUUID().toString();
        etiquetaRepository.saveAndFlush(buildEtiqueta(e1, "A", TipoEtiqueta.TAREA));

        // 3 fichas, each linked to e1
        for (int i = 0; i < 3; i++) {
            FichaEntity ficha = buildFicha(UUID.randomUUID().toString(), UUID.randomUUID().toString());
            ficha.getEtiquetas().add(buildChild(
                UUID.randomUUID().toString(), ficha, e1, TipoEtiqueta.TAREA));
            fichaRepository.saveAndFlush(ficha);
        }

        long count = fichaEtiquetaRepository.countByEtiquetaId(e1);

        assertEquals(3L, count);
    }

    @Test
    void countByEtiquetaId_withNoRelations_shouldReturnZero() {
        long count = fichaEtiquetaRepository.countByEtiquetaId(UUID.randomUUID().toString());
        assertEquals(0L, count);
    }

    // ── DB unique constraint on (ficha_id, etiqueta_id) ────────────

    @Test
    void save_sameEtiquetaTwiceOnSameFicha_shouldViolateUniqueConstraint() {
        String fichaId = UUID.randomUUID().toString();
        String e1 = UUID.randomUUID().toString();
        etiquetaRepository.saveAndFlush(buildEtiqueta(e1, "A", TipoEtiqueta.TAREA));

        FichaEntity ficha = buildFicha(fichaId, UUID.randomUUID().toString());
        ficha.getEtiquetas().add(buildChild(
            UUID.randomUUID().toString(), ficha, e1, TipoEtiqueta.TAREA));
        ficha.getEtiquetas().add(buildChild(
            UUID.randomUUID().toString(), ficha, e1, TipoEtiqueta.TAREA)); // duplicate!

        // The unique constraint on (ficha_id, etiqueta_id) must fire on flush.
        assertThrows(DataIntegrityViolationException.class,
            () -> fichaRepository.saveAndFlush(ficha));
    }
}
