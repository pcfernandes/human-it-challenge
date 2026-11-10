package com.humanit.challenge.dto.client;

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

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Bean Validation contract for {@code ClientPatchRequest}, exercised through real Jackson
 * binding so that the tri-state semantics are the ones validated in production.
 */
class ClientPatchRequestValidationTest {

    private static final String SCALARS_OMITTED = "\"version\": 0";

    private final JsonMapper mapper = JsonMapper.builder().findAndAddModules().build();

    private ClientPatchRequest bind(String json) throws Exception {
        return mapper.readValue("{" + json + "}", ClientPatchRequest.class);
    }

    @Nested
    class OmittedAndSuppliedScalars {

        @Test
        void onlyVersionIsAValidNoOpPatch() throws Exception {
            assertThat(validate(bind(SCALARS_OMITTED))).isEmpty();
        }

        @Test
        void suppliedValidScalarsAreAccepted() throws Exception {
            var request = bind(SCALARS_OMITTED + """
                    , "firstName": "Ana", "lastName": "Silva", "taxIdentifier": "TAX-123",
                    "email": "ana.silva@example.com", "phoneNumber": "+351 912 345 678"
                    """);

            assertThat(validate(request)).isEmpty();
        }

        @Test
        void missingVersionIsRejected() throws Exception {
            var request = bind("\"firstName\": \"Ana\"");

            assertThat(hasViolation(validate(request), "version", NotNull.class)).isTrue();
        }

        @Test
        void negativeVersionIsRejected() throws Exception {
            var request = bind("\"version\": -1");

            assertThat(hasViolation(validate(request), "version", PositiveOrZero.class)).isTrue();
        }
    }

    @Nested
    class RequiredScalars {

        @ParameterizedTest
        @ValueSource(strings = {"firstName", "lastName", "taxIdentifier", "email", "phoneNumber"})
        void explicitNullIsRejectedForEveryRequiredScalar(String property) throws Exception {
            var request = bind(SCALARS_OMITTED + ", \"" + property + "\": null");

            assertThat(hasViolation(validate(request), property, PatchRequired.class)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"firstName", "lastName", "taxIdentifier", "phoneNumber"})
        void blankSuppliedValueIsRejected(String property) throws Exception {
            var request = bind(SCALARS_OMITTED + ", \"" + property + "\": \"   \"");

            assertThat(hasViolation(validate(request), property, NotBlank.class)).isTrue();
        }

        @Test
        void blankEmailIsRejected() throws Exception {
            var request = bind(SCALARS_OMITTED + ", \"email\": \"\"");

            assertThat(hasViolation(validate(request), "email", NotBlank.class)).isTrue();
        }

        @Test
        void omittedRequiredScalarsAreNotViolations() throws Exception {
            assertThat(validate(bind(SCALARS_OMITTED + ", \"firstName\": \"Ana\""))).isEmpty();
        }
    }

    @Nested
    class LengthLimits {

        @Test
        void firstNameAcceptsExactly100AndRejects101() throws Exception {
            assertThat(validate(bind(SCALARS_OMITTED + ", \"firstName\": \"" + "a".repeat(100) + "\""))).isEmpty();
            assertThat(hasViolation(validate(bind(SCALARS_OMITTED + ", \"firstName\": \"" + "a".repeat(101) + "\"")),
                    "firstName", Size.class)).isTrue();
        }

        @Test
        void lastNameAcceptsExactly100AndRejects101() throws Exception {
            assertThat(validate(bind(SCALARS_OMITTED + ", \"lastName\": \"" + "a".repeat(100) + "\""))).isEmpty();
            assertThat(hasViolation(validate(bind(SCALARS_OMITTED + ", \"lastName\": \"" + "a".repeat(101) + "\"")),
                    "lastName", Size.class)).isTrue();
        }

        @Test
        void taxIdentifierAcceptsExactly50AndRejects51() throws Exception {
            assertThat(validate(bind(SCALARS_OMITTED + ", \"taxIdentifier\": \"" + "t".repeat(50) + "\""))).isEmpty();
            assertThat(hasViolation(validate(bind(SCALARS_OMITTED + ", \"taxIdentifier\": \"" + "t".repeat(51) + "\"")),
                    "taxIdentifier", Size.class)).isTrue();
        }

