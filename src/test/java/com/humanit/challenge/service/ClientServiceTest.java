package com.humanit.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.humanit.challenge.domain.Client;
import com.humanit.challenge.dto.client.ClientCreateRequest;
import com.humanit.challenge.dto.client.ClientDocumentPatchRequest;
import com.humanit.challenge.dto.client.ClientDocumentPutRequest;
import com.humanit.challenge.dto.client.ClientPatchRequest;
import com.humanit.challenge.dto.client.ClientPutRequest;
import com.humanit.challenge.dto.client.ClientResponse;
import com.humanit.challenge.dto.document.DocumentCreateRequest;
import com.humanit.challenge.dto.patch.PatchValue;
import com.humanit.challenge.repository.ClientRepository;
import com.humanit.challenge.repository.DocumentRepository;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true"
})
@Import(ClientService.class)
class ClientServiceTest {

    @Autowired
    private ClientService clientService;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void createAndRetrieve() {
        ClientCreateRequest request = new ClientCreateRequest();
        request.setFirstName("Ada");
        request.setLastName("Lovelace");
        request.setTaxIdentifier("TAX-1");
        request.setEmail("ada@example.com");
        request.setPhoneNumber("123");
        request.setDocuments(List.of(new DocumentCreateRequest("DOC-1", "desc", LocalDate.of(2027, 1, 1))));

        var created = clientService.create(request);
        assertThat(created.id()).isNotNull();
        assertThat(created.version()).isNotNull();
        assertThat(created.documents()).hasSize(1);

        var fetched = clientService.getById(created.id());
        assertThat(fetched.firstName()).isEqualTo("Ada");
        assertThat(fetched.documents()).hasSize(1);
        assertThat(fetched.documents().get(0).number()).isEqualTo("DOC-1");
    }

    @Test
    void updateHappyPath() {
        ClientCreateRequest create = new ClientCreateRequest();
        create.setFirstName("Ada");
        create.setLastName("Lovelace");
        create.setTaxIdentifier("TAX-2");
        create.setEmail("ada@example.com");
        create.setPhoneNumber("123");
        var created = clientService.create(create);

        ClientPutRequest put = new ClientPutRequest();
        put.setFirstName("Grace");
        put.setLastName("Hopper");
        put.setTaxIdentifier("TAX-2");
        put.setEmail("grace@example.com");
        put.setPhoneNumber("999");
        put.setVersion(created.version());
        put.setDocuments(List.of());

        var updated = clientService.update(created.id(), put);
        assertThat(updated.firstName()).isEqualTo("Grace");
        assertThat(updated.email()).isEqualTo("grace@example.com");
        assertThat(updated.version()).isNotEqualTo(created.version());
    }

