package com.humanit.challenge.dto.document;

import static com.humanit.challenge.dto.DtoValidationSupport.hasViolation;
import static com.humanit.challenge.dto.DtoValidationSupport.validate;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

class DocumentRequestValidationTest {

    private static final LocalDate FUTURE = LocalDate.of(2099, 1, 1);
    private static final LocalDate EXPIRED = LocalDate.of(2001, 1, 1);

    @Nested
    class CreateRequest {

        @Test
        void validCreateRequestHasNoViolations() {
            var request = new DocumentCreateRequest("DOC-001", "Passport", FUTURE);

            assertThat(validate(request)).isEmpty();
        }

        @Test
        void nullDescriptionIsAcceptedAsOptional() {
            var request = new DocumentCreateRequest("DOC-001", null, FUTURE);

            assertThat(validate(request)).isEmpty();
        }

        @Test
        void expiredDocumentIsAccepted() {
            var request = new DocumentCreateRequest("DOC-EXPIRED", null, EXPIRED);

            assertThat(validate(request)).isEmpty();
        }

        @Test
        void missingExpirationDateIsRejected() {
            var request = new DocumentCreateRequest("DOC-001", null, null);

            assertThat(hasViolation(validate(request), "expirationDate", NotNull.class)).isTrue();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        void blankOrMissingNumberIsRejected(String number) {
            var request = new DocumentCreateRequest(number, null, FUTURE);

            assertThat(hasViolation(validate(request), "number", NotBlank.class)).isTrue();
        }

        @Test
        void numberAtMaximumLengthIsAccepted() {
            var request = new DocumentCreateRequest("n".repeat(100), null, FUTURE);

            assertThat(validate(request)).isEmpty();
        }

        @Test
        void numberAboveMaximumLengthIsRejected() {
            var request = new DocumentCreateRequest("n".repeat(101), null, FUTURE);

            assertThat(hasViolation(validate(request), "number", Size.class)).isTrue();
        }

        @Test
        void descriptionAtMaximumLengthIsAccepted() {
            var request = new DocumentCreateRequest("DOC-001", "d".repeat(1000), FUTURE);

            assertThat(validate(request)).isEmpty();
        }

        @Test
        void descriptionAboveMaximumLengthIsRejected() {
            var request = new DocumentCreateRequest("DOC-001", "d".repeat(1001), FUTURE);

            assertThat(hasViolation(validate(request), "description", Size.class)).isTrue();
        }
    }

    @Nested
    class PutRequest {

        @Test
        void validPutRequestHasNoViolations() {
            var request = new DocumentPutRequest(3L, "DOC-001", "Passport", FUTURE);

            assertThat(validate(request)).isEmpty();
        }

        @Test
        void versionZeroIsAccepted() {
            var request = new DocumentPutRequest(0L, "DOC-001", null, FUTURE);

            assertThat(validate(request)).isEmpty();
        }

        @Test
        void negativeVersionIsRejected() {
            var request = new DocumentPutRequest(-1L, "DOC-001", null, FUTURE);

            assertThat(hasViolation(validate(request), "version", PositiveOrZero.class)).isTrue();
        }

        @Test
        void missingVersionIsRejected() {
            var request = new DocumentPutRequest(null, "DOC-001", null, FUTURE);

            assertThat(hasViolation(validate(request), "version", NotNull.class)).isTrue();
        }

        @Test
        void blankNumberIsRejected() {
            var request = new DocumentPutRequest(1L, " ", null, FUTURE);

            assertThat(hasViolation(validate(request), "number", NotBlank.class)).isTrue();
        }

        @Test
        void missingExpirationDateIsRejected() {
            var request = new DocumentPutRequest(1L, "DOC-001", null, null);

            assertThat(hasViolation(validate(request), "expirationDate", NotNull.class)).isTrue();
        }

        @Test
        void expiredDocumentIsAccepted() {
            var request = new DocumentPutRequest(1L, "DOC-EXPIRED", null, EXPIRED);

            assertThat(validate(request)).isEmpty();
        }

        @Test
        void overlongNumberAndDescriptionAreRejected() {
            var request = new DocumentPutRequest(1L, "n".repeat(101), "d".repeat(1001), FUTURE);
            var violations = validate(request);

            assertThat(hasViolation(violations, "number", Size.class)).isTrue();
            assertThat(hasViolation(violations, "description", Size.class)).isTrue();
        }
    }
}
