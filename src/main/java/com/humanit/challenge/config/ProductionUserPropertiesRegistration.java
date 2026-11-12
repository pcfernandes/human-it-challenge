package com.humanit.challenge.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/** Registers production account properties only when the production profile is active. */
@Configuration(proxyBeanMethods = false)
@Profile("prod")
@EnableConfigurationProperties(AppProductionUserProperties.class)
public class ProductionUserPropertiesRegistration {
}
