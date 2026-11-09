package com.humanit.challenge.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Development-only demo-user credentials, externalized via the environment
 * ({@code APP_SECURITY_DEMO_USER_USERNAME} / {@code APP_SECURITY_DEMO_USER_PASSWORD}).
 *
 * <p>This record is registered <strong>only</strong> by
 * {@link DevConfigPropertiesRegistration} which is itself {@code @Profile("dev")}-gated:
 * in a correctly activated production profile the bean does not exist, independently of
 * which environment variables are present. No demo user is provisioned by T1.3 — actual
 * provisioning is Phase 3 (T3.2) and is also dev-gated.</p>
 */
@Validated
@ConfigurationProperties(prefix = "app.security.demo-user")
public record AppDemoUserProperties(
        @NotBlank String username,
        @NotBlank String password) {
}
