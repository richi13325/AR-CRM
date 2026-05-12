package com.ar.crm2.config;

import com.ar.crm2.application.empresa.port.out.EmpresaRepositoryPort;
import com.ar.crm2.application.empresa.service.EmpresaService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Application wiring configuration.
 * Wires application services with infrastructure adapters without touching application/domain classes.
 *
 * Dependency flow: boot -> infrastructure -> application -> domain
 * This configuration lives in infrastructure so it can import both application services and adapters.
 */
@Configuration
public class WiringConfig {

    /**
     * Wires EmpresaService with the persistence adapter implementing EmpresaRepositoryPort.
     * EmpresaService implements CreateEmpresaPort and GetEmpresasPort — these become the
     * injected interface types for the controller.
     */
    @Bean
    public EmpresaService empresaService(EmpresaRepositoryPort repositoryPort) {
        return new EmpresaService(repositoryPort);
    }
}