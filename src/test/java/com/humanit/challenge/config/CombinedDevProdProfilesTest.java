package com.humanit.challenge.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Combined {@code dev,prod} activation loads both profile files but must not register the
 * development demo user; the production account remains the sole configured principal.
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

    @Autowired
    private UserDetailsService userDetailsService;

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
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(StartupTestHarness.DEMO_USERNAME))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
