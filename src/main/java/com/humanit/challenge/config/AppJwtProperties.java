package com.humanit.challenge.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * JWT configuration. The secret is <strong>externalized</strong>: it is supplied by the
 * environment ({@code APP_SECURITY_JWT_SECRET}, relaxed-bound onto {@code app.security.jwt.secret})
 * in both profiles and is never committed to source control.
 *
 * <p>The secret must be <strong>standard Base64 (RFC 4648)</strong> and must decode to at least
 * 32 bytes (256 bits), the HMAC-SHA-256 key floor of RFC 7518. Missing, blank, malformed or
 * too-short values fail application startup. Validation is performed by
 * {@link JwtSecretValidator} (application code, not Bean Validation) so that rejected
 * secret values never reach startup diagnostics. Failure messages never contain the
 * secret value.</p>
 *
 * <p>T1.3 delivers configuration only: no JWT encoder, decoder, filter or algorithm choice
 * belongs to this task (Phase 3 handles those). {@code expiration-minutes} defaults to 60
 * (approved decision O-02).</p>
 */
@Validated
@ConfigurationProperties(prefix = "app.security.jwt")
public record AppJwtProperties(
        String secret,
        @NotNull @Min(1) Integer expirationMinutes) {
}
