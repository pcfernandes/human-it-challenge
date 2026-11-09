package com.humanit.challenge.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * T-M15 — demo-user properties: dev-profile-only binding, externalized values, and a
 * fail-fast when dev starts without them (correctly activated prod never binds them —
 * see {@link ProdProfileStartupTest#devGatedDemoUserBeanIsNotRegistered()}).
 */
class DemoUserPropertiesTest {

    @Test
    void externalizedDemoCredentialsBindInDev() { // T-M15 happy path
        Map<String, String> properties = StartupTestHarness.devProperties();
        try (var context = StartupTestHarness.runApp(properties, "dev")) {
            assertThat(context.getBean(AppDemoUserProperties.class).username())
                    .isEqualTo(StartupTestHarness.DEMO_USERNAME);
            assertThat(context.getBean(AppDemoUserProperties.class).password())
                    .isEqualTo(StartupTestHarness.DEMO_PASSWORD);
        }
    }

    @Test
    void missingDemoCredentialsFailStartupInDev() { // T-M15 failure path (dev requires them)
        Map<String, String> properties = StartupTestHarness.devProperties();
        properties.remove("app.security.demo-user.username");
        properties.remove("app.security.demo-user.password");
        Throwable failure = StartupTestHarness.tryRun(properties, "dev");
        assertThat(failure).as("a dev start without demo-user credentials must fail").isNotNull();
        assertThat(StartupTestHarness.failureMessages(failure)).contains("app.security.demo-user");
    }
}
