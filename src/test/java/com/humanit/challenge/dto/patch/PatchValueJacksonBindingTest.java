package com.humanit.challenge.dto.patch;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.humanit.challenge.dto.client.ClientDocumentPatchRequest;
import com.humanit.challenge.dto.client.ClientPatchRequest;
import com.humanit.challenge.dto.document.DocumentPatchRequest;

/**
 * Verifies the Jackson tri-state binding: omitted (ABSENT), explicit null (PRESENT_NULL),
 * supplied value (PRESENT_VALUE), generic types, nested PATCH DTOs and collections of them.
 */
class PatchValueJacksonBindingTest {

    private final JsonMapper mapper = JsonMapper.builder().findAndAddModules().build();

    private <T> T read(String json, Class<T> type) throws Exception {
        return mapper.readValue(json, type);
    }

    @Test
    void missingPropertiesRemainAbsent() throws Exception {
        ClientPatchRequest request = read("{\"version\": 0}", ClientPatchRequest.class);

        assertThat(request.getFirstName().isAbsent()).isTrue();
        assertThat(request.getLastName().isAbsent()).isTrue();
        assertThat(request.getTaxIdentifier().isAbsent()).isTrue();
        assertThat(request.getEmail().isAbsent()).isTrue();
        assertThat(request.getPhoneNumber().isAbsent()).isTrue();
        assertThat(request.getDocuments().isAbsent()).isTrue();
    }

    @Test
    void explicitNullBecomesPresentNull() throws Exception {
        ClientPatchRequest request = read("{\"version\": 0, \"firstName\": null, \"documents\": null}",
                ClientPatchRequest.class);

        assertThat(request.getFirstName()).isEqualTo(PatchValue.presentNull());
        assertThat(request.getFirstName().isPresentNull()).isTrue();
        assertThat(request.getDocuments().isPresentNull()).isTrue();
        assertThat(request.getLastName().isAbsent()).isTrue();
    }

    @Test
    void suppliedNonNullValueBecomesPresentValue() throws Exception {
        ClientPatchRequest request = read("{\"version\": 0, \"firstName\": \"Ana\"}", ClientPatchRequest.class);

        assertThat(request.getFirstName().isPresentValue()).isTrue();
        assertThat(request.getFirstName().get()).isEqualTo("Ana");
    }

    @Test
    void suppliedEmptyStringIsPresentValueNotNull() throws Exception {
        DocumentPatchRequest request = read("{\"version\": 0, \"description\": \"\"}", DocumentPatchRequest.class);

        assertThat(request.getDescription().isPresentValue()).isTrue();
        assertThat(request.getDescription().get()).isEmpty();
    }

    @Test
    void documentPatchDescriptionHasThreeDistinguishableStates() throws Exception {
        DocumentPatchRequest omitted = read("{\"version\": 0}", DocumentPatchRequest.class);
        DocumentPatchRequest explicitNull = read("{\"version\": 0, \"description\": null}", DocumentPatchRequest.class);
        DocumentPatchRequest supplied = read("{\"version\": 0, \"description\": \"Passport\"}", DocumentPatchRequest.class);

        assertThat(omitted.getDescription().state()).isEqualTo(PatchValue.State.ABSENT);
        assertThat(explicitNull.getDescription().state()).isEqualTo(PatchValue.State.PRESENT_NULL);
        assertThat(supplied.getDescription().state()).isEqualTo(PatchValue.State.PRESENT_VALUE);
        assertThat(supplied.getDescription().get()).isEqualTo("Passport");
    }

    @Test
    void localDateScalarBindsWithTriStateSemantics() throws Exception {
        DocumentPatchRequest supplied = read("{\"version\": 0, \"expirationDate\": \"2001-01-01\"}",
                DocumentPatchRequest.class);
        DocumentPatchRequest explicitNull = read("{\"version\": 0, \"expirationDate\": null}",
                DocumentPatchRequest.class);

        assertThat(supplied.getExpirationDate().get()).isEqualTo(LocalDate.of(2001, 1, 1));
        assertThat(explicitNull.getExpirationDate().isPresentNull()).isTrue();
        assertThat(read("{\"version\": 0}", DocumentPatchRequest.class).getExpirationDate().isAbsent()).isTrue();
    }

    @Test
    void genericRootStringBindsToPatchValue() throws Exception {
        PatchValue<String> value = mapper.readValue("\"Ana\"", new TypeReference<PatchValue<String>>() {
        });

        assertThat(value.isPresentValue()).isTrue();
        assertThat(value.get()).isEqualTo("Ana");
    }

