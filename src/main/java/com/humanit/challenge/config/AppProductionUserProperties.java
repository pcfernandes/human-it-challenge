package com.humanit.challenge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Production account credentials supplied by the deployment environment or secrets manager. */
@ConfigurationProperties(prefix = "app.security.user")
public record AppProductionUserProperties(
        String username,
        String passwordHash) {
}
