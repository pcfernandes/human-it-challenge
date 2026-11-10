package com.humanit.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.humanit.challenge.domain.Client;
import com.humanit.challenge.dto.client.ClientCreateRequest;
import com.humanit.challenge.dto.client.ClientDocumentPutRequest;
import com.humanit.challenge.dto.client.ClientPatchRequest;
import com.humanit.challenge.dto.client.ClientPutRequest;
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
}
