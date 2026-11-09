package com.humanit.challenge.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Registers the development-only demo-user properties under a strict
 * {@code @Profile("dev & !prod")} guard: in a correctly activated production profile,
 * or under combined {@code dev,prod}/{@code prod,dev} activation, this configuration
 * does not load and no {@link AppDemoUserProperties} bean exists, independently of
 * which environment variables are set. Actual demo-user provisioning is Phase 3 (T3.2)
 * and must use the same guard.
 */
@Configuration(proxyBeanMethods = false)
@Profile("dev & !prod")
@EnableConfigurationProperties(AppDemoUserProperties.class)
public class DevConfigPropertiesRegistration {
}
