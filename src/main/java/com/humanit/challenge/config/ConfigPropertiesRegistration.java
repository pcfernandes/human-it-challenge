package com.humanit.challenge.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Registers the environment-neutral configuration records (pagination, JWT).
 *
 * <p>The dev-only demo-user record is registered separately by
 * {@link DevConfigPropertiesRegistration} under a {@code @Profile("dev & !prod")} guard,
 * so production contexts never bind demo-user properties.</p>
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({AppPaginationProperties.class, AppJwtProperties.class})
public class ConfigPropertiesRegistration {
}
