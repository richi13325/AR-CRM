package com.ar.crm2.config;

import com.ar.crm2.adapter.out.persistence.FichaRepositoryAdapter;
import com.ar.crm2.adapter.out.persistence.EtiquetaRepositoryAdapter;
import com.ar.crm2.adapter.out.persistence.repository.EtiquetaRepository;
import com.ar.crm2.adapter.out.persistence.repository.FichaEtiquetaRepository;
import com.ar.crm2.adapter.out.persistence.repository.FichaRepository;
import com.ar.crm2.application.etiqueta.port.out.FindEtiquetasByIdsPort;
import com.ar.crm2.application.ficha.port.in.CreateFichaUseCase;
import com.ar.crm2.application.ficha.port.in.EditFichaUseCase;
import com.ar.crm2.application.ficha.port.out.FindFichaByIdPort;
import com.ar.crm2.application.ficha.port.out.SaveFichaPort;
import com.ar.crm2.application.ficha.service.CreateFichaService;
import com.ar.crm2.application.ficha.service.EditFichaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Wiring-only test for the Ficha UseCase beans introduced or modified by
 * the add-ficha-etiquetas slice 3 carry-forward corrective.
 *
 * <p>Scope: prove that the boot composition root ({@link WiringConfig})
 * constructs {@link CreateFichaService} and {@link EditFichaService} with
 * their full constructor argument lists, including the
 * {@link FindEtiquetasByIdsPort} port that the slice-3 ficha services
 * require but the slice-3 wiring initially omitted.
 *
 * <p>This test now loads only the ficha-relevant bean subset from
 * {@link WiringConfig}. That keeps the composition-root contract for the
 * ficha services while avoiding unrelated transitive collaborators from the
 * broader boot graph.
 */
@SpringJUnitConfig(classes = FichaWiringTest.FichaSliceConfig.class)
class FichaWiringTest {

    @MockitoBean private FichaRepository fichaRepository;
    @MockitoBean private EtiquetaRepository etiquetaRepository;
    @MockitoBean private FichaEtiquetaRepository fichaEtiquetaRepository;

    @Autowired private CreateFichaUseCase createFichaUseCase;
    @Autowired private EditFichaUseCase editFichaUseCase;
    @Autowired private FichaRepositoryAdapter fichaRepositoryAdapter;
    @Autowired private EtiquetaRepositoryAdapter etiquetaRepositoryAdapter;

    @Configuration
    static class FichaSliceConfig {

        private final WiringConfig wiringConfig = new WiringConfig();

        @Bean
        FichaRepositoryAdapter fichaRepositoryAdapter(FichaRepository repository) {
            return wiringConfig.fichaRepositoryAdapter(repository);
        }

        @Bean
        EtiquetaRepositoryAdapter etiquetaRepositoryAdapter(
                EtiquetaRepository etiquetaRepository,
                FichaEtiquetaRepository fichaEtiquetaRepository
        ) {
            return wiringConfig.etiquetaRepositoryAdapter(etiquetaRepository, fichaEtiquetaRepository);
        }

        @Bean
        CreateFichaUseCase createFichaUseCase(
                FichaRepositoryAdapter savePort,
                EtiquetaRepositoryAdapter findEtiquetasPort
        ) {
            return wiringConfig.createFichaUseCase(savePort, findEtiquetasPort);
        }

        @Bean
        EditFichaUseCase editFichaUseCase(
                FichaRepositoryAdapter findPort,
                FichaRepositoryAdapter savePort,
                EtiquetaRepositoryAdapter findEtiquetasPort
        ) {
            return wiringConfig.editFichaUseCase(findPort, savePort, findEtiquetasPort);
        }
    }

    /**
     * Reflectively reads the private {@code findEtiquetasPort} field from
     * {@link CreateFichaService} so we can assert the wiring actually
     * injected a {@link FindEtiquetasByIdsPort} implementation. The
     * compile-time gate is not enough: the wiring could pass a
     * {@code null} or the wrong port and still compile (if the cast
     * matches the parameter type). The runtime reflection check
     * triangulates the contract.
     */
    @Test
    void createFichaService_receivesFindEtiquetasByIdsPortFromWiring() throws Exception {
        java.lang.reflect.Field field = CreateFichaService.class.getDeclaredField("findEtiquetasPort");
        field.setAccessible(true);
        Object injected = field.get(createFichaUseCase);
        assertThat(injected)
            .as("CreateFichaService must receive a FindEtiquetasByIdsPort; "
                + "the slice-3 corrective wired EtiquetaRepositoryAdapter for this slot")
            .isInstanceOf(FindEtiquetasByIdsPort.class)
            .isSameAs(etiquetaRepositoryAdapter);
    }

    /**
     * Same reflection check for {@link EditFichaService}: confirms the
     * wiring injected an EtiquetaRepositoryAdapter (which implements
     * {@link FindEtiquetasByIdsPort}) into the third constructor slot.
     */
    @Test
    void editFichaService_receivesFindEtiquetasByIdsPortFromWiring() throws Exception {
        java.lang.reflect.Field field = EditFichaService.class.getDeclaredField("findEtiquetasPort");
        field.setAccessible(true);
        Object injected = field.get(editFichaUseCase);
        assertThat(injected)
            .as("EditFichaService must receive a FindEtiquetasByIdsPort; "
                + "the slice-3 corrective wired EtiquetaRepositoryAdapter for this slot")
            .isInstanceOf(FindEtiquetasByIdsPort.class)
            .isSameAs(etiquetaRepositoryAdapter);
    }

    /**
     * The SaveFichaPort slot of {@link CreateFichaService} must be the
     * FichaRepositoryAdapter bean defined earlier in the same
     * {@link WiringConfig}. Triangulation: prove the
     * single-adapter-per-aggregate convention is preserved after the
     * slice-3 wiring extension.
     */
    @Test
    void createFichaService_receivesFichaRepositoryAdapterAsSavePort() throws Exception {
        java.lang.reflect.Field field = CreateFichaService.class.getDeclaredField("savePort");
        field.setAccessible(true);
        Object injected = field.get(createFichaUseCase);
        assertThat(injected)
            .isInstanceOf(SaveFichaPort.class)
            .isSameAs(fichaRepositoryAdapter);
    }

    /**
     * The {@code findPort} slot of {@link EditFichaService} must also
     * be the FichaRepositoryAdapter bean (it implements
     * {@link FindFichaByIdPort}). Same convention as the create service.
     */
    @Test
    void editFichaService_receivesFichaRepositoryAdapterAsFindPort() throws Exception {
        java.lang.reflect.Field field = EditFichaService.class.getDeclaredField("findPort");
        field.setAccessible(true);
        Object injected = field.get(editFichaUseCase);
        assertThat(injected)
            .isInstanceOf(FindFichaByIdPort.class)
            .isSameAs(fichaRepositoryAdapter);
    }

    /**
     * The bean instance exposed under the {@link CreateFichaUseCase} /
     * {@link EditFichaUseCase} types is the concrete service class.
     * This is the type-level contract the rest of the application
     * (controllers, etc.) depends on.
     */
    @Test
    void createFichaUseCase_isWiredAsCreateFichaService() {
        assertThat(createFichaUseCase)
            .isInstanceOf(CreateFichaService.class);
    }

    @Test
    void editFichaUseCase_isWiredAsEditFichaService() {
        assertThat(editFichaUseCase)
            .isInstanceOf(EditFichaService.class);
    }
}

