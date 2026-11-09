package com.humanit.challenge.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Explicit dev activation: equivalent to the no-profile default start, but with the
 * profile set explicitly by the environment (SPRING_PROFILES_ACTIVE=dev equivalent).
 */
@SpringBootTest
@ActiveProfiles("dev")
class ExplicitDevProfileStartupTest {

    @DynamicPropertySource
    static void supplyExternals(DynamicPropertyRegistry registry) {
        StartupTestHarness.supplyDevExternals(registry);
    }

    @Autowired
    private Environment environment;

    @Autowired
    private AppPaginationProperties pagination;

    @Test
    void explicitDevLoadsDevelopmentConfiguration() {
        assertThat(environment.getActiveProfiles()).containsExactly("dev");
        assertThat(environment.getProperty("logging.level.com.humanit.challenge")).isEqualTo("DEBUG");
        assertThat(pagination.defaultPageSize()).isEqualTo(20);
        assertThat(pagination.maxPageSize()).isEqualTo(100);
    }
}
