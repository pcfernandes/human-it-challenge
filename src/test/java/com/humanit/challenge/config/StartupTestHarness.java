package com.humanit.challenge.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.DynamicPropertyRegistry;

import com.humanit.challenge.HumanItChallengeApplication;

/**
 * Test-only harness for full application-startup runs with deterministic dummy
 * properties. All secrets here are fixed dummy values ({@code "A"}-runs), never
 * real credentials.
 *
 * <p>Runs the real {@link HumanItChallengeApplication} non-web (no embedded server);
 * external values are supplied the same way production does it (in-memory
 * "environment" properties), never via committed YAML.</p>
 */
public final class StartupTestHarness {

    /** 43 Base64 chars = exactly 32 decoded bytes (minimal valid secret). */
    public static final String VALID_SECRET_32_BYTES = "A".repeat(43);

    /** 32 Base64 chars = only 24 decoded bytes (below the floor; chars &ne; bytes). */
    public static final String SHORT_SECRET_24_BYTES = "A".repeat(32);

    public static final String MALFORMED_SECRET = "not!valid";

    public static final String DEMO_USERNAME = "test-demo-username";
    public static final String DEMO_PASSWORD = "test-demo-password";

    private StartupTestHarness() {
    }

    /** Registers valid dev-required externals (secret + demo-user credentials) for @SpringBootTest contexts. */
    public static void supplyDevExternals(DynamicPropertyRegistry registry) {
        registry.add("app.security.jwt.secret", () -> VALID_SECRET_32_BYTES);
        registry.add("app.security.demo-user.username", () -> DEMO_USERNAME);
        registry.add("app.security.demo-user.password", () -> DEMO_PASSWORD);
    }

    /** Registers the prod-required externals (secret only) for @SpringBootTest contexts. */
    public static void supplyProdExternals(DynamicPropertyRegistry registry) {
        registry.add("app.security.jwt.secret", () -> VALID_SECRET_32_BYTES);
    }

    /** Dev-start environment: valid 32-byte secret + demo-user credentials (mutable map). */
    public static Map<String, String> devProperties() {
        return propertyMap(VALID_SECRET_32_BYTES);
    }

    /** Dev-start environment with an overridden secret value (mutable map). */
    public static Map<String, String> devProperties(String secretOverride) {
        return propertyMap(secretOverride);
    }

    /** Prod-start environment: only the secret is provided (demo-user keys are dev-only). */
    public static Map<String, String> prodProperties() {
        return propertyMap(VALID_SECRET_32_BYTES);
    }

    /** Runs the application and returns the running context (caller must close it). */
    public static ConfigurableApplicationContext runApp(Map<String, String> properties, String... profiles) {
        return builder(properties, profiles).run();
    }

    /**
     * Runs the application and returns the thrown failure, or {@code null} if the
     * startup succeeded (context is closed in that case). Used to assert fail-fast paths.
     */
    public static Throwable tryRun(Map<String, String> properties, String... profiles) {
        try {
            ConfigurableApplicationContext context = builder(properties, profiles).run();
            context.close();
            return null;
        } catch (Throwable failure) {
            return failure;
        }
    }

    /** Joins the whole causal chain's messages; used for safe "contains" assertions. */
    public static String failureMessages(Throwable failure) {
        StringBuilder sb = new StringBuilder();
        for (Throwable t = failure; t != null; t = t.getCause()) {
            sb.append(t.getClass().getName()).append(": ").append(t.getMessage()).append(" | ");
        }
        return sb.toString();
    }

    private static Map<String, String> propertyMap(String secret) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("app.security.jwt.secret", secret);
        map.put("app.security.demo-user.username", DEMO_USERNAME);
        map.put("app.security.demo-user.password", DEMO_PASSWORD);
        return map;
    }

    private static SpringApplicationBuilder builder(Map<String, String> properties, String... profiles) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(HumanItChallengeApplication.class)
                .web(WebApplicationType.NONE)
                .bannerMode(Banner.Mode.OFF);
        if (profiles.length > 0) {
            builder.profiles(profiles);
        }
        if (properties != null && !properties.isEmpty()) {
            // Apply overrides through a high-precedence property source so that values
            // committed in application.yml (e.g. max-page-size: 100) cannot shadow
            // the invalid overrides under test.
            builder.initializers(context -> TestPropertyValues
                    .of(properties.entrySet().stream()
                            .map(entry -> entry.getKey() + "=" + entry.getValue())
                            .toArray(String[]::new))
                    .applyTo(context.getEnvironment()));
        }
        return builder;
    }
}
