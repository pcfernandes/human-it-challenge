package com.humanit.challenge.dto.client;

import static com.humanit.challenge.dto.DtoValidationSupport.hasViolation;
import static com.humanit.challenge.dto.DtoValidationSupport.hasViolationOn;
import static com.humanit.challenge.dto.DtoValidationSupport.validate;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.humanit.challenge.dto.document.DocumentCreateRequest;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

class ClientRequestValidationTest {

    private ClientCreateRequest request;

    @BeforeEach
    void validRequest() {
        request = new ClientCreateRequest();
        request.setFirstName("Ana");
        request.setLastName("Silva");
        request.setTaxIdentifier("TAX-123");
        request.setEmail("ana.silva@example.com");
        request.setPhoneNumber("+351 912 345 678");
    }

    @Nested
    class RequiredAndBlankFields {

        @Test
        void validCreateRequestWithoutDocumentsHasNoViolations() {
            assertThat(validate(request)).isEmpty();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        void blankOrMissingFirstNameIsRejected(String value) {
            request.setFirstName(value);

            assertThat(hasViolation(validate(request), "firstName", NotBlank.class)).isTrue();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" "})
        void blankOrMissingLastNameIsRejected(String value) {
            request.setLastName(value);

            assertThat(hasViolation(validate(request), "lastName", NotBlank.class)).isTrue();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" "})
        void blankOrMissingTaxIdentifierIsRejected(String value) {
            request.setTaxIdentifier(value);

            assertThat(hasViolation(validate(request), "taxIdentifier", NotBlank.class)).isTrue();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" "})
        void blankOrMissingPhoneNumberIsRejected(String value) {
            request.setPhoneNumber(value);

            assertThat(hasViolation(validate(request), "phoneNumber", NotBlank.class)).isTrue();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" "})
        void blankOrMissingEmailIsRejected(String value) {
            request.setEmail(value);

            assertThat(hasViolation(validate(request), "email", NotBlank.class)).isTrue();
        }
    }

    @Nested
    class EmailFormat {

        @ParameterizedTest
        @ValueSource(strings = {"not-an-email", "ana@", "@example.com", "ana example.com"})
        void malformedEmailIsRejected(String email) {
            request.setEmail(email);

            assertThat(hasViolation(validate(request), "email", Email.class)).isTrue();
        }

        @Test
        void emailAtMaximumLengthIsNotRejectedForSize() {
            // 243-character local part + "@example.com" (12 characters) = 255 characters.
            request.setEmail("a".repeat(243) + "@example.com");

            assertThat(hasViolation(validate(request), "email", Size.class)).isFalse();
        }

        @Test
        void emailAboveMaximumLengthIsRejectedForSize() {
            request.setEmail("a".repeat(244) + "@example.com");

            assertThat(hasViolation(validate(request), "email", Size.class)).isTrue();
        }
    }

    @Nested
    class MaximumLengths {

        @Test
        void firstNameAtMaximumIsAcceptedAndAboveIsRejected() {
            request.setFirstName("f".repeat(100));
            assertThat(validate(request)).isEmpty();

            request.setFirstName("f".repeat(101));
            assertThat(hasViolation(validate(request), "firstName", Size.class)).isTrue();
        }

        @Test
        void lastNameAtMaximumIsAcceptedAndAboveIsRejected() {
            request.setLastName("l".repeat(100));
            assertThat(validate(request)).isEmpty();

            request.setLastName("l".repeat(101));
            assertThat(hasViolation(validate(request), "lastName", Size.class)).isTrue();
        }

        @Test
        void taxIdentifierAtMaximumIsAcceptedAndAboveIsRejected() {
            request.setTaxIdentifier("t".repeat(50));
            assertThat(validate(request)).isEmpty();

            request.setTaxIdentifier("t".repeat(51));
            assertThat(hasViolation(validate(request), "taxIdentifier", Size.class)).isTrue();
        }

        @Test
        void phoneNumberAtMaximumIsAcceptedAndAboveIsRejected() {
            request.setPhoneNumber("1".repeat(50));
            assertThat(validate(request)).isEmpty();

            request.setPhoneNumber("1".repeat(51));
            assertThat(hasViolation(validate(request), "phoneNumber", Size.class)).isTrue();
        }
    }

    @Nested
    class NoCountrySpecificFormats {

        @Test
        void arbitraryTaxIdentifierAndPhoneFormatsAreAccepted() {
            request.setTaxIdentifier("ABC-123/xyz");
            request.setPhoneNumber("not a phone number");

            assertThat(validate(request)).isEmpty();
        }
    }

    @Nested
    class CreateDocuments {

        @Test
        void nullDocumentsIsTreatedAsOmittedOnPost() {
            request.setDocuments(null);

            assertThat(request.getDocuments()).isEmpty();
            assertThat(validate(request)).isEmpty();
        }

        @Test
        void emptyDocumentsIsAccepted() {
            request.setDocuments(List.of());

            assertThat(validate(request)).isEmpty();
        }

        @Test
        void validSuppliedDocumentsAreAccepted() {
            request.setDocuments(List.of(
                    new DocumentCreateRequest("DOC-1", null, LocalDate.of(2099, 1, 1)),
                    new DocumentCreateRequest("DOC-2", "Visa", LocalDate.of(2001, 1, 1))));

            assertThat(validate(request)).isEmpty();
        }

        @Test
        void invalidSuppliedDocumentIsValidatedThroughCascade() {
            request.setDocuments(List.of(new DocumentCreateRequest(" ", null, null)));
            var violations = validate(request);

            assertThat(hasViolation(violations, "documents[0].number", NotBlank.class)).isTrue();
            assertThat(hasViolation(violations, "documents[0].expirationDate", NotNull.class)).isTrue();
        }

        @Test
        void nullElementInsideDocumentsIsRejected() {
            request.setDocuments(java.util.Arrays.asList((DocumentCreateRequest) null));

            assertThat(hasViolationOn(validate(request), "documents[0].<list element>")).isTrue();
        }
    }
}
