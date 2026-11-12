package com.humanit.challenge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Development-only demo-user credentials, externalized via the environment
 * ({@code APP_SECURITY_DEMO_USER_USERNAME} / {@code APP_SECURITY_DEMO_USER_PASSWORD_HASH}).
 *
 * <p>This record is registered <strong>only</strong> by
 * {@link DevConfigPropertiesRegistration} which is itself {@code @Profile("dev & !prod")}-gated:
 * in a correctly activated production profile the bean does not exist, independently of
 * which environment variables are present. No demo user is provisioned by T1.3 — actual
 * provisioning is Phase 3 (T3.2) and is also dev-gated.</p>
 */
@ConfigurationProperties(prefix = "app.security.demo-user")
public record AppDemoUserProperties(
        String username,
        String passwordHash) {
}
