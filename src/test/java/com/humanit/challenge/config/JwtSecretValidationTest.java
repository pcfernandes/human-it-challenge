package com.humanit.challenge.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * T-M3…T-M7, T-M17 — the JWT secret is required in both profiles and must be
 * standard Base64 decoding to at least 32 bytes (approved decision OD-03; property-level
 * fail-fast, the only fail-fast retained by Revision 3). Failure messages never contain
 * the secret value.
 */
class JwtSecretValidationTest {

    private static final String PROPERTY_PREFIX = "app.security.jwt";

    @Test
    void noSecretFailsStartupInDev() { // T-M3
        Map<String, String> properties = StartupTestHarness.devProperties();
        properties.remove("app.security.jwt.secret");
        Throwable failure = StartupTestHarness.tryRun(properties, "dev");
        assertThat(failure).as("startup must fail without a secret").isNotNull();
        assertThat(StartupTestHarness.failureMessages(failure)).contains(PROPERTY_PREFIX);
    }

    @Test
    void prodWithoutSecretFailsStartup() { // T-M17
        Map<String, String> properties = StartupTestHarness.prodProperties();
        properties.remove("app.security.jwt.secret");
        Throwable failure = StartupTestHarness.tryRun(properties, "prod");
        assertThat(failure).as("startup must fail in prod without a secret").isNotNull();
        assertThat(StartupTestHarness.failureMessages(failure)).contains(PROPERTY_PREFIX);
    }

    @Test
    void blankSecretFailsStartup() { // T-M4
        Map<String, String> properties = StartupTestHarness.devProperties("   ");
        Throwable failure = StartupTestHarness.tryRun(properties, "dev");
        assertThat(failure).as("startup must fail with a blank secret").isNotNull();
        assertThat(StartupTestHarness.failureMessages(failure)).contains(PROPERTY_PREFIX);
    }

    @Test
    void malformedBase64SecretFailsStartupWithoutValueLeak() { // T-M5
        Map<String, String> properties = StartupTestHarness.devProperties(StartupTestHarness.MALFORMED_SECRET);
        Throwable failure = StartupTestHarness.tryRun(properties, "dev");
        assertThat(failure).as("startup must fail for non-Base64 input").isNotNull();
        String messages = StartupTestHarness.failureMessages(failure);
        assertThat(messages).contains("Base64");
        // Secret values are never echoed by validation.
        assertThat(messages).doesNotContain(StartupTestHarness.MALFORMED_SECRET);
    }

    @Test
    void secretShorterThanThirtyTwoDecodedBytesFailsWithByteCountAndNoValueLeak() { // T-M6
        Map<String, String> properties = StartupTestHarness.devProperties(StartupTestHarness.SHORT_SECRET_24_BYTES);
        Throwable failure = StartupTestHarness.tryRun(properties, "dev");
        assertThat(failure).as("startup must fail below the 32-byte floor").isNotNull();
        String messages = StartupTestHarness.failureMessages(failure);
        // Message reports decoded length only (24 bytes), never the content.
        assertThat(messages).contains("decoded to 24 bytes; minimum 32 required");
        assertThat(messages).doesNotContain(StartupTestHarness.SHORT_SECRET_24_BYTES);
    }

    @Test
    void exactlyThirtyTwoDecodedBytesIsTheAcceptedBoundary() { // T-M7
        // 43 Base64 chars = exactly 32 decoded bytes (boundary); 32 chars = only 24 bytes.
        Map<String, String> properties = StartupTestHarness.devProperties(StartupTestHarness.VALID_SECRET_32_BYTES);
        try (var context = StartupTestHarness.runApp(properties, "dev")) {
            assertThat(context).isNotNull();
            var jwt = context.getBean(AppJwtProperties.class);
            assertThat(jwt.secret()).isEqualTo(StartupTestHarness.VALID_SECRET_32_BYTES);
            assertThat(jwt.expirationMinutes()).isEqualTo(60);
        }
    }
}