        @Test
        void phoneNumberAcceptsExactly50AndRejects51() throws Exception {
            assertThat(validate(bind(SCALARS_OMITTED + ", \"phoneNumber\": \"" + "1".repeat(50) + "\""))).isEmpty();
            assertThat(hasViolation(validate(bind(SCALARS_OMITTED + ", \"phoneNumber\": \"" + "1".repeat(51) + "\"")),
                    "phoneNumber", Size.class)).isTrue();
        }

        @Test
        void emailAcceptsExactly255AndRejects256() throws Exception {
            String local = "a".repeat(64);
            String domainLabel = "b".repeat(63);
            // 64 + "@" + 63 + "." + 63 + "." + 62 = 255 characters
            String max = local + "@" + domainLabel + "." + domainLabel + "." + "c".repeat(62);
            assertThat(max).hasSize(255);

            assertThat(validate(bind(SCALARS_OMITTED + ", \"email\": \"" + max + "\""))).isEmpty();
            assertThat(hasViolation(validate(bind(SCALARS_OMITTED + ", \"email\": \"x" + max + "\"")),
                    "email", Size.class)).isTrue();
        }
    }

    @Nested
    class EmailFormat {

        @ParameterizedTest
        @ValueSource(strings = {"not-an-email", "ana@", "@example.com", "ana example.com", "ana@example.com "})
        void invalidEmailIsRejected(String email) throws Exception {
            var request = bind(SCALARS_OMITTED + ", \"email\": " + mapper.writeValueAsString(email));

            assertThat(hasViolation(validate(request), "email", Email.class)).isTrue();
        }

        @Test
        void validEmailIsAccepted() throws Exception {
            assertThat(validate(bind(SCALARS_OMITTED + ", \"email\": \"ana.silva@example.com\""))).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(strings = {"ana.silva@example.com", "not-an-email", "ana@", "@example.com",
                "ana example.com", "ana@example.com "})
        void patchEmailAgreesWithPostEmailRule(String email) throws Exception {
            var patch = bind(SCALARS_OMITTED + ", \"email\": " + mapper.writeValueAsString(email));
            var post = mapper.readValue(
                    "{\"firstName\": \"Ana\", \"lastName\": \"Silva\", \"taxIdentifier\": \"TAX-1\", "
                            + "\"phoneNumber\": \"+351\", \"email\": " + mapper.writeValueAsString(email) + "}",
                    ClientCreateRequest.class);

            boolean patchRejects = hasViolation(validate(patch), "email", Email.class);
            boolean postRejects = hasViolation(validate(post), "email",
                    Email.class);

            assertThat(patchRejects).isEqualTo(postRejects);
        }
    }

    @Nested
    class DocumentsCollection {

        @Test
        void omittedDocumentsPreserveExistingDocuments() throws Exception {
            var request = bind(SCALARS_OMITTED);

            assertThat(request.getDocuments().isAbsent()).isTrue();
            assertThat(validate(request)).isEmpty();
        }

        @Test
        void emptyDocumentsPreserveExistingDocuments() throws Exception {
            var request = bind(SCALARS_OMITTED + ", \"documents\": []");

            assertThat(request.getDocuments().isPresentValue()).isTrue();
            assertThat(validate(request)).isEmpty();
        }

        @Test
        void explicitNullDocumentsIsRejected() throws Exception {
            var request = bind(SCALARS_OMITTED + ", \"documents\": null");

            assertThat(hasViolation(validate(request), "documents", PatchRequired.class)).isTrue();
        }

        @Test
        void nullEntryInsideDocumentsIsRejected() throws Exception {
            var request = bind(SCALARS_OMITTED + ", \"documents\": [null]");

            assertThat(hasViolation(validate(request), "documents[0].<list element>", NotNull.class)).isTrue();
        }
    }

    @Nested
    class NestedDocumentIdentity {

        private static final String FUTURE = "\"expirationDate\": \"2099-01-01\"";

        @Test
        void existingDocumentWithIdAndVersionIsValidWithOmittedScalars() throws Exception {
            var request = bind(SCALARS_OMITTED + ", \"documents\": [{\"id\": 7, \"version\": 2}]");

            assertThat(validate(request)).isEmpty();
        }

