package com.humanit.challenge.config.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * T-M9 — chars &ne; bytes demonstration for {@link Base64DecodedMinBytesValidator}:
 * a 32-character Base64 string decodes to only 24 bytes and is rejected; the minimal
 * valid form is 43 chars encoding exactly 32 bytes; a 64-byte secret (88 chars with
 * padding) is valid.
 */
class Base64DecodedMinBytesValidatorTest {

    private static final Validator VALIDATOR;

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        VALIDATOR = factory.getValidator();
    }

    static class ThirtyTwoByteBoundHolder {
        @Base64DecodedMinBytes(32)
        final String value;

        ThirtyTwoByteBoundHolder(String value) {
            this.value = value;
        }
    }

    @Test
    void thirtyTwoCharactersDecodeToTwentyFourBytesAndAreRejected() {
        Set<ConstraintViolation<ThirtyTwoByteBoundHolder>> violations =
                VALIDATOR.validate(new ThirtyTwoByteBoundHolder("A".repeat(32)));
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("decoded to 24 bytes; minimum 32 required");
    }

    @Test
    void fortyThreeCharactersDecodeToExactlyThirtyTwoBytesAndAreAccepted() {
        Set<ConstraintViolation<ThirtyTwoByteBoundHolder>> violations =
                VALIDATOR.validate(new ThirtyTwoByteBoundHolder("A".repeat(43)));
        assertThat(violations).isEmpty();
    }

    @Test
    void sixtyFourByteSecretIsAccepted() {
        String base64Of64Bytes = java.util.Base64.getEncoder().encodeToString(new byte[64]);
        Set<ConstraintViolation<ThirtyTwoByteBoundHolder>> violations =
                VALIDATOR.validate(new ThirtyTwoByteBoundHolder(base64Of64Bytes));
        assertThat(violations).isEmpty();
    }

    @Test
    void malformedInputOutsideTheBase64AlphabetIsRejected() {
        Set<ConstraintViolation<ThirtyTwoByteBoundHolder>> violations =
                VALIDATOR.validate(new ThirtyTwoByteBoundHolder("not!valid"));
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage()).isEqualTo("must be valid Base64");
    }

    @Test
    void blankInputIsRejectedByTheConstraintItself() {
        Set<ConstraintViolation<ThirtyTwoByteBoundHolder>> violations =
                VALIDATOR.validate(new ThirtyTwoByteBoundHolder(""));
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("must be valid Base64 decoding to at least 32 bytes");
    }

    @Test
    void twoByteDanglingInputIsRejectedWithByteCount() {
        // "ABC" is valid Base64 (unpadded final quantum) decoding to exactly 2 bytes.
        Set<ConstraintViolation<ThirtyTwoByteBoundHolder>> violations =
                VALIDATOR.validate(new ThirtyTwoByteBoundHolder("ABC"));
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("decoded to 2 bytes; minimum 32 required");
    }
}