    @Test
    void genericRootNullBindsToPresentNull() throws Exception {
        PatchValue<String> value = mapper.readValue("null", new TypeReference<PatchValue<String>>() {
        });

        assertThat(value.isPresentNull()).isTrue();
    }

    @Test
    void genericRootCollectionOfNestedPatchDtosBinds() throws Exception {
        PatchValue<List<ClientDocumentPatchRequest>> value = mapper.readValue(
                "[{\"id\": 3, \"version\": 1}, {\"number\": \"DOC-NEW\", \"expirationDate\": \"2099-01-01\"}]",
                new TypeReference<PatchValue<List<ClientDocumentPatchRequest>>>() {
                });

        assertThat(value.isPresentValue()).isTrue();
        assertThat(value.get()).hasSize(2);
        ClientDocumentPatchRequest existing = value.get().get(0);
        assertThat(existing.getId()).isEqualTo(3L);
        assertThat(existing.getNumber().isAbsent()).isTrue();
        ClientDocumentPatchRequest fresh = value.get().get(1);
        assertThat(fresh.getNumber().get()).isEqualTo("DOC-NEW");
        assertThat(fresh.getExpirationDate().get()).isEqualTo(LocalDate.of(2099, 1, 1));
    }

    @Test
    void emptyDocumentsArrayIsPresentValueWithNoEntries() throws Exception {
        ClientPatchRequest request = read("{\"version\": 0, \"documents\": []}", ClientPatchRequest.class);

        assertThat(request.getDocuments().isPresentValue()).isTrue();
        assertThat(request.getDocuments().get()).isEmpty();
    }

    @Test
    void nestedDocumentsKeepPerScalarTriStateStates() throws Exception {
        ClientPatchRequest request = read("""
                {"version": 4,
                 "documents": [
                   {"id": 7, "version": 2, "description": null},
                   {"id": 8, "version": 0, "number": "DOC-8"},
                   {"number": "DOC-NEW", "expirationDate": "2099-12-31", "description": "fresh"}
                 ]}
                """, ClientPatchRequest.class);

        var entries = request.getDocuments().get();
        assertThat(entries).hasSize(3);

        ClientDocumentPatchRequest cleared = entries.get(0);
        assertThat(cleared.getId()).isEqualTo(7L);
        assertThat(cleared.getDescription().isPresentNull()).isTrue();
        assertThat(cleared.getNumber().isAbsent()).isTrue();
        assertThat(cleared.getExpirationDate().isAbsent()).isTrue();

        ClientDocumentPatchRequest renamed = entries.get(1);
        assertThat(renamed.getNumber().get()).isEqualTo("DOC-8");
        assertThat(renamed.getDescription().isAbsent()).isTrue();

        ClientDocumentPatchRequest created = entries.get(2);
        assertThat(created.getId()).isNull();
        assertThat(created.getVersion()).isNull();
        assertThat(created.getDescription().get()).isEqualTo("fresh");
    }

    @Test
    void nullElementInsideDocumentsBindsAsNullEntry() throws Exception {
        ClientPatchRequest request = read("{\"version\": 0, \"documents\": [null]}", ClientPatchRequest.class);

        assertThat(request.getDocuments().isPresentValue()).isTrue();
        assertThat(request.getDocuments().get()).containsExactly((ClientDocumentPatchRequest) null);
    }

    @Test
    void springConfiguredObjectMapperPreservesTheThreeStates() throws Exception {
        ObjectMapper springMapper = Jackson2ObjectMapperBuilder.json().build();

        ClientPatchRequest request = springMapper.readValue("""
                {"version": 1,
                 "firstName": null,
                 "lastName": "Silva",
                 "documents": [{"id": 4, "version": 2, "description": null}]}
                """, ClientPatchRequest.class);

        assertThat(request.getFirstName().isPresentNull()).isTrue();
        assertThat(request.getLastName().get()).isEqualTo("Silva");
        assertThat(request.getTaxIdentifier().isAbsent()).isTrue();
        assertThat(request.getDocuments().get().get(0).getDescription().isPresentNull()).isTrue();
        assertThat(request.getDocuments().get().get(0).getNumber().isAbsent()).isTrue();
    }

    @Test
    void settersRejectNullPatchHolders() {
        ClientPatchRequest request = new ClientPatchRequest();

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> request.setFirstName(null))
                .isInstanceOf(NullPointerException.class);
    }
}