        @Test
        void newDocumentWithNumberAndExpirationDateIsValid() throws Exception {
            var request = bind(SCALARS_OMITTED + ", \"documents\": [{\"number\": \"DOC-NEW\", " + FUTURE + "}]");

            assertThat(validate(request)).isEmpty();
        }

        @Test
        void newDocumentWithoutNumberIsRejected() throws Exception {
            var request = bind(SCALARS_OMITTED + ", \"documents\": [{" + FUTURE + "}]");

            assertThat(hasViolation(validate(request), "documents[0].newDocumentFieldsComplete",
                    AssertTrue.class)).isTrue();
        }

        @Test
        void newDocumentWithoutExpirationDateIsRejected() throws Exception {
            var request = bind(SCALARS_OMITTED + ", \"documents\": [{\"number\": \"DOC-NEW\"}]");

            assertThat(hasViolation(validate(request), "documents[0].newDocumentFieldsComplete",
                    AssertTrue.class)).isTrue();
        }

        @Test
        void newDocumentWithExplicitNullNumberIsRejected() throws Exception {
            var request = bind(SCALARS_OMITTED + ", \"documents\": [{\"number\": null, " + FUTURE + "}]");

            assertThat(hasViolation(validate(request), "documents[0].number", PatchRequired.class)).isTrue();
        }

        @Test
        void idWithoutVersionIsRejected() throws Exception {
            var request = bind(SCALARS_OMITTED + ", \"documents\": [{\"id\": 7}]");

            assertThat(hasViolation(validate(request), "documents[0].identityPairConsistent", AssertTrue.class))
                    .isTrue();
        }

        @Test
        void versionWithoutIdIsRejected() throws Exception {
            var request = bind(SCALARS_OMITTED + ", \"documents\": [{\"version\": 1, \"number\": \"DOC-X\", " + FUTURE + "}]");

            assertThat(hasViolation(validate(request), "documents[0].identityPairConsistent", AssertTrue.class))
                    .isTrue();
        }

        @Test
        void negativeNestedVersionIsRejected() throws Exception {
            var request = bind(SCALARS_OMITTED + ", \"documents\": [{\"id\": 7, \"version\": -1}]");

            assertThat(hasViolation(validate(request), "documents[0].version", PositiveOrZero.class)).isTrue();
        }

        @Test
        void existingDocumentDescriptionMayBeCleared() throws Exception {
            var request = bind(SCALARS_OMITTED + ", \"documents\": [{\"id\": 7, \"version\": 2, \"description\": null}]");

            assertThat(validate(request)).isEmpty();
        }

        @Test
        void existingDocumentRequiredScalarExplicitNullIsRejected() throws Exception {
            var request = bind(SCALARS_OMITTED + ", \"documents\": [{\"id\": 7, \"version\": 2, \"expirationDate\": null}]");

            assertThat(hasViolation(validate(request), "documents[0].expirationDate", PatchRequired.class)).isTrue();
        }

        @Test
        void nestedNumberTooLongIsRejected() throws Exception {
            var request = bind(SCALARS_OMITTED + ", \"documents\": [{\"id\": 7, \"version\": 2, \"number\": \""
                    + "n".repeat(101) + "\"}]");

            assertThat(hasViolation(validate(request), "documents[0].number", Size.class)).isTrue();
        }

        @Test
        void nestedDescriptionTooLongIsRejected() throws Exception {
            var request = bind(SCALARS_OMITTED + ", \"documents\": [{\"id\": 7, \"version\": 2, \"description\": \""
                    + "d".repeat(1001) + "\"}]");

            assertThat(hasViolation(validate(request), "documents[0].description", Size.class)).isTrue();
        }

        @Test
        void expiredNestedDocumentIsAccepted() throws Exception {
            var request = bind(SCALARS_OMITTED + ", \"documents\": [{\"id\": 7, \"version\": 2, \"expirationDate\": \"2001-01-01\"}]");

            assertThat(validate(request)).isEmpty();
            assertThat(hasViolationOn(validate(request), "documents[0].expirationDate")).isFalse();
        }
    }
}
