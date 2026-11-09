package com.humanit.challenge.config.validation;

import java.util.Base64;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Decodes standard Base64 and enforces a minimum <em>decoded byte</em> length.
 *
 * <p>Secret values are never logged or echoed: messages carry byte counts only,
 * because the validated text must never appear in any message.</p>
 */
public class Base64DecodedMinBytesValidator implements ConstraintValidator<Base64DecodedMinBytes, String> {

    private int minBytes;

    @Override
    public void initialize(Base64DecodedMinBytes annotation) {
        this.minBytes = annotation.value();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            // Let @NotBlank classify absence/blank; this stays independently invalid.
            return false;
        }
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(value);
        } catch (IllegalArgumentException ex) {
            return fail(context, "must be valid Base64");
        }
        if (decoded.length < minBytes) {
            return fail(context, "decoded to " + decoded.length + " bytes; minimum " + minBytes + " required");
        }
        return true;
    }

    private boolean fail(ConstraintValidatorContext context, String message) {
        if (context != null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
        }
        return false;
    }
}
