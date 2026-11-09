package com.humanit.challenge.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import io.swagger.v3.oas.models.OpenAPI;

@SpringBootTest
class OpenApiConfigTest {

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        StartupTestHarness.supplyDevExternals(registry);
    }

    @Autowired
    private OpenAPI openAPI;

    @Test
    void openApiBeanExistsWithExpectedMetadata() {
        assertThat(openAPI).isNotNull();
        assertThat(openAPI.getInfo()).isNotNull();
        assertThat(openAPI.getInfo().getTitle()).isNotBlank();
        assertThat(openAPI.getInfo().getVersion()).isNotBlank();
        assertThat(openAPI.getInfo().getDescription()).isNotBlank();
    }
}
