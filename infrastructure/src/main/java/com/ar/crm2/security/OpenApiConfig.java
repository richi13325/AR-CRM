package com.ar.crm2.security;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3 (Springdoc) configuration for CRM2 REST API.
 * Lives in infrastructure as REST API documentation configuration.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI crm2OpenApi() {
        return new OpenAPI().info(new Info()
                .title("CRM2 REST API")
                .description("Empresa management API — Create, List, Get by ID.")
                .version("1.0.0")
                .contact(new Contact()
                        .name("CRM2 Team")));
    }
}
