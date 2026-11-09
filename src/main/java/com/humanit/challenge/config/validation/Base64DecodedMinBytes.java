package com.humanit.challenge.config.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Constraint requiring the annotated {@link String} to be standard Base64 (RFC 4648,
 * strict decoder, no MIME tolerance and no whitespace) decoding to at least
 * {@link #value()} bytes (default 32, the HMAC-SHA-256 key floor of RFC 7518).
 *
 * <p>The validator decodes the value and counts the resulting <em>bytes</em>; it never
 * inspects {@code String.length()}, so a 32-character Base64 string (24 decoded bytes)
 * is rejected. Failure messages contain byte counts only — never the secret value.</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER,
        ElementType.ANNOTATION_TYPE, ElementType.RECORD_COMPONENT, ElementType.TYPE_USE})
@Constraint(validatedBy = Base64DecodedMinBytesValidator.class)
public @interface Base64DecodedMinBytes {

    /** Minimum decoded byte count (default 32 = 256 bits). */
    int value() default 32;

    String message() default "must be valid Base64 decoding to at least {value} bytes";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
