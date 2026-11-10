package com.humanit.challenge.dto.document;

import static com.humanit.challenge.dto.DtoValidationSupport.hasViolation;
import static com.humanit.challenge.dto.DtoValidationSupport.hasViolationOn;
import static com.humanit.challenge.dto.DtoValidationSupport.validate;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.humanit.challenge.dto.patch.PatchRequired;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** Bean Validation contract for {@code DocumentPatchRequest}, validated after real Jackson binding. */
class DocumentPatchRequestValidationTest {

    private static final String VERSION = "\"version\": 0";

    private final JsonMapper mapper = JsonMapper.builder().findAndAddModules().build();

    private DocumentPatchRequest bind(String json) throws Exception {
        return mapper.readValue("{" + json + "}", DocumentPatchRequest.class);
    }

    @Nested
    class VersionField {

        @Test
        void onlyVersionIsAValidNoOpPatch() throws Exception {
            assertThat(validate(bind(VERSION))).isEmpty();
        }

        @Test
        void missingVersionIsRejected() throws Exception {
            assertThat(hasViolation(validate(bind("\"number\": \"DOC-1\"")), "version", NotNull.class)).isTrue();
        }

        @Test
        void negativeVersionIsRejected() throws Exception {
            assertThat(hasViolation(validate(bind("\"version\": -1")), "version", PositiveOrZero.class)).isTrue();
        }

        @Test
        void versionZeroIsAccepted() throws Exception {
            assertThat(validate(bind(VERSION))).isEmpty();
        }
    }

    @Nested
    class RequiredScalars {

        @ParameterizedTest
        @ValueSource(strings = {"number", "expirationDate"})
        void explicitNullIsRejectedForRequiredScalars(String property) throws Exception {
            var request = bind(VERSION + ", \"" + property + "\": null");

            assertThat(hasViolation(validate(request), property, PatchRequired.class)).isTrue();
        }

        @Test
        void omittedRequiredScalarsAreNotViolations() throws Exception {
            assertThat(validate(bind(VERSION))).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   "})
        void blankNumberIsRejected(String number) throws Exception {
            var request = bind(VERSION + ", \"number\": " + mapper.writeValueAsString(number));

            assertThat(hasViolation(validate(request), "number", NotBlank.class)).isTrue();
        }

        @Test
        void numberAcceptsExactly100AndRejects101() throws Exception {
            assertThat(validate(bind(VERSION + ", \"number\": \"" + "n".repeat(100) + "\""))).isEmpty();
            assertThat(hasViolation(validate(bind(VERSION + ", \"number\": \"" + "n".repeat(101) + "\"")),
                    "number", Size.class)).isTrue();
        }

        @Test
        void expiredExpirationDateIsAccepted() throws Exception {
            assertThat(validate(bind(VERSION + ", \"expirationDate\": \"2001-01-01\""))).isEmpty();
        }
    }

    @Nested
    class OptionalDescription {

        @Test
        void omittedDescriptionIsNotAViolation() throws Exception {
            assertThat(validate(bind(VERSION + ", \"number\": \"DOC-1\""))).isEmpty();
        }

        @Test
        void explicitNullDescriptionIsAcceptedForClearing() throws Exception {
            var request = bind(VERSION + ", \"description\": null");

            assertThat(request.getDescription().isPresentNull()).isTrue();
            assertThat(validate(request)).isEmpty();
        }

        @Test
        void suppliedDescriptionIsAccepted() throws Exception {
            assertThat(validate(bind(VERSION + ", \"description\": \"Passport copy\""))).isEmpty();
        }

        @Test
        void descriptionAcceptsExactly1000AndRejects1001() throws Exception {
            assertThat(validate(bind(VERSION + ", \"description\": \"" + "d".repeat(1000) + "\""))).isEmpty();
            assertThat(hasViolation(validate(bind(VERSION + ", \"description\": \"" + "d".repeat(1001) + "\"")),
                    "description", Size.class)).isTrue();
        }

        @Test
        void blankDescriptionIsAcceptedBecauseItIsOptional() throws Exception {
            var request = bind(VERSION + ", \"description\": \"   \"");

            assertThat(request.getDescription().isPresentValue()).isTrue();
            assertThat(validate(request)).isEmpty();
            assertThat(hasViolationOn(validate(request), "description")).isFalse();
        }
    }
}
