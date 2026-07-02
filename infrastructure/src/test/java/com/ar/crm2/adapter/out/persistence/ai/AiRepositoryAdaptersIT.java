package com.ar.crm2.adapter.out.persistence.ai;

import com.ar.crm2.adapter.out.persistence.ai.entity.AiAccionJpaEntity;
import com.ar.crm2.adapter.out.persistence.ai.entity.AiConversacionJpaEntity;
import com.ar.crm2.adapter.out.persistence.ai.entity.AiMemoriaJpaEntity;
import com.ar.crm2.adapter.out.persistence.ai.entity.AiMensajeJpaEntity;
import com.ar.crm2.adapter.out.persistence.ai.entity.AiResumenContextoJpaEntity;
import com.ar.crm2.adapter.out.persistence.ai.repository.AiAccionSpringDataRepository;
import com.ar.crm2.adapter.out.persistence.ai.repository.AiConversacionSpringDataRepository;
import com.ar.crm2.adapter.out.persistence.ai.repository.AiMemoriaSpringDataRepository;
import com.ar.crm2.adapter.out.persistence.ai.repository.AiMensajeSpringDataRepository;
import com.ar.crm2.adapter.out.persistence.ai.repository.AiResumenContextoSpringDataRepository;
import com.ar.crm2.model.enums.EstadoAccion;
import com.ar.crm2.model.enums.OrigenMemoria;
import com.ar.crm2.model.enums.RolMensajeAi;
import com.ar.crm2.model.enums.VisibilidadMemoria;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * H2 round-trip integration tests for the AI persistence layer
 * (PR 2, Phase 3). Wires the four new AI repository adapters
 * ({@link AiConversacionRepositoryAdapter},
 * {@link AiMensajeRepositoryAdapter},
 * {@link AiResumenContextoRepositoryAdapter},
 * {@link AiMemoriaRepositoryAdapter},
 * {@link AiAccionRepositoryAdapter}) as Spring beans through
 * {@code @Import} and round-trips data through the application
 * ports — same pattern used by
 * {@code EtiquetaRepositoryAdapterIT} and
 * {@code TableroRepositoryIT}.
 *
 * <p>Validates:
 * <ul>
 *   <li>UUID ↔ String id conversion at the persistence boundary.</li>
 *   <li>Domain → entity → domain round-trip preserves all fields.</li>
 *   <li>Derived queries used by the application services return the
 *       expected scope.</li>
 *   <li>Optimistic-lock baseline (version=1) is preserved on a fresh
 *       PENDING proposal.</li>
 * </ul>
 */