    @Test
    void missingClientThrowsNotFound() {
        assertThatThrownBy(() -> clientService.getById(999999L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void versionConflictThrowsConflict() {
        ClientCreateRequest create = new ClientCreateRequest();
        create.setFirstName("Ada");
        create.setLastName("Lovelace");
        create.setTaxIdentifier("TAX-3");
        create.setEmail("ada@example.com");
        create.setPhoneNumber("123");
        var created = clientService.create(create);

        ClientPutRequest put = new ClientPutRequest();
        put.setFirstName("Grace");
        put.setLastName("Hopper");
        put.setTaxIdentifier("TAX-3");
        put.setEmail("grace@example.com");
        put.setPhoneNumber("999");
        put.setVersion(created.version() + 1);
        put.setDocuments(List.of());

        assertThatThrownBy(() -> clientService.update(created.id(), put))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void duplicateDocumentNumberInSingleCreateThrowsConflict() {
        ClientCreateRequest request = new ClientCreateRequest();
        request.setFirstName("Ada");
        request.setLastName("Lovelace");
        request.setTaxIdentifier("TAX-DUP");
        request.setEmail("ada@example.com");
        request.setPhoneNumber("123");
        request.setDocuments(List.of(
                new DocumentCreateRequest("DOC-DUP", "desc", LocalDate.of(2027, 1, 1)),
                new DocumentCreateRequest("DOC-DUP", "desc2", LocalDate.of(2027, 2, 2))));

        assertThatThrownBy(() -> clientService.create(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void nestedDocumentScenario() {
        ClientCreateRequest create = new ClientCreateRequest();
        create.setFirstName("Ada");
        create.setLastName("Lovelace");
        create.setTaxIdentifier("TAX-4");
        create.setEmail("ada@example.com");
        create.setPhoneNumber("123");
        var created = clientService.create(create);

        // update adding a new nested document
        ClientDocumentPutRequest doc = new ClientDocumentPutRequest();
        doc.setNumber("DOC-2");
        doc.setDescription("desc");
        doc.setExpirationDate(LocalDate.of(2027, 2, 2));

        ClientPutRequest put = new ClientPutRequest();
        put.setFirstName("Ada");
        put.setLastName("Lovelace");
        put.setTaxIdentifier("TAX-4");
        put.setEmail("ada@example.com");
        put.setPhoneNumber("123");
        put.setVersion(created.version());
        put.setDocuments(List.of(doc));

        var updated = clientService.update(created.id(), put);
        assertThat(updated.documents()).hasSize(1);
        assertThat(updated.documents().get(0).number()).isEqualTo("DOC-2");

        // patch preserving documents when empty/omitted
        ClientPatchRequest patch = new ClientPatchRequest();
        patch.setVersion(updated.version());
        patch.setFirstName(PatchValue.of("Ada2"));
        var patched = clientService.patch(created.id(), patch);
        assertThat(patched.firstName()).isEqualTo("Ada2");
        assertThat(patched.documents()).hasSize(1);
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void clientPutWithOmittedOrEmptyDocumentsPreservesDocumentIds(boolean explicitlyEmpty) {
        var created = createClientWithDocuments("TAX-PUT-PRESERVE-" + explicitlyEmpty, "DOC-PUT-A", "DOC-PUT-B");
        List<Long> originalIds = created.documents().stream().map(document -> document.id()).toList();

        ClientPutRequest put = putRequest(created);
        if (explicitlyEmpty) {
            put.setDocuments(List.of());
        }

        var updated = clientService.update(created.id(), put);

        assertThat(updated.documents()).extracting(document -> document.id()).containsExactlyElementsOf(originalIds);
        assertThat(documentRepository.findAll()).hasSize(2);
    }

    @Test
    void clientPutUpdatesExistingCreatesNewAndPreservesOmittedDocuments() {
        var created = createClientWithDocuments("TAX-PUT-MERGE", "DOC-PUT-UPDATE", "DOC-PUT-OMITTED");
        var existing = created.documents().get(0);
        var omitted = created.documents().get(1);

        ClientDocumentPutRequest updateExisting = new ClientDocumentPutRequest();
        updateExisting.setId(existing.id());
        updateExisting.setVersion(existing.version());
        updateExisting.setNumber("DOC-PUT-UPDATED");
        updateExisting.setDescription("updated in place");
        updateExisting.setExpirationDate(LocalDate.of(2030, 1, 1));

        ClientDocumentPutRequest createNew = new ClientDocumentPutRequest();
        createNew.setNumber("DOC-PUT-NEW");
        createNew.setDescription("created without identity");
        createNew.setExpirationDate(LocalDate.of(2031, 2, 2));

        ClientPutRequest put = putRequest(created);
        put.setDocuments(List.of(updateExisting, createNew));
        var updated = clientService.update(created.id(), put);

        assertThat(updated.documents()).hasSize(3);
        assertThat(updated.documents()).anySatisfy(document -> {
            assertThat(document.id()).isEqualTo(existing.id());
            assertThat(document.number()).isEqualTo("DOC-PUT-UPDATED");
            assertThat(document.description()).isEqualTo("updated in place");
        });
        assertThat(updated.documents()).anySatisfy(document -> {
            assertThat(document.id()).isEqualTo(omitted.id());
            assertThat(document.number()).isEqualTo(omitted.number());
            assertThat(document.description()).isEqualTo(omitted.description());
        });
        assertThat(updated.documents()).anySatisfy(document -> {
            assertThat(document.id()).isNotNull();
            assertThat(document.number()).isEqualTo("DOC-PUT-NEW");
            assertThat(document.clientId()).isEqualTo(created.id());
        });
        assertThat(documentRepository.findAll()).hasSize(3);
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void clientPatchWithOmittedOrEmptyDocumentsPreservesCollection(boolean explicitlyEmpty) {
        var created = createClientWithDocuments("TAX-PATCH-PRESERVE-" + explicitlyEmpty,
                "DOC-PATCH-A", "DOC-PATCH-B");
        List<Long> originalIds = created.documents().stream().map(document -> document.id()).toList();

        ClientPatchRequest patch = new ClientPatchRequest();
        patch.setVersion(created.version());
        if (explicitlyEmpty) {
            patch.setDocuments(PatchValue.of(List.of()));
        }

        var updated = clientService.patch(created.id(), patch);

        assertThat(updated.documents()).extracting(document -> document.id()).containsExactlyElementsOf(originalIds);
        assertThat(documentRepository.findAll()).hasSize(2);
    }

    @Test
    void nestedPatchChangesSuppliedFieldsOnlyAndPreservesDocumentIdentity() {
        var created = createClientWithDocuments("TAX-NESTED-PATCH", "DOC-NESTED-PATCH", "DOC-NESTED-RETAIN");
        var original = created.documents().get(0);
        var untouched = created.documents().get(1);
        ClientDocumentPatchRequest nestedPatch = new ClientDocumentPatchRequest();
        nestedPatch.setId(original.id());
        nestedPatch.setVersion(original.version());
        nestedPatch.setDescription(PatchValue.of("description only"));

        ClientPatchRequest patch = new ClientPatchRequest();
        patch.setVersion(created.version());
        patch.setDocuments(PatchValue.of(List.of(nestedPatch)));
        var updated = clientService.patch(created.id(), patch);

        assertThat(updated.documents()).hasSize(2);
        assertThat(updated.documents()).anySatisfy(document -> {
            assertThat(document.id()).isEqualTo(original.id());
            assertThat(document.number()).isEqualTo(original.number());
            assertThat(document.description()).isEqualTo("description only");
            assertThat(document.expirationDate()).isEqualTo(original.expirationDate());
            assertThat(document.clientId()).isEqualTo(created.id());
        });
        assertThat(updated.documents()).anySatisfy(document -> {
            assertThat(document.id()).isEqualTo(untouched.id());
            assertThat(document.number()).isEqualTo(untouched.number());
            assertThat(document.description()).isEqualTo(untouched.description());
        });
        assertThat(documentRepository.findAll()).hasSize(2);
    }

    @Test
    void clientPatchChangesSuppliedScalarAndPreservesOmittedScalarsAfterReload() {
        var created = createClientWithDocuments("TAX-SCALAR-PATCH", "DOC-SCALAR-PATCH");
        ClientPatchRequest patch = new ClientPatchRequest();
        patch.setVersion(created.version());
        patch.setFirstName(PatchValue.of("Grace"));

        clientService.patch(created.id(), patch);
        entityManager.flush();
        entityManager.clear();

        Client reloaded = clientRepository.findById(created.id()).orElseThrow();
        assertThat(reloaded.getFirstName()).isEqualTo("Grace");
        assertThat(reloaded.getLastName()).isEqualTo("Lovelace");
        assertThat(reloaded.getTaxIdentifier()).isEqualTo("TAX-SCALAR-PATCH");
        assertThat(reloaded.getEmail()).isEqualTo("tax-scalar-patch@example.com");
        assertThat(reloaded.getPhoneNumber()).isEqualTo("123");
    }

    @Test
    void unknownNestedDocumentIdReturnsNotFoundWithoutCreatingDocuments() {
        var created = createClientWithDocuments("TAX-UNKNOWN-NESTED", "DOC-UNKNOWN-BASE");
        ClientDocumentPutRequest unknown = new ClientDocumentPutRequest();
        unknown.setId(Long.MAX_VALUE);
        unknown.setVersion(0L);
        unknown.setNumber("DOC-UNKNOWN-REQUEST");
        unknown.setExpirationDate(LocalDate.of(2030, 1, 1));
        ClientPutRequest put = putRequest(created);
        put.setDocuments(List.of(unknown));

        assertThatThrownBy(() -> clientService.update(created.id(), put))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
        assertThat(documentRepository.findAll()).hasSize(1);
        assertThat(clientService.getById(created.id()).documents()).extracting(document -> document.id())
                .containsExactly(created.documents().get(0).id());
    }

    @Test
    void crossClientNestedDocumentAssociationReturnsConflictWithoutTransfer() {
        var target = createClientWithDocuments("TAX-CROSS-TARGET", "DOC-CROSS-TARGET");
        var owner = createClientWithDocuments("TAX-CROSS-OWNER", "DOC-CROSS-OWNER");
        var foreignDocument = owner.documents().get(0);
        ClientDocumentPutRequest association = new ClientDocumentPutRequest();
        association.setId(foreignDocument.id());
        association.setVersion(foreignDocument.version());
        association.setNumber("DOC-CROSS-ATTEMPT");
        association.setDescription("must not transfer");
        association.setExpirationDate(LocalDate.of(2030, 1, 1));
        ClientPutRequest put = putRequest(target);
        put.setDocuments(List.of(association));

        assertThatThrownBy(() -> clientService.update(target.id(), put))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));
        assertThat(documentRepository.findAll()).hasSize(2);
        assertThat(clientService.getById(owner.id()).documents()).extracting(document -> document.id())
                .containsExactly(foreignDocument.id());
        assertThat(clientService.getById(target.id()).documents()).extracting(document -> document.id())
                .containsExactly(target.documents().get(0).id());
    }

    private ClientResponse createClientWithDocuments(String taxIdentifier, String... documentNumbers) {
        ClientCreateRequest create = new ClientCreateRequest();
        create.setFirstName("Ada");
        create.setLastName("Lovelace");
        create.setTaxIdentifier(taxIdentifier);
        create.setEmail(taxIdentifier.toLowerCase() + "@example.com");
        create.setPhoneNumber("123");
        List<DocumentCreateRequest> documents = new ArrayList<>();
        for (String number : documentNumbers) {
            documents.add(new DocumentCreateRequest(number, "original " + number, LocalDate.of(2028, 1, 1)));
        }
        create.setDocuments(documents);
        return clientService.create(create);
    }

    private ClientPutRequest putRequest(ClientResponse client) {
        ClientPutRequest put = new ClientPutRequest();
        put.setFirstName(client.firstName());
        put.setLastName(client.lastName());
        put.setTaxIdentifier(client.taxIdentifier());
        put.setEmail(client.email());
        put.setPhoneNumber(client.phoneNumber());
        put.setVersion(client.version());
        return put;
    }
}
