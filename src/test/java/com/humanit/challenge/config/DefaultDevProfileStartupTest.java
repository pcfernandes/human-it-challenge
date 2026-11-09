package com.humanit.challenge.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * T-M1 / T-M2 / T-M8R: a context with <strong>no</strong> {@code @ActiveProfiles}
 * starts with the native {@code spring.profiles.default: dev} behavior — proving
 * approved decision OD-02.
 */
@SpringBootTest
class DefaultDevProfileStartupTest {

    @DynamicPropertySource
    static void supplyExternals(DynamicPropertyRegistry registry) {
        StartupTestHarness.supplyDevExternals(registry);
    }

    @Autowired
    private Environment environment;

    @Autowired
    private AppPaginationProperties pagination;

    @Autowired
    private AppJwtProperties jwt;

    @Test
    void noProfileAnnotationStartsWithExactlyDefaultDevProfile() { // T-M1 / T-M8R
        // spring.profiles.default activates dev for config loading but reports it via
        // getDefaultProfiles(); getActiveProfiles() stays empty for a default-dev start.
        assertThat(environment.getDefaultProfiles()).contains("dev");
        assertThat(environment.getActiveProfiles()).isEmpty();
    }

    @Test
    void committedDefaultsBind() { // T-M2
        assertThat(pagination.defaultPage()).isZero();
        assertThat(pagination.defaultPageSize()).isEqualTo(20);
        assertThat(pagination.maxPageSize()).isEqualTo(100);
        assertThat(jwt.expirationMinutes()).isEqualTo(60);
        assertThat(jwt.secret()).isEqualTo(StartupTestHarness.VALID_SECRET_32_BYTES);
    }

    @Test
    void applicationDevYmlSettingsAreVisible() { // T-M8R
        assertThat(environment.getProperty("logging.level.com.humanit.challenge")).isEqualTo("DEBUG");
    }
}
