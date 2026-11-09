package com.humanit.challenge.config;

import java.util.Base64;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Application-code validation of the externalized JWT secret (T1.3, Revision 4).
 *
 * <p>Validation is intentionally performed here, rather than via Bean Validation on
 * {@link AppJwtProperties#secret()}, so that rejected secret values never reach
 * startup diagnostics (no {@code rejected value [...]} in the bind-error path).</p>
 */
@Component
public class JwtSecretValidator {

    private final AppJwtProperties properties;

    public JwtSecretValidator(AppJwtProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void validate() {
        String secret = properties.secret();
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("app.security.jwt.secret must not be blank");
        }
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("app.security.jwt.secret must be valid Base64");
        }
        if (decoded.length < 32) {
            throw new IllegalStateException(
                    "app.security.jwt.secret decoded to " + decoded.length + " bytes; minimum 32 required");
        }
    }
}
