package com.humanit.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.humanit.challenge.domain.Client;
import com.humanit.challenge.dto.client.ClientDocumentPatchRequest;
import com.humanit.challenge.dto.client.ClientDocumentPutRequest;
import com.humanit.challenge.dto.client.ClientPatchRequest;
import com.humanit.challenge.dto.client.ClientPutRequest;
import com.humanit.challenge.dto.document.DocumentCreateRequest;
import com.humanit.challenge.dto.document.DocumentPatchRequest;
import com.humanit.challenge.dto.document.DocumentPutRequest;
import com.humanit.challenge.dto.patch.PatchValue;
import com.humanit.challenge.repository.ClientRepository;
import com.humanit.challenge.repository.DocumentRepository;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true"
})
@Import({ ClientService.class, DocumentService.class })
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class DocumentServiceTest {

    private static final AtomicLong CLIENT_SEQUENCE = new AtomicLong();

    @Autowired
    private DocumentService documentService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Test
    void createAndRetrieve() {
        Client client = createClient();

        var created = documentService.create(client.getId(), client.getVersion(),
                new DocumentCreateRequest("DOC-CREATE", "initial", LocalDate.of(2028, 1, 1)));
        var fetched = documentService.getById(client.getId(), created.id());

        assertThat(fetched.id()).isEqualTo(created.id());
        assertThat(fetched.clientId()).isEqualTo(client.getId());
        assertThat(fetched.number()).isEqualTo("DOC-CREATE");
        assertThat(fetched.version()).isNotNull();
    }

    @Test
    void listIsScopedToClientAndReturnsPage() {
        Client client = createClient();
        Client otherClient = createClient();
        documentService.create(client.getId(), client.getVersion(),
                new DocumentCreateRequest("DOC-LIST-1", null, LocalDate.of(2028, 1, 1)));
        documentService.create(otherClient.getId(), otherClient.getVersion(),
                new DocumentCreateRequest("DOC-LIST-2", null, LocalDate.of(2028, 1, 1)));

        var page = documentService.list(client.getId(),
                PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "id")));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).clientId()).isEqualTo(client.getId());
        assertThat(page.getContent().get(0).number()).isEqualTo("DOC-LIST-1");
    }

    @Test
    void putAndPatchUpdateEditableFieldsAndKeepOwner() {
        Client client = createClient();
        var created = documentService.create(client.getId(), client.getVersion(),
                new DocumentCreateRequest("DOC-UPDATE", "before", LocalDate.of(2028, 1, 1)));
        Long clientVersion = currentClientVersion(client.getId());

        var updated = documentService.update(client.getId(), created.id(), clientVersion,
                new DocumentPutRequest(created.version(), "DOC-UPDATED", "put", LocalDate.of(2029, 1, 1)));
        Long versionAfterPut = currentClientVersion(client.getId());
        DocumentPatchRequest patch = new DocumentPatchRequest();
        patch.setVersion(updated.version());
        patch.setDescription(PatchValue.presentNull());
        var patched = documentService.patch(client.getId(), created.id(), versionAfterPut, patch);

        assertThat(updated.number()).isEqualTo("DOC-UPDATED");
        assertThat(updated.id()).isEqualTo(created.id());
        assertThat(updated.expirationDate()).isEqualTo(LocalDate.of(2029, 1, 1));
        assertThat(patched.description()).isNull();
        assertThat(patched.id()).isEqualTo(created.id());
        assertThat(patched.number()).isEqualTo(updated.number());
        assertThat(patched.expirationDate()).isEqualTo(updated.expirationDate());
        assertThat(patched.clientId()).isEqualTo(client.getId());
        assertThat(patched.version()).isGreaterThan(updated.version());
        assertThat(documentRepository.findById(created.id()).orElseThrow().getClient().getId())
                .isEqualTo(client.getId());
    }

    @Test
    void standaloneAndAggregateDocumentPutAndPatchApplyEquivalentFieldSemantics() {
        Client client = createClient();
        var standalone = documentService.create(client.getId(), client.getVersion(),
                new DocumentCreateRequest("DOC-CONSISTENT-STANDALONE", "before", LocalDate.of(2028, 1, 1)));

        ClientPutRequest createNested = clientPutRequest(client, currentClientVersion(client.getId()));
        ClientDocumentPutRequest newNestedDocument = new ClientDocumentPutRequest();
        newNestedDocument.setNumber("DOC-CONSISTENT-AGGREGATE");
        newNestedDocument.setDescription("before");
        newNestedDocument.setExpirationDate(LocalDate.of(2028, 1, 1));
        createNested.setDocuments(List.of(newNestedDocument));
        var aggregateCreated = clientService.update(client.getId(), createNested);
        var aggregate = aggregateCreated.documents().stream()
                .filter(document -> document.number().equals("DOC-CONSISTENT-AGGREGATE"))
                .findFirst().orElseThrow();

        var standalonePut = documentService.update(client.getId(), standalone.id(),
                currentClientVersion(client.getId()),
                new DocumentPutRequest(standalone.version(), "DOC-CONSISTENT-PUT-S", "put", LocalDate.of(2030, 1, 1)));
        ClientPutRequest aggregatePutRequest = clientPutRequest(aggregateCreated, currentClientVersion(client.getId()));
        ClientDocumentPutRequest updateNestedDocument = new ClientDocumentPutRequest();
        updateNestedDocument.setId(aggregate.id());
        updateNestedDocument.setVersion(aggregate.version());
        updateNestedDocument.setNumber("DOC-CONSISTENT-PUT-A");
        updateNestedDocument.setDescription("put");
        updateNestedDocument.setExpirationDate(LocalDate.of(2030, 1, 1));
        aggregatePutRequest.setDocuments(List.of(updateNestedDocument));
        var aggregatePut = clientService.update(client.getId(), aggregatePutRequest).documents().stream()
                .filter(document -> document.id().equals(aggregate.id())).findFirst().orElseThrow();

        assertThat(standalonePut.id()).isEqualTo(standalone.id());
        assertThat(aggregatePut.id()).isEqualTo(aggregate.id());
        assertThat(standalonePut.clientId()).isEqualTo(client.getId());
        assertThat(aggregatePut.clientId()).isEqualTo(client.getId());
        assertThat(standalonePut.description()).isEqualTo(aggregatePut.description());
        assertThat(standalonePut.expirationDate()).isEqualTo(aggregatePut.expirationDate());

        DocumentPatchRequest standalonePatchRequest = new DocumentPatchRequest();
        standalonePatchRequest.setVersion(standalonePut.version());
        standalonePatchRequest.setNumber(PatchValue.of("DOC-CONSISTENT-PATCH-S"));
        var standalonePatch = documentService.patch(client.getId(), standalone.id(),
                currentClientVersion(client.getId()), standalonePatchRequest);

        ClientDocumentPatchRequest aggregatePatchDocument = new ClientDocumentPatchRequest();
        aggregatePatchDocument.setId(aggregatePut.id());
        aggregatePatchDocument.setVersion(aggregatePut.version());
        aggregatePatchDocument.setNumber(PatchValue.of("DOC-CONSISTENT-PATCH-A"));
        ClientPatchRequest aggregatePatchRequest = new ClientPatchRequest();
        aggregatePatchRequest.setVersion(currentClientVersion(client.getId()));
        aggregatePatchRequest.setDocuments(PatchValue.of(List.of(aggregatePatchDocument)));
        var aggregatePatch = clientService.patch(client.getId(), aggregatePatchRequest).documents().stream()
                .filter(document -> document.id().equals(aggregate.id())).findFirst().orElseThrow();

        assertThat(standalonePatch.id()).isEqualTo(standalone.id());
        assertThat(aggregatePatch.id()).isEqualTo(aggregate.id());
        assertThat(standalonePatch.clientId()).isEqualTo(client.getId());
        assertThat(aggregatePatch.clientId()).isEqualTo(client.getId());
        assertThat(standalonePatch.number()).isEqualTo("DOC-CONSISTENT-PATCH-S");
        assertThat(aggregatePatch.number()).isEqualTo("DOC-CONSISTENT-PATCH-A");
        assertThat(standalonePatch.description()).isEqualTo("put");
        assertThat(aggregatePatch.description()).isEqualTo("put");
        assertThat(standalonePatch.expirationDate()).isEqualTo(aggregatePatch.expirationDate());
        assertThat(documentRepository.findByClientId(client.getId(), PageRequest.of(0, 20)).getContent()).hasSize(2);
    }

    @Test
    void deleteRemovesOnlyTheRequestedDocument() {
        Client client = createClient();
        var first = documentService.create(client.getId(), client.getVersion(),
                new DocumentCreateRequest("DOC-DELETE", null, LocalDate.of(2028, 1, 1)));
        Long clientVersion = currentClientVersion(client.getId());
        var retained = documentService.create(client.getId(), clientVersion,
                new DocumentCreateRequest("DOC-RETAIN", null, LocalDate.of(2028, 1, 1)));

        documentService.delete(client.getId(), first.id(), currentClientVersion(client.getId()), first.version());

        assertThat(documentRepository.existsById(first.id())).isFalse();
        assertThat(documentRepository.existsById(retained.id())).isTrue();
    }

    @Test
    void missingClientAndDocumentReturnNotFound() {
        Client client = createClient();

        assertStatus(HttpStatus.NOT_FOUND,
                () -> documentService.create(Long.MAX_VALUE, 0L,
                        new DocumentCreateRequest("DOC-MISSING-CLIENT", null, LocalDate.of(2028, 1, 1))));
        assertStatus(HttpStatus.NOT_FOUND,
                () -> documentService.getById(client.getId(), Long.MAX_VALUE));
        assertStatus(HttpStatus.NOT_FOUND,
                () -> documentService.list(Long.MAX_VALUE, PageRequest.of(0, 20)));
    }

    @Test
    void duplicateDocumentNumberReturnsConflict() {
        Client client = createClient();
        documentService.create(client.getId(), client.getVersion(),
                new DocumentCreateRequest("DOC-DUPLICATE", null, LocalDate.of(2028, 1, 1)));

        assertStatus(HttpStatus.CONFLICT,
                () -> documentService.create(client.getId(), currentClientVersion(client.getId()),
                        new DocumentCreateRequest("DOC-DUPLICATE", null, LocalDate.of(2028, 1, 1))));
    }

    @Test
    void staleVersionsConflictAndNestedAccessDoesNotTransferOwnership() {
        Client owner = createClient();
        Client other = createClient();
        var document = documentService.create(owner.getId(), owner.getVersion(),
                new DocumentCreateRequest("DOC-OWNER", null, LocalDate.of(2028, 1, 1)));

        assertStatus(HttpStatus.CONFLICT,
                () -> documentService.create(owner.getId(), owner.getVersion(),
                        new DocumentCreateRequest("DOC-STALE-CLIENT", null, LocalDate.of(2028, 1, 1))));
        assertStatus(HttpStatus.CONFLICT,
                () -> documentService.update(owner.getId(), document.id(), currentClientVersion(owner.getId()),
                        new DocumentPutRequest(document.version() + 1, "DOC-STALE", null,
                                LocalDate.of(2029, 1, 1))));
        assertStatus(HttpStatus.NOT_FOUND,
                () -> documentService.getById(other.getId(), document.id()));
    }

    @ParameterizedTest
    @EnumSource(InvalidVersion.class)
    void missingOrNegativeExpectedVersionsReturnBadRequest(InvalidVersion invalidVersion) {
        Client client = createClient();
        var document = documentService.create(client.getId(), client.getVersion(),
                new DocumentCreateRequest("DOC-INVALID-VERSION-" + invalidVersion, null,
                        LocalDate.of(2028, 1, 1)));
        Long expectedClientVersion = currentClientVersion(client.getId());

        switch (invalidVersion) {
            case MISSING_CLIENT -> assertStatus(HttpStatus.BAD_REQUEST,
                    () -> documentService.create(client.getId(), null,
                            new DocumentCreateRequest("DOC-MISSING-CLIENT-VERSION", null,
                                    LocalDate.of(2028, 1, 1))));
            case NEGATIVE_CLIENT -> assertStatus(HttpStatus.BAD_REQUEST,
                    () -> documentService.create(client.getId(), -1L,
                            new DocumentCreateRequest("DOC-NEGATIVE-CLIENT-VERSION", null,
                                    LocalDate.of(2028, 1, 1))));
            case MISSING_DOCUMENT -> assertStatus(HttpStatus.BAD_REQUEST,
                    () -> documentService.delete(client.getId(), document.id(), expectedClientVersion, null));
            case NEGATIVE_DOCUMENT -> assertStatus(HttpStatus.BAD_REQUEST,
                    () -> documentService.delete(client.getId(), document.id(), expectedClientVersion, -1L));
        }
    }

    private Client createClient() {
        long suffix = CLIENT_SEQUENCE.incrementAndGet();
        return clientRepository.saveAndFlush(
                new Client("Ada", "Lovelace", "TAX-DOC-" + suffix,
                        "ada" + suffix + "@example.com", "123"));
    }

    private Long currentClientVersion(Long clientId) {
        return clientRepository.findById(clientId).orElseThrow().getVersion();
    }

    private ClientPutRequest clientPutRequest(Client client, Long version) {
        ClientPutRequest request = new ClientPutRequest();
        request.setFirstName(client.getFirstName());
        request.setLastName(client.getLastName());
        request.setTaxIdentifier(client.getTaxIdentifier());
        request.setEmail(client.getEmail());
        request.setPhoneNumber(client.getPhoneNumber());
        request.setVersion(version);
        return request;
    }

    private ClientPutRequest clientPutRequest(com.humanit.challenge.dto.client.ClientResponse client, Long version) {
        ClientPutRequest request = new ClientPutRequest();
        request.setFirstName(client.firstName());
        request.setLastName(client.lastName());
        request.setTaxIdentifier(client.taxIdentifier());
        request.setEmail(client.email());
        request.setPhoneNumber(client.phoneNumber());
        request.setVersion(version);
        return request;
    }

    private void assertStatus(HttpStatus status, Runnable action) {
        assertThatThrownBy(action::run)
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(status));
    }

    private enum InvalidVersion {
        MISSING_CLIENT,
        NEGATIVE_CLIENT,
        MISSING_DOCUMENT,
        NEGATIVE_DOCUMENT
    }
}
