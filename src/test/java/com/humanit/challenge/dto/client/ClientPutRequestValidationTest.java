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

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

class ClientPutRequestValidationTest {

    private static final LocalDate FUTURE = LocalDate.of(2099, 1, 1);

    private ClientPutRequest request;

    @BeforeEach
    void validRequest() {
        request = new ClientPutRequest();
        request.setFirstName("Ana");
        request.setLastName("Silva");
        request.setTaxIdentifier("TAX-123");
        request.setEmail("ana.silva@example.com");
        request.setPhoneNumber("+351 912 345 678");
        request.setVersion(0L);
    }

    private static ClientDocumentPutRequest existing(long id, long version, String number) {
        var document = new ClientDocumentPutRequest();
        document.setId(id);
        document.setVersion(version);
        document.setNumber(number);
        document.setExpirationDate(FUTURE);
        return document;
    }

    private static ClientDocumentPutRequest fresh(String number) {
        var document = new ClientDocumentPutRequest();
        document.setNumber(number);
        document.setExpirationDate(FUTURE);
        return document;
    }

    @Nested
    class ScalarAndVersionFields {

        @Test
        void validPutWithoutDocumentsFieldHasNoViolations() {
            assertThat(validate(request)).isEmpty();
        }

        @Test
        void missingVersionIsRejected() {
            request.setVersion(null);

            assertThat(hasViolation(validate(request), "version", NotNull.class)).isTrue();
        }

        @Test
        void negativeVersionIsRejected() {
            request.setVersion(-1L);

            assertThat(hasViolation(validate(request), "version", PositiveOrZero.class)).isTrue();
        }

        @Test
        void blankRequiredFieldIsRejected() {
            request.setLastName("   ");

            assertThat(hasViolation(validate(request), "lastName", NotBlank.class)).isTrue();
        }
    }

    @Nested
    class DocumentsCollection {

        @Test
        void omittedDocumentsPreservesExistingDocumentsAndIsValid() {
            // A freshly constructed request keeps the empty default when the property is omitted.
            assertThat(request.getDocuments()).isEmpty();
            assertThat(validate(request)).isEmpty();
        }

        @Test
        void emptyDocumentsIsValid() {
            request.setDocuments(List.of());

            assertThat(validate(request)).isEmpty();
        }

        @Test
        void explicitNullDocumentsIsRejected() {
            request.setDocuments(null);

            assertThat(hasViolation(validate(request), "documents", NotNull.class)).isTrue();
        }

        @Test
        void existingDocumentWithIdAndVersionIsValid() {
            request.setDocuments(List.of(existing(10L, 2L, "DOC-EXISTING")));

            assertThat(validate(request)).isEmpty();
        }

        @Test
        void newDocumentWithoutIdAndVersionIsValid() {
            request.setDocuments(List.of(fresh("DOC-NEW")));

            assertThat(validate(request)).isEmpty();
        }

        @Test
        void mixedExistingAndNewDocumentsAreValid() {
            request.setDocuments(List.of(existing(10L, 0L, "DOC-EXISTING"), fresh("DOC-NEW")));

            assertThat(validate(request)).isEmpty();
        }

        @Test
        void idWithoutVersionIsRejected() {
            var document = fresh("DOC-PARTIAL");
            document.setId(10L);
            request.setDocuments(List.of(document));

            assertThat(hasViolation(validate(request), "documents[0].identityPairConsistent", AssertTrue.class))
                    .isTrue();
        }

        @Test
        void versionWithoutIdIsRejected() {
            var document = fresh("DOC-PARTIAL");
            document.setVersion(1L);
            request.setDocuments(List.of(document));

            assertThat(hasViolation(validate(request), "documents[0].identityPairConsistent", AssertTrue.class))
                    .isTrue();
        }

        @Test
        void negativeNestedVersionIsRejected() {
            request.setDocuments(List.of(existing(10L, -1L, "DOC-EXISTING")));

            assertThat(hasViolation(validate(request), "documents[0].version", PositiveOrZero.class)).isTrue();
        }

        @Test
        void existingDocumentRequiresNumberAndExpirationDate() {
            var document = existing(10L, 1L, " ");
            document.setExpirationDate(null);
            request.setDocuments(List.of(document));
            var violations = validate(request);

            assertThat(hasViolation(violations, "documents[0].number", NotBlank.class)).isTrue();
            assertThat(hasViolation(violations, "documents[0].expirationDate", NotNull.class)).isTrue();
        }

        @Test
        void newDocumentRequiresNumberAndExpirationDate() {
            var document = new ClientDocumentPutRequest();
            request.setDocuments(List.of(document));
            var violations = validate(request);

            assertThat(hasViolation(violations, "documents[0].number", NotBlank.class)).isTrue();
            assertThat(hasViolation(violations, "documents[0].expirationDate", NotNull.class)).isTrue();
        }

        @Test
        void expiredNestedDocumentIsAccepted() {
            var document = fresh("DOC-EXPIRED");
            document.setExpirationDate(LocalDate.of(2001, 1, 1));
            request.setDocuments(List.of(document));

            assertThat(validate(request)).isEmpty();
        }

        @Test
        void nullNestedElementIsRejected() {
            request.setDocuments(java.util.Arrays.asList((ClientDocumentPutRequest) null));

            assertThat(hasViolationOn(validate(request), "documents[0].<list element>")).isTrue();
        }
    }

    @Nested
    class NestedIdentityRuleInIsolation {

        @Test
        void bothAbsentIsConsistent() {
            assertThat(fresh("DOC").isIdentityPairConsistent()).isTrue();
        }

        @Test
        void bothPresentIsConsistent() {
            assertThat(existing(1L, 0L, "DOC").isIdentityPairConsistent()).isTrue();
        }

        @Test
        void partialPairIsInconsistent() {
            var document = fresh("DOC");
            document.setId(1L);

            assertThat(document.isIdentityPairConsistent()).isFalse();
        }
    }
}