@DataJpaTest
@Import({
    AiConversacionRepositoryAdapter.class,
    AiConversacionSaveAdapter.class,
    AiMensajeRepositoryAdapter.class,
    AiResumenContextoRepositoryAdapter.class,
    AiMemoriaRepositoryAdapter.class,
    AiAccionRepositoryAdapter.class
})
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:ai_testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "ai-assistant.phase1.memory-writes-enabled=true",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AiRepositoryAdaptersIT {

    @Autowired private AiConversacionRepositoryAdapter conversacionReadAdapter;
    @Autowired private AiConversacionSaveAdapter conversacionWriteAdapter;
    @Autowired private AiConversacionSpringDataRepository conversacionRepo;

    @Autowired private AiMensajeRepositoryAdapter mensajeAdapter;
    @Autowired private AiMensajeSpringDataRepository mensajeRepo;

    @Autowired private AiResumenContextoRepositoryAdapter resumenAdapter;
    @Autowired private AiResumenContextoSpringDataRepository resumenRepo;

    @Autowired private AiMemoriaRepositoryAdapter memoriaAdapter;
    @Autowired private AiMemoriaSpringDataRepository memoriaRepo;

    @Autowired private AiAccionRepositoryAdapter accionAdapter;
    @Autowired private AiAccionSpringDataRepository accionRepo;

    // ── AiConversacion round-trip ──────────────────────────────────

    @Test
    void aiConversacion_saveAndFindById_shouldRoundTripDomain() {
        LocalDateTime ahora = LocalDateTime.of(2026, 6, 23, 15, 0);
        com.ar.crm2.model.entity.ia.AiConversacion domain = com.ar.crm2.model.entity.ia.AiConversacion.crear(
            com.ar.crm2.model.vo.EmpresaId.from(UUID.randomUUID()),
            com.ar.crm2.model.vo.UsuarioId.from(UUID.randomUUID()),
            "wa-conv-1",
            com.ar.crm2.model.vo.ContactoId.from(UUID.randomUUID()),
            ahora
        );

        com.ar.crm2.model.entity.ia.AiConversacion saved = conversacionWriteAdapter.save(domain);

        Optional<com.ar.crm2.model.entity.ia.AiConversacion> found =
            conversacionReadAdapter.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        assertEquals("wa-conv-1", found.get().getWaConversacionId());
        assertFalse(found.get().isArchivada());
    }

    @Test
    void aiConversacion_listByActor_shouldReturnFreshestFirst() {
        LocalDateTime ahora = LocalDateTime.of(2026, 6, 23, 15, 0);
        UUID actor = UUID.randomUUID();
        UUID empresa = UUID.randomUUID();
        com.ar.crm2.model.entity.ia.AiConversacion older = com.ar.crm2.model.entity.ia.AiConversacion.crear(
            com.ar.crm2.model.vo.EmpresaId.from(empresa),
            com.ar.crm2.model.vo.UsuarioId.from(actor),
            "wa-conv-old",
            null,
            ahora.minusMinutes(10)
        );
        com.ar.crm2.model.entity.ia.AiConversacion newer = com.ar.crm2.model.entity.ia.AiConversacion.crear(
            com.ar.crm2.model.vo.EmpresaId.from(empresa),
            com.ar.crm2.model.vo.UsuarioId.from(actor),
            "wa-conv-new",
            null,
            ahora
        );
        conversacionWriteAdapter.save(older);
        conversacionWriteAdapter.save(newer);

        List<com.ar.crm2.model.entity.ia.AiConversacion> result =
            conversacionReadAdapter.listByActor(actor, empresa, 10);

        assertEquals(2, result.size());
        assertEquals("wa-conv-new", result.get(0).getWaConversacionId(),
            "freshest conversation must come first");
    }

    // ── AiMensaje round-trip ───────────────────────────────────────

    @Test
    void aiMensaje_saveAndFindByConversacionId_shouldRoundTrip() {
        LocalDateTime ahora = LocalDateTime.of(2026, 6, 23, 15, 0);
        com.ar.crm2.model.entity.ia.AiConversacion conv = com.ar.crm2.model.entity.ia.AiConversacion.crear(
            com.ar.crm2.model.vo.EmpresaId.from(UUID.randomUUID()),
            com.ar.crm2.model.vo.UsuarioId.from(UUID.randomUUID()),
            "wa-conv-1",
            null,
            ahora
        );
        conversacionWriteAdapter.save(conv);

        com.ar.crm2.model.entity.ia.AiMensaje user = com.ar.crm2.model.entity.ia.AiMensaje.crear(
            conv.getId(), RolMensajeAi.USER, "hola",
            null, null, null, null, null, ahora
        );
        com.ar.crm2.model.entity.ia.AiMensaje assistant = com.ar.crm2.model.entity.ia.AiMensaje.crear(
            conv.getId(), RolMensajeAi.ASSISTANT, "buenos días",
            "gpt-4o-mini", 10, 20, 100L, null, ahora.plusSeconds(1)
        );
        mensajeAdapter.save(user);
        mensajeAdapter.save(assistant);

        List<com.ar.crm2.model.entity.ia.AiMensaje> result =
            mensajeAdapter.findByConversacionId(conv.getId().value());

        assertEquals(2, result.size());
        assertEquals(RolMensajeAi.USER, result.get(0).getRol());
        assertEquals(RolMensajeAi.ASSISTANT, result.get(1).getRol());
    }

    // ── AiResumenContexto round-trip ───────────────────────────────

    @Test
    void aiResumen_saveAndFindByConversacionId_shouldRoundTrip() {
        LocalDateTime ahora = LocalDateTime.of(2026, 6, 23, 15, 0);
        com.ar.crm2.model.entity.ia.AiConversacion conv = com.ar.crm2.model.entity.ia.AiConversacion.crear(
            com.ar.crm2.model.vo.EmpresaId.from(UUID.randomUUID()),
            com.ar.crm2.model.vo.UsuarioId.from(UUID.randomUUID()),
            "wa-conv-1",
            null,
            ahora
        );
        conversacionWriteAdapter.save(conv);

        com.ar.crm2.model.entity.ia.AiResumenContexto resumen = com.ar.crm2.model.entity.ia.AiResumenContexto.crear(
            conv.getActorUsuarioId(), conv.getEmpresaId(),
            "wa-conv-1", null,
            "Cliente quiere demo", "Probabilidad alta",
            null, 5L, conv.getId(), ahora
        );
        resumenAdapter.save(resumen);

        Optional<com.ar.crm2.model.entity.ia.AiResumenContexto> found =
            resumenAdapter.findByConversacionId(conv.getId().value());

        assertTrue(found.isPresent());
        assertEquals("Cliente quiere demo", found.get().getFacts());
        assertEquals(5L, found.get().getSourceWatermark());
    }

    // ── AiMemoria round-trip ───────────────────────────────────────

    @Test
    void aiMemoria_saveAndFindActivasByConversacionId_shouldReturnOnlyActive() {
        // Use wall-clock anchors so this test stays green regardless of
        // when it runs — the slice-9 hardening added an `expiresAt > now`
        // predicate, so a fixed `2026-06-23` clock would now leave every
        // seeded row past TTL. Same time-stable pattern as the sibling
        // `aiMemoria_findActivasByConversacionId_shouldExcludeExpiredByDateAndMalformedContactScopedRows`
        // and as the slice-7 fix on `aiAccion_findPendingExpired_shouldReturnOnlyPENDINGPastExpiry`.
        LocalDateTime ahora = LocalDateTime.now().withNano(0);
        UUID actor = UUID.randomUUID();
        UUID empresa = UUID.randomUUID();
        UUID waConv = UUID.randomUUID();

        com.ar.crm2.model.entity.ia.AiMemoria activa = com.ar.crm2.model.entity.ia.AiMemoria.crear(
            com.ar.crm2.model.vo.UsuarioId.from(actor),
            com.ar.crm2.model.vo.EmpresaId.from(empresa),
            waConv.toString(),
            null,
            VisibilidadMemoria.CONVERSACION_SCOPED,
            "Cliente prefiere email",
            OrigenMemoria.MANUAL,
            "wa-msg-1",
            ahora,
            ahora.plusDays(7)
        );
        // superseded row whose replacement time + content is different;
        // both anchors are still in the future so the date predicate
        // alone is not what excludes it (the `superseded = false`
        // predicate is). This row independently proves the superseded
        // predicate at the SQL boundary.
        com.ar.crm2.model.entity.ia.AiMemoria superseded = com.ar.crm2.model.entity.ia.AiMemoria.crear(
            com.ar.crm2.model.vo.UsuarioId.from(actor),
            com.ar.crm2.model.vo.EmpresaId.from(empresa),
            waConv.toString(),
            null,
            VisibilidadMemoria.CONVERSACION_SCOPED,
            "Cliente prefiere teléfono",
            OrigenMemoria.MANUAL,
            "wa-msg-2",
            ahora,
            ahora.plusDays(7)
        );
        // Triangulation row matching the spec scenario
        // "Expired memory is filtered" (ai-memory spec §Requirement:
        // Memory TTL): superseded=false AND expirada=false BUT
        // expires_at already in the past. The row is built via
        // `AiMemoria.reconstitute` because the `AiMemoria.crear` factory
        // invariant rejects rows whose expiresAt is not strictly after
        // the create-time anchor.
        //
        // This row independently proves the `expiresAt > :ahora`
        // predicate at the SQL boundary: if that predicate were dropped
        // from the production query, this row would satisfy every other
        // filter (matching actor/empresa/waConv, CONVERSACION_SCOPED,
        // superseded=false, expirada=false) and the assertion below
        // would fail with `expected: <1> but was: <2>`.
        com.ar.crm2.model.entity.ia.AiMemoria expiradaNoSuperseded = com.ar.crm2.model.entity.ia.AiMemoria.reconstitute(
            com.ar.crm2.model.vo.AiMemoriaId.create(),
            com.ar.crm2.model.vo.UsuarioId.from(actor),
            com.ar.crm2.model.vo.EmpresaId.from(empresa),
            waConv.toString(),
            null,
            VisibilidadMemoria.CONVERSACION_SCOPED,
            "memoria caducada por TTL",
            OrigenMemoria.MANUAL,
            "wa-msg-3",
            2L,
            ahora.minusMinutes(2),
            ahora.minusMinutes(2),
            ahora.minusMinutes(1),
            null,
            false,
            false
        );

        memoriaAdapter.save(activa);
        memoriaAdapter.save(superseded.supersede(com.ar.crm2.model.vo.AiMemoriaId.create(), ahora));
        memoriaAdapter.save(expiradaNoSuperseded);

        List<com.ar.crm2.model.entity.ia.AiMemoria> result = memoriaAdapter.findActivasByConversacionId(
            waConv, actor, empresa
        );

        assertEquals(1, result.size(),
            "findActivasByConversacionId must return ONLY the non-superseded, non-TTL-expired row; got "
                + result.size() + " row(s) — if a TTL-expired+non-superseded row leaked here, the "
                + "expiresAt > :ahora predicate is missing from the SQL boundary");
        assertEquals("Cliente prefiere email", result.get(0).getContenido());
        // Triangulation pin: the result row must be the one we saved as `activa`
        // (so we know both the superseded row AND the TTL-expired row are genuinely filtered out,
        // not just that the wrong row happened to win).
        assertEquals(activa.getId(), result.get(0).getId());
    }

    @Test
    void aiMemoria_findActivasByConversacionId_shouldExcludeExpiredByDateAndMalformedContactScopedRows() {
        LocalDateTime ahora = LocalDateTime.now().withNano(0);
        UUID actor = UUID.randomUUID();
        UUID empresa = UUID.randomUUID();
        UUID waConv = UUID.randomUUID();
        UUID contacto = UUID.randomUUID();

        com.ar.crm2.model.entity.ia.AiMemoria activa = com.ar.crm2.model.entity.ia.AiMemoria.crear(
            com.ar.crm2.model.vo.UsuarioId.from(actor),
            com.ar.crm2.model.vo.EmpresaId.from(empresa),
            waConv.toString(),
            null,
            VisibilidadMemoria.CONVERSACION_SCOPED,
            "Cliente prefiere email",
            OrigenMemoria.MANUAL,
            "wa-msg-1",
            ahora,
            ahora.plusDays(7)
        );
        memoriaAdapter.save(activa);

        memoriaRepo.save(AiMemoriaJpaEntity.builder()
            .id(UUID.randomUUID().toString())
            .actorUsuarioId(actor.toString())
            .empresaId(empresa.toString())
            .waConversacionId(waConv.toString())
            .contactoId(null)
            .visibilidad(VisibilidadMemoria.CONVERSACION_SCOPED)
            .contenido("expirada por fecha")
            .origenTipo(OrigenMemoria.MANUAL)
            .origenId("wa-msg-2")
            .version(1L)
            .creadoEn(ahora.minusDays(2))
            .actualizadoEn(ahora.minusDays(2))
            .expiresAt(ahora.minusMinutes(1))
            .superseded(false)
            .expirada(false)
            .build());

        memoriaRepo.save(AiMemoriaJpaEntity.builder()
            .id(UUID.randomUUID().toString())
            .actorUsuarioId(actor.toString())
            .empresaId(empresa.toString())
            .waConversacionId(waConv.toString())
            .contactoId(contacto.toString())
            .visibilidad(VisibilidadMemoria.CONTACTO_SCOPED)
            .contenido("solo contacto")
            .origenTipo(OrigenMemoria.MANUAL)
            .origenId("wa-msg-3")
            .version(1L)
            .creadoEn(ahora)
            .actualizadoEn(ahora)
            .expiresAt(ahora.plusDays(7))
            .superseded(false)
            .expirada(false)
            .build());

        List<com.ar.crm2.model.entity.ia.AiMemoria> result = memoriaAdapter.findActivasByConversacionId(
            waConv, actor, empresa
        );

        assertEquals(1, result.size());
        assertEquals(activa.getId(), result.get(0).getId());
        assertEquals(VisibilidadMemoria.CONVERSACION_SCOPED, result.get(0).getVisibilidad());
    }

    @Test
    void aiMemoria_delete_shouldRemoveRow() {
        LocalDateTime ahora = LocalDateTime.of(2026, 6, 23, 15, 0);
        com.ar.crm2.model.entity.ia.AiMemoria memoria = com.ar.crm2.model.entity.ia.AiMemoria.crear(
            com.ar.crm2.model.vo.UsuarioId.from(UUID.randomUUID()),
            com.ar.crm2.model.vo.EmpresaId.from(UUID.randomUUID()),
            UUID.randomUUID().toString(),
            null,
            VisibilidadMemoria.CONVERSACION_SCOPED,
            "Memoria efímera",
            OrigenMemoria.MANUAL,
            null,
            ahora,
            ahora.plusDays(1)
        );
        com.ar.crm2.model.entity.ia.AiMemoria saved = memoriaAdapter.save(memoria);

        memoriaAdapter.delete(saved);

        assertTrue(memoriaRepo.findById(saved.getId().value().toString()).isEmpty());
    }

    // ── AiAccion round-trip (existing sample extended) ─────────────

    @Test
    void aiAccion_saveAndFindById_shouldRoundTripAndStartWithVersionOne() {
        LocalDateTime ahora = LocalDateTime.of(2026, 6, 23, 15, 0);
        com.ar.crm2.model.entity.ia.AiAccion domain = com.ar.crm2.model.entity.ia.AiAccion.crear(
            com.ar.crm2.model.vo.EmpresaId.from(UUID.randomUUID()),
            com.ar.crm2.model.vo.UsuarioId.from(UUID.randomUUID()),
            "wa-conv-1",
            null,
            com.ar.crm2.model.vo.AiConversacionId.create(),
            "CREATE_TAREA",
            "{\"titulo\":\"Llamar\"}",
            "Cliente pidió demo",
            60,
            ahora
        );

        com.ar.crm2.model.entity.ia.AiAccion saved = accionAdapter.save(domain);

        Optional<com.ar.crm2.model.entity.ia.AiAccion> found = accionAdapter.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(EstadoAccion.PENDING, found.get().getEstado());
        assertEquals(1, found.get().getVersion(),
            "fresh PENDING proposals must start at version 1 for the optimistic-lock baseline");
        assertEquals("CREATE_TAREA", found.get().getTipoAccion());
        assertEquals(domain.getAiConversacionId(), found.get().getAiConversacionId(),
            "audit-link aiConversacionId must survive the round-trip");
        assertEquals(null, found.get().getWaMensajeId(),
            "audit-link waMensajeId null in source must remain null after round-trip");
    }

    @Test
    void aiAccion_saveAndFindById_shouldRoundTripWithWaMensajeId() {
        LocalDateTime ahora = LocalDateTime.of(2026, 6, 23, 15, 0);
        com.ar.crm2.model.entity.ia.AiAccion domain = com.ar.crm2.model.entity.ia.AiAccion.crear(
            com.ar.crm2.model.vo.EmpresaId.from(UUID.randomUUID()),
            com.ar.crm2.model.vo.UsuarioId.from(UUID.randomUUID()),
            "wa-conv-1",
            "wa-msg-42",
            com.ar.crm2.model.vo.AiConversacionId.create(),
            "CREATE_CONTACTO",
            "{\"nombre\":\"Juan\"}",
            "r",
            60,
            ahora
        );

        com.ar.crm2.model.entity.ia.AiAccion saved = accionAdapter.save(domain);
        com.ar.crm2.model.entity.ia.AiAccion found = accionAdapter.findById(saved.getId()).orElseThrow();

        assertEquals("wa-msg-42", found.getWaMensajeId(),
            "audit-link waMensajeId must survive the round-trip when set");
    }

    @Test
    void aiAccion_findPendingExpired_shouldReturnOnlyPENDINGPastExpiry() {
        LocalDateTime ahora = LocalDateTime.now().withNano(0);
        com.ar.crm2.model.entity.ia.AiAccion expired = com.ar.crm2.model.entity.ia.AiAccion.crear(
            com.ar.crm2.model.vo.EmpresaId.from(UUID.randomUUID()),
            com.ar.crm2.model.vo.UsuarioId.from(UUID.randomUUID()),
            "wa-conv-1",
            null,
            com.ar.crm2.model.vo.AiConversacionId.create(),
            "CREATE_TAREA",
            "{}",
            "expirada",
            60,
            ahora.minusHours(1)
        );
        com.ar.crm2.model.entity.ia.AiAccion expiredRejected = com.ar.crm2.model.entity.ia.AiAccion.crear(
            com.ar.crm2.model.vo.EmpresaId.from(UUID.randomUUID()),
            com.ar.crm2.model.vo.UsuarioId.from(UUID.randomUUID()),
            "wa-conv-2",
            null,
            com.ar.crm2.model.vo.AiConversacionId.create(),
            "CREATE_TAREA",
            "{}",
            "rechazada-expirada",
            60,
            ahora.minusHours(2)
        ).rechazar(ahora.minusMinutes(30));
        // fresh: base time 1 hour in the future + ttl of 24h → expiresAt
        // is comfortably beyond the adapter's wall-clock now, regardless
        // of when the test runs.
        com.ar.crm2.model.entity.ia.AiAccion fresh = com.ar.crm2.model.entity.ia.AiAccion.crear(
            com.ar.crm2.model.vo.EmpresaId.from(UUID.randomUUID()),
            com.ar.crm2.model.vo.UsuarioId.from(UUID.randomUUID()),
            "wa-conv-1",
            null,
            com.ar.crm2.model.vo.AiConversacionId.create(),
            "CREATE_TAREA",
            "{}",
            "fresca",
            1440,
            ahora.plusHours(1)
        );
        accionAdapter.save(expired);
        accionAdapter.save(expiredRejected);
        accionAdapter.save(fresh);

        List<com.ar.crm2.model.entity.ia.AiAccion> result = accionAdapter.findPendingExpired(10);

        assertEquals(1, result.size());
        assertEquals("expirada", result.get(0).getRationale());
        assertEquals(EstadoAccion.PENDING, result.get(0).getEstado());
    }

    // ── Sanity: entities annotated for JPA discovery ──────────────

    @Test
    void sanity_springDataRepositoriesShouldBeWired() {
        assertNotNull(conversacionRepo);
        assertNotNull(mensajeRepo);
        assertNotNull(resumenRepo);
        assertNotNull(memoriaRepo);
        assertNotNull(accionRepo);
    }

    // ── PR7 selector query — actor + PENDING + empresaId filter ─────

    @Test
    void aiAccion_listPendingByActor_returnsOnlyMatchingTenant() {
        // PR7 contract: the SQL filter MUST scope results by
        // (solicitadaPor, estado, empresaId). The selector endpoint
        // never returns rows for a different tenant even when the
        // same actor staged proposals there.
        LocalDateTime ahora = LocalDateTime.of(2026, 6, 29, 17, 0);
        UUID actor = UUID.randomUUID();
        UUID empresaA = UUID.randomUUID();
        UUID empresaB = UUID.randomUUID();

        com.ar.crm2.model.entity.ia.AiAccion pendienteEmpresaA = com.ar.crm2.model.entity.ia.AiAccion.crear(
                com.ar.crm2.model.vo.EmpresaId.from(empresaA),
                com.ar.crm2.model.vo.UsuarioId.from(actor),
                "wa-conv-1",
                null,
                com.ar.crm2.model.vo.AiConversacionId.create(),
                "CREATE_CONTACTO",
                "{}",
                "en empresa A",
                60,
                ahora
        );
        com.ar.crm2.model.entity.ia.AiAccion pendienteEmpresaB = com.ar.crm2.model.entity.ia.AiAccion.crear(
                com.ar.crm2.model.vo.EmpresaId.from(empresaB),
                com.ar.crm2.model.vo.UsuarioId.from(actor),
                "wa-conv-2",
                null,
                com.ar.crm2.model.vo.AiConversacionId.create(),
                "CREATE_TAREA",
                "{}",
                "en empresa B",
                60,
                ahora
        );
        com.ar.crm2.model.entity.ia.AiAccion confirmadaEmpresaA = com.ar.crm2.model.entity.ia.AiAccion.crear(
                com.ar.crm2.model.vo.EmpresaId.from(empresaA),
                com.ar.crm2.model.vo.UsuarioId.from(actor),
                "wa-conv-3",
                null,
                com.ar.crm2.model.vo.AiConversacionId.create(),
                "CREATE_TAREA",
                "{}",
                "ya confirmada",
                60,
                ahora
        );
        // Persist a non-PENDING row that MUST NOT be returned.
        com.ar.crm2.model.entity.ia.AiAccion confirmadaPersistida =
                accionAdapter.save(confirmadaEmpresaA.confirmar(ahora.plusMinutes(1)));

        com.ar.crm2.model.entity.ia.AiAccion pendienteAPersistida =
                accionAdapter.save(pendienteEmpresaA);
        com.ar.crm2.model.entity.ia.AiAccion pendienteBPersistida =
                accionAdapter.save(pendienteEmpresaB);

        // Sanity: the confirmado row exists, but it is CONFIRMED, not PENDING.
        assertEquals(EstadoAccion.CONFIRMED, confirmadaPersistida.getEstado());

        // Select pending actions for (actor, empresaA) — should return ONLY the one
        // pending row scoped to empresaA.
        List<com.ar.crm2.model.entity.ia.AiAccion> result = accionAdapter.listPendingByActor(
                actor, empresaA, 50
        );

        assertEquals(1, result.size(),
                "listPendingByActor MUST scope to actor + PENDING + empresaId; got " + result.size());
        assertEquals(pendienteAPersistida.getId(), result.get(0).getId());
        assertEquals(empresaA, result.get(0).getEmpresaId().value());
        assertEquals(EstadoAccion.PENDING, result.get(0).getEstado());
    }

    @Test
    void aiAccion_listPendingByActor_actorSinAccionesPendientes_retornaListaVacia() {
        // No setup row should produce an empty list.
        UUID actorSinAcciones = UUID.randomUUID();
        UUID empresaSinAcciones = UUID.randomUUID();

        List<com.ar.crm2.model.entity.ia.AiAccion> result = accionAdapter.listPendingByActor(
                actorSinAcciones, empresaSinAcciones, 10
        );

        assertTrue(result.isEmpty());
    }
}
