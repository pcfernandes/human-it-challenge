package com.humanit.challenge.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * T-M10…T-M14 — invalid pagination configuration and JWT expiration overrides fail
 * startup loudly at bind time. The ceiling of 100 is code-owned; raising it is
 * impossible without a code change (approved Revision 3, Appendix E). HTTP pagination
 * behavior remains T2.8's responsibility.
 */
class PaginationPropertiesValidationTest {

    @Test
    void maxPageSizeAboveHundredFailsStartup() { // T-M10
        Map<String, String> properties = StartupTestHarness.devProperties();
        properties.put("app.pagination.max-page-size", "150");
        Throwable failure = StartupTestHarness.tryRun(properties, "dev");
        assertThat(failure).as("startups with maxPageSize > 100 must fail").isNotNull();
        assertThat(StartupTestHarness.failureMessages(failure)).contains("maxPageSize")
                .contains("100");
    }

    @Test
    void defaultPageSizeOfZeroFailsStartup() { // T-M11
        Map<String, String> properties = StartupTestHarness.devProperties();
        properties.put("app.pagination.default-page-size", "0");
        Throwable failure = StartupTestHarness.tryRun(properties, "dev");
        assertThat(failure).as("defaultPageSize must be >= 1").isNotNull();
        assertThat(StartupTestHarness.failureMessages(failure)).contains("defaultPageSize")
                .contains("greater than or equal to 1");
    }

    @Test
    void negativeDefaultPageFailsStartup() { // T-M12
        Map<String, String> properties = StartupTestHarness.devProperties();
        properties.put("app.pagination.default-page", "-1");
        Throwable failure = StartupTestHarness.tryRun(properties, "dev");
        assertThat(failure).as("defaultPage must be >= 0").isNotNull();
        assertThat(StartupTestHarness.failureMessages(failure)).contains("defaultPage")
                .contains("greater than or equal to 0");
    }

    @Test
    void defaultPageSizeAboveMaxPageSizeFailsStartup() { // T-M13 cross-field invariant
        Map<String, String> properties = StartupTestHarness.devProperties();
        properties.put("app.pagination.max-page-size", "10");
        Throwable failure = StartupTestHarness.tryRun(properties, "dev");
        assertThat(failure).as("defaultPageSize (20) > maxPageSize (10) must fail").isNotNull();
        assertThat(StartupTestHarness.failureMessages(failure))
                .contains("default-page-size must not exceed max-page-size");
    }

    @Test
    void expirationMinutesOfZeroFailsStartup() { // T-M14
        Map<String, String> properties = StartupTestHarness.devProperties();
        properties.put("app.security.jwt.expiration-minutes", "0");
        Throwable failure = StartupTestHarness.tryRun(properties, "dev");
        assertThat(failure).as("expirationMinutes must be >= 1").isNotNull();
        assertThat(StartupTestHarness.failureMessages(failure)).contains("expirationMinutes")
                .contains("greater than or equal to 1");
    }
}
