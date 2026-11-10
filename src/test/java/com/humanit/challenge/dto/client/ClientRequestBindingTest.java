package com.humanit.challenge.dto.client;

import static com.humanit.challenge.dto.DtoValidationSupport.hasViolation;
import static com.humanit.challenge.dto.DtoValidationSupport.validate;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.json.JsonMapper;

import jakarta.validation.constraints.NotNull;

/**
 * Verifies the standard Jackson binding of the Client documents collection, which is the
 * only binding behavior Part 1 relies on. These tests do NOT exercise the project-owned
 * PATCH tri-state binding, which belongs to Part 2.
 */
class ClientRequestBindingTest {

    private static final String VALID_PUT_SCALARS = """
            "firstName": "Ana",
            "lastName": "Silva",
            "taxIdentifier": "TAX-123",
            "email": "ana.silva@example.com",
            "phoneNumber": "+351 912 345 678",
            "version": 0
            """;

    private final JsonMapper mapper = JsonMapper.builder().findAndAddModules().build();

    @Test
    void putWithOmittedDocumentsPropertyIsValidAndKeepsEmptyDefault() throws Exception {
        var request = mapper.readValue("{" + VALID_PUT_SCALARS + "}", ClientPutRequest.class);

        assertThat(request.getDocuments()).isEmpty();
        assertThat(validate(request)).isEmpty();
    }

    @Test
    void putWithEmptyDocumentsArrayIsValid() throws Exception {
        var request = mapper.readValue("{" + VALID_PUT_SCALARS + ", \"documents\": []}", ClientPutRequest.class);

        assertThat(request.getDocuments()).isEmpty();
        assertThat(validate(request)).isEmpty();
    }

    @Test
    void putWithExplicitNullDocumentsIsRejected() throws Exception {
        var request = mapper.readValue("{" + VALID_PUT_SCALARS + ", \"documents\": null}", ClientPutRequest.class);

        assertThat(request.getDocuments()).isNull();
        assertThat(hasViolation(validate(request), "documents", NotNull.class)).isTrue();
    }

    @Test
    void putBindsExistingAndNewNestedDocuments() throws Exception {
        var json = "{" + VALID_PUT_SCALARS + """
                , "documents": [
                    {"id": 7, "version": 2, "number": "DOC-EXISTING", "expirationDate": "2001-01-01"},
                    {"number": "DOC-NEW", "description": null, "expirationDate": "2099-12-31"}
                ]}
                """;

        var request = mapper.readValue(json, ClientPutRequest.class);

        assertThat(request.getDocuments()).hasSize(2);
        ClientDocumentPutRequest existing = request.getDocuments().get(0);
        assertThat(existing.getId()).isEqualTo(7L);
        assertThat(existing.getVersion()).isEqualTo(2L);
        assertThat(existing.getExpirationDate().getYear()).isEqualTo(2001);
        ClientDocumentPutRequest fresh = request.getDocuments().get(1);
        assertThat(fresh.getId()).isNull();
        assertThat(fresh.getVersion()).isNull();
        assertThat(fresh.getDescription()).isNull();
        assertThat(validate(request)).isEmpty();
    }

    @Test
    void createWithOmittedDocumentsIsValid() throws Exception {
        var json = """
                {"firstName": "Ana", "lastName": "Silva", "taxIdentifier": "TAX-123",
                 "email": "ana.silva@example.com", "phoneNumber": "+351 912 345 678"}
                """;

        var request = mapper.readValue(json, ClientCreateRequest.class);

        assertThat(request.getDocuments()).isEmpty();
        assertThat(validate(request)).isEmpty();
    }

    @Test
    void createWithExplicitNullDocumentsIsTreatedAsOmitted() throws Exception {
        var json = """
                {"firstName": "Ana", "lastName": "Silva", "taxIdentifier": "TAX-123",
                 "email": "ana.silva@example.com", "phoneNumber": "+351 912 345 678",
                 "documents": null}
                """;

        var request = mapper.readValue(json, ClientCreateRequest.class);

        assertThat(request.getDocuments()).isNotNull().isEmpty();
        assertThat(validate(request)).isEmpty();
    }

    @Test
    void createBindsSuppliedDocumentsAndValidatesThem() throws Exception {
        var json = """
                {"firstName": "Ana", "lastName": "Silva", "taxIdentifier": "TAX-123",
                 "email": "ana.silva@example.com", "phoneNumber": "+351 912 345 678",
                 "documents": [{"number": "DOC-EXPIRED", "expirationDate": "2001-01-01"},
                               {"number": "", "expirationDate": "2099-01-01"}]}
                """;

        var request = mapper.readValue(json, ClientCreateRequest.class);

        assertThat(request.getDocuments()).hasSize(2);
        assertThat(request.getDocuments().get(0).getNumber()).isEqualTo("DOC-EXPIRED");
        assertThat(validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("documents[1].number");
    }

    @Test
    void createSetterNormalizesExplicitNullToEmptyList() {
        var request = new ClientCreateRequest();
        request.setDocuments(null);

        assertThat(request.getDocuments()).isNotNull().isEmpty();
    }
}
