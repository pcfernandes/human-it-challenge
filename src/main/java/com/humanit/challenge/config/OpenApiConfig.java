package com.humanit.challenge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/**
 * Minimal OpenAPI metadata bean (T1.5). Overrides springdoc's auto-configured
 * {@link OpenAPI} with stable application-level {@link Info}. No security scheme
 * or groups are registered here; those are deferred to Phase 3/4.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Human IT Challenge API")
                        .version("0.0.1-SNAPSHOT")
                        .description("REST API for managing Clients and Documents."));
    }
}
