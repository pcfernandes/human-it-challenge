package com.humanit.challenge.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * T-M16 / T-M19 / T-M15 (prod isolation): explicit prod activation loads only the
 * production configuration: the default dev profile must not leak in, dev settings are
 * absent, and the dev-gated demo-user bean is not registered.
 */
@SpringBootTest
@ActiveProfiles("prod")
class ProdProfileStartupTest {

    @DynamicPropertySource
    static void supplyExternals(DynamicPropertyRegistry registry) {
        StartupTestHarness.supplyProdExternals(registry);
    }

    @Autowired
    private Environment environment;

    @Autowired
    private ConfigurableApplicationContext context;

    @Autowired
    private AppPaginationProperties pagination;

    @Autowired
    private AppJwtProperties jwt;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void activeProfilesAreExactlyProdWithNoDefaultDevLeakage() { // T-M19
        assertThat(environment.getActiveProfiles()).containsExactly("prod");
    }

    @Test
    void prodYmlSettingsAreVisibleAndDevYmlSettingsAreAbsent() {
        assertThat(environment.getProperty("logging.level.root")).isEqualTo("WARN");
        assertThat(environment.getProperty("logging.level.com.humanit.challenge")).isEqualTo("INFO");
    }

    @Test
    void defaultsBindInProdToo() { // T-M16
        assertThat(pagination.defaultPage()).isZero();
        assertThat(pagination.defaultPageSize()).isEqualTo(20);
        assertThat(pagination.maxPageSize()).isEqualTo(100);
        assertThat(jwt.expirationMinutes()).isEqualTo(60);
        assertThat(jwt.secret()).isEqualTo(StartupTestHarness.VALID_SECRET_32_BYTES);
    }

    @Test
    void devGatedDemoUserBeanIsNotRegistered() { // T-M15 (prod isolation)
        assertThat(context.getBeansOfType(AppDemoUserProperties.class)).isEmpty();
        assertThat(context.getBeanNamesForType(DevConfigPropertiesRegistration.class)).isEmpty();
    }

    @Test
    void productionUserLoadsFromExternalBcryptConfigurationAndDemoUserDoesNot() {
        UserDetails productionUser = userDetailsService.loadUserByUsername(StartupTestHarness.PRODUCTION_USERNAME);
        assertThat(productionUser.getPassword()).startsWith("$2");
        assertThat(passwordEncoder.matches(StartupTestHarness.TEST_PASSWORD_INPUT, productionUser.getPassword())).isTrue();
        assertThat(productionUser.getAuthorities()).isEmpty();
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(StartupTestHarness.DEMO_USERNAME))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
