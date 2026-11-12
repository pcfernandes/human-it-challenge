package com.humanit.challenge.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

class ProductionUserProvisioningTest {

    @Test
    void missingProductionUsernameFailsStartupWithoutLeakingCredentialValues() {
        Map<String, String> properties = StartupTestHarness.prodProperties();
        properties.remove("app.security.user.username");

        Throwable failure = StartupTestHarness.tryRun(properties, "prod");

        assertThat(failure).isNotNull();
        assertThat(StartupTestHarness.failureMessages(failure))
                .contains("app.security.user")
                .doesNotContain(StartupTestHarness.TEST_BCRYPT_HASH, StartupTestHarness.TEST_PASSWORD_INPUT);
    }

    @Test
    void missingProductionHashFailsStartupWithoutLeakingCredentialValues() {
        Map<String, String> properties = StartupTestHarness.prodProperties();
        properties.remove("app.security.user.password-hash");

        Throwable failure = StartupTestHarness.tryRun(properties, "prod");

        assertThat(failure).isNotNull();
        assertThat(StartupTestHarness.failureMessages(failure))
                .contains("app.security.user")
                .doesNotContain(StartupTestHarness.TEST_BCRYPT_HASH, StartupTestHarness.TEST_PASSWORD_INPUT);
    }

    @Test
    void invalidProductionHashFailsStartupWithoutLeakingHash() {
        Map<String, String> properties = StartupTestHarness.prodProperties();
        String invalidHash = "invalid-bcrypt-value";
        properties.put("app.security.user.password-hash", invalidHash);

        Throwable failure = StartupTestHarness.tryRun(properties, "prod");

        assertThat(failure).isNotNull();
        assertThat(StartupTestHarness.failureMessages(failure))
                .contains("app.security.user")
                .doesNotContain(invalidHash, StartupTestHarness.TEST_BCRYPT_HASH);
    }
}
