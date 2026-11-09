package com.humanit.challenge.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.PropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * T-M20 — combined {@code dev,prod} activation (behavior documentation, not a guard):
 * with no technical guard in the approved Revision 3 architecture, both profile files
 * load and the dev-gated demo-user configuration registers. This documents finding F-03
 * and the accepted residual-risk boundary (proposal Sections 9 and B.1, T-M20).
 */
@SpringBootTest
@ActiveProfiles({"dev", "prod"})
class CombinedDevProdProfilesTest {

    @DynamicPropertySource
    static void supplyExternals(DynamicPropertyRegistry registry) {
        StartupTestHarness.supplyDevExternals(registry);
        StartupTestHarness.supplyProdExternals(registry);
    }

    @Autowired
    private ConfigurableApplicationContext context;

    @Test
    void documentsF03BothProfileFilesLoad() {
        // Both profile-specific configuration files are present in the environment.
        Set<String> sourceNames = new LinkedHashSet<>();
        for (PropertySource<?> source : context.getEnvironment().getPropertySources()) {
            sourceNames.add(String.valueOf(source.getName()));
        }
        assertThat(sourceNames.toString()).contains("application-dev.yml").contains("application-prod.yml");

        // Unambiguous per-file evidence: root WARN is defined only in the prod file.
        assertThat(context.getEnvironment().getProperty("logging.level.root")).isEqualTo("WARN");
    }

    @Test
    void documentsF03DevGatedDemoUserBeanDoesNotRegisterUnderCombinedProfiles() {
        // Revision 4 (amendment 2): the dev & !prod guard keeps the dev-gated
        // demo-user bean from registering under combined dev,prod activation.
        assertThat(context.getBeansOfType(AppDemoUserProperties.class)).isEmpty();
        assertThat(context.getEnvironment().getActiveProfiles()).containsExactly("dev", "prod");
    }
}
