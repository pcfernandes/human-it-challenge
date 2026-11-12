package com.humanit.challenge.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * T-M15 — demo-user properties: dev-profile-only binding, externalized values, and a
 * fail-fast when dev starts without them (correctly activated prod never binds them —
 * see {@link ProdProfileStartupTest#devGatedDemoUserBeanIsNotRegistered()}).
 */
class DemoUserPropertiesTest {

    @Test
    void externalizedDemoCredentialsBindAsBcryptInDev() { // T-M15 happy path
        Map<String, String> properties = StartupTestHarness.devProperties();
        try (var context = StartupTestHarness.runApp(properties, "dev")) {
            AppDemoUserProperties credentials = context.getBean(AppDemoUserProperties.class);
            assertThat(credentials.username()).isEqualTo(StartupTestHarness.DEMO_USERNAME);
            assertThat(credentials.passwordHash()).startsWith("$2");
            assertThat(new BCryptPasswordEncoder().matches(
                    StartupTestHarness.TEST_PASSWORD_INPUT, credentials.passwordHash())).isTrue();
        }
    }

    @Test
    void missingDemoCredentialsFailStartupInDev() { // T-M15 failure path (dev requires them)
        Map<String, String> properties = StartupTestHarness.devProperties();
        properties.remove("app.security.demo-user.username");
        properties.remove("app.security.demo-user.password-hash");
        Throwable failure = StartupTestHarness.tryRun(properties, "dev");
        assertThat(failure).as("a dev start without demo-user credentials must fail").isNotNull();
        assertThat(StartupTestHarness.failureMessages(failure)).contains("app.security.demo-user");
    }

    @Test
    void invalidDemoPasswordHashFailsStartupWithoutLeakingHash() {
        Map<String, String> properties = StartupTestHarness.devProperties();
        String invalidHash = "not-a-bcrypt-hash";
        properties.put("app.security.demo-user.password-hash", invalidHash);
        Throwable failure = StartupTestHarness.tryRun(properties, "dev");
        assertThat(failure).isNotNull();
        assertThat(StartupTestHarness.failureMessages(failure))
                .contains("app.security.demo-user")
                .doesNotContain(invalidHash);
    }
}
