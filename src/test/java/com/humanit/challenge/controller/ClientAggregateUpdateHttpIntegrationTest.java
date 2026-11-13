package com.humanit.challenge.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.EntityManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;

import com.humanit.challenge.config.StartupTestHarness;
import com.humanit.challenge.dto.client.ClientCreateRequest;
import com.humanit.challenge.dto.document.DocumentCreateRequest;
import com.humanit.challenge.dto.client.ClientResponse;
import com.humanit.challenge.repository.ClientRepository;
import com.humanit.challenge.repository.DocumentRepository;
import com.humanit.challenge.service.ClientService;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:client-aggregate-http-test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
})
@AutoConfigureMockMvc(addFilters = false)
class ClientAggregateUpdateHttpIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClientService clientService;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void supplyExternals(DynamicPropertyRegistry registry) {
        StartupTestHarness.supplyDevExternals(registry);
    }

    @Test
    void putWithUnknownNestedDocumentIdReturns404ProblemDetails() throws Exception {
        ClientResponse client = createClientWithDocument("TAX-HTTP-UNKNOWN", "DOC-HTTP-UNKNOWN");

        mockMvc.perform(put("/api/clients/{id}", client.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(putBody(client, Long.MAX_VALUE, 0L, "DOC-HTTP-UNKNOWN-REQUEST")))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("about:blank"))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Document not found"))
                .andExpect(jsonPath("$.instance").value("/api/clients/" + client.id()));
    }

    @Test
    void putWithDocumentOwnedByAnotherClientReturns409ProblemDetails() throws Exception {
        ClientResponse target = createClientWithDocument("TAX-HTTP-TARGET", "DOC-HTTP-TARGET");
        ClientResponse owner = createClientWithDocument("TAX-HTTP-OWNER", "DOC-HTTP-OWNER");
        var foreignDocument = owner.documents().get(0);

        mockMvc.perform(put("/api/clients/{id}", target.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(putBody(target, foreignDocument.id(), foreignDocument.version(),
                                "DOC-HTTP-CROSS-REQUEST")))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("about:blank"))
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("Document does not belong to client"))
                .andExpect(jsonPath("$.instance").value("/api/clients/" + target.id()));
    }

    @Test
    void getDocumentUnderNonOwningClientReturns404ProblemDetails() throws Exception {
        ClientResponse nonOwner = createClientWithDocument("TAX-HTTP-NON-OWNER", "DOC-HTTP-NON-OWNER");
        ClientResponse owner = createClientWithDocument("TAX-HTTP-DOCUMENT-OWNER", "DOC-HTTP-OWNED");
        var foreignDocument = owner.documents().get(0);

        mockMvc.perform(get("/api/clients/{clientId}/documents/{documentId}", nonOwner.id(), foreignDocument.id()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("about:blank"))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Document not found"))
                .andExpect(jsonPath("$.instance").value(
                        "/api/clients/" + nonOwner.id() + "/documents/" + foreignDocument.id()));
    }

    @Test
    void postWithDuplicateClientTaxIdentifierReturns409WithoutPartialPersistence() throws Exception {
        String taxIdentifier = "TAX-POST-DUPLICATE-CLIENT";
        ClientResponse existingClient = createClientWithDocument(taxIdentifier, "DOC-DUPLICATE-CLIENT-EXISTING");
        String attemptedDocumentNumber = "DOC-DUPLICATE-CLIENT-ROLLBACK";

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Grace","lastName":"Hopper","taxIdentifier":"%s",
                                 "email":"duplicate-client@example.com","phoneNumber":"456",
                                 "documents":[{"number":"%s","description":"must roll back",
                                                "expirationDate":"2031-01-01"}]}
                                """.formatted(taxIdentifier, attemptedDocumentNumber)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("about:blank"))
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.status").value(409));

        entityManager.clear();
        assertThat(clientRepository.findByTaxIdentifier(taxIdentifier))
                .get()
                .extracting(client -> client.getId())
                .isEqualTo(existingClient.id());
        assertThat(documentRepository.findByNumber(attemptedDocumentNumber)).isEmpty();
        assertThat(documentRepository.findByNumber("DOC-DUPLICATE-CLIENT-EXISTING")).isPresent();
    }

    @Test
    void postWithInitialDocumentsNeedsNoExpectedClientVersionAndUsesJpaVersion() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Ada","lastName":"Lovelace","taxIdentifier":"TAX-INITIAL-DOCS",
                                 "email":"initial-docs@example.com","phoneNumber":"123",
                                 "documents":[{"number":"DOC-INITIAL","description":"initial",
                                                "expirationDate":"2030-01-01"}]}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        ClientResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), ClientResponse.class);
        var persisted = clientRepository.findById(response.id()).orElseThrow();
        assertThat(response.version()).isEqualTo(persisted.getVersion());
        assertThat(response.documents()).hasSize(1);
        assertThat(documentRepository.findByNumber("DOC-INITIAL")).isPresent();
    }

    @Test
    void postWithDuplicateNestedDocumentNumbersRollsBackClientAndDocuments() throws Exception {
        String duplicateNumber = "DOC-POST-DUPLICATE-ROLLBACK";
        String taxIdentifier = "TAX-POST-DUPLICATE-ROLLBACK";

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Ada","lastName":"Lovelace","taxIdentifier":"%s",
                                 "email":"post-duplicate-rollback@example.com","phoneNumber":"123",
                                 "documents":[
                                   {"number":"%s","description":"first","expirationDate":"2030-01-01"},
                                   {"number":"%s","description":"second","expirationDate":"2031-01-01"}
                                 ]}
                                """.formatted(taxIdentifier, duplicateNumber, duplicateNumber)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("about:blank"))
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.status").value(409));

        entityManager.clear();
        assertThat(clientRepository.findByTaxIdentifier(taxIdentifier)).isEmpty();
        assertThat(documentRepository.findByNumber(duplicateNumber)).isEmpty();
    }

    @Test
    void nestedOnlyPutAndPatchReturnPersistedClientVersion() throws Exception {
        ClientResponse created = createClientWithDocument("TAX-RESPONSE-VERSION", "DOC-RESPONSE-VERSION");
        var originalDocument = created.documents().get(0);

        MvcResult putResult = mockMvc.perform(put("/api/clients/{id}", created.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(putBody(created, originalDocument.id(), originalDocument.version(),
                                "DOC-RESPONSE-VERSION-PUT")))
                .andExpect(status().isOk())
                .andReturn();
        ClientResponse putResponse = objectMapper.readValue(putResult.getResponse().getContentAsString(),
                ClientResponse.class);
        assertThat(putResponse.version()).isGreaterThan(created.version());
        assertThat(putResponse.version()).isEqualTo(
                clientRepository.findById(created.id()).orElseThrow().getVersion());

        var putDocument = putResponse.documents().stream()
                .filter(document -> document.id().equals(originalDocument.id())).findFirst().orElseThrow();
        MvcResult patchResult = mockMvc.perform(patch("/api/clients/{id}", created.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"version":%d,"documents":[{"id":%d,"version":%d,"description":"patched"}]}
                                """.formatted(putResponse.version(), putDocument.id(), putDocument.version())))
                .andExpect(status().isOk())
                .andReturn();
        ClientResponse patchResponse = objectMapper.readValue(patchResult.getResponse().getContentAsString(),
                ClientResponse.class);
        assertThat(patchResponse.version()).isGreaterThan(putResponse.version());
        assertThat(patchResponse.version()).isEqualTo(
                clientRepository.findById(created.id()).orElseThrow().getVersion());
    }

    @Test
    void clientDeleteEnforcesVersionAndDoesNotDeleteOnInvalidOrStalePrecondition() throws Exception {
        ClientResponse created = createClientWithDocument("TAX-DELETE-HTTP", "DOC-DELETE-HTTP");

        mockMvc.perform(delete("/api/clients/{id}", created.id()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        mockMvc.perform(delete("/api/clients/{id}", created.id()).header("X-Client-Version", "bad"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        mockMvc.perform(delete("/api/clients/{id}", created.id()).header("X-Client-Version", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        mockMvc.perform(delete("/api/clients/{id}", created.id())
                        .header("X-Client-Version", created.version() + 1))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(409));

        assertThat(clientRepository.existsById(created.id())).isTrue();
        assertThat(documentRepository.existsById(created.documents().get(0).id())).isTrue();

        mockMvc.perform(delete("/api/clients/{id}", created.id())
                        .header("X-Client-Version", created.version()))
                .andExpect(status().isNoContent());
        assertThat(clientRepository.existsById(created.id())).isFalse();
        assertThat(documentRepository.existsById(created.documents().get(0).id())).isFalse();
    }

    @Test
    void clientDeletePreservesNotFoundBehavior() throws Exception {
        mockMvc.perform(delete("/api/clients/{id}", Long.MAX_VALUE).header("X-Client-Version", "0"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void staleNestedDocumentVersionRollsBackEarlierAggregateChanges() throws Exception {
        ClientResponse created = createClientWithDocuments("TAX-ROLLBACK-CONFLICT",
                "DOC-ROLLBACK-FIRST", "DOC-ROLLBACK-SECOND");
        var first = created.documents().get(0);
        var second = created.documents().get(1);
        String request = """
                {
                  "firstName":"Changed but rolled back",
                  "lastName":"%s","taxIdentifier":"%s","email":"%s","phoneNumber":"%s",
                  "version":%d,
                  "documents":[
                    {"id":%d,"version":%d,"number":"DOC-ROLLBACK-UPDATED",
                     "description":"attempted change","expirationDate":"2031-01-01"},
                    {"id":%d,"version":%d,"number":"DOC-ROLLBACK-STALE",
                     "description":"stale change","expirationDate":"2032-01-01"}
                  ]
                }
                """.formatted(created.lastName(), created.taxIdentifier(), created.email(), created.phoneNumber(),
                created.version(), first.id(), first.version(), second.id(), second.version() + 1);

        mockMvc.perform(put("/api/clients/{id}", created.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(409));

        entityManager.clear();
        var reloadedClient = clientRepository.findById(created.id()).orElseThrow();
        assertThat(reloadedClient.getFirstName()).isEqualTo(created.firstName());
        var reloadedFirst = documentRepository.findById(first.id()).orElseThrow();
        var reloadedSecond = documentRepository.findById(second.id()).orElseThrow();
        assertThat(reloadedFirst.getNumber()).isEqualTo(first.number());
        assertThat(reloadedFirst.getDescription()).isEqualTo(first.description());
        assertThat(reloadedSecond.getNumber()).isEqualTo(second.number());
        assertThat(documentRepository.findByNumber("DOC-ROLLBACK-STALE")).isEmpty();
    }

    @Test
    void patchWithLaterStaleNestedDocumentRollsBackEarlierAggregateChanges() throws Exception {
        ClientResponse created = createClientWithDocuments("TAX-PATCH-ROLLBACK-CONFLICT",
                "DOC-PATCH-ROLLBACK-FIRST", "DOC-PATCH-ROLLBACK-SECOND");
        var first = created.documents().get(0);
        var second = created.documents().get(1);
        String request = """
                {
                  "firstName":"Changed but rolled back",
                  "lastName":"%s","taxIdentifier":"%s","email":"%s","phoneNumber":"%s",
                  "version":%d,
                  "documents":[
                    {"id":%d,"version":%d,"number":"DOC-PATCH-ROLLBACK-UPDATED",
                     "description":"attempted change","expirationDate":"2031-01-01"},
                    {"id":%d,"version":%d,"number":"DOC-PATCH-ROLLBACK-STALE",
                     "description":"stale change","expirationDate":"2032-01-01"}
                  ]
                }
                """.formatted(created.lastName(), created.taxIdentifier(), created.email(), created.phoneNumber(),
                created.version(), first.id(), first.version(), second.id(), second.version() + 1);

        mockMvc.perform(patch("/api/clients/{id}", created.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("about:blank"))
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.status").value(409));

        entityManager.clear();
        var reloadedClient = clientRepository.findById(created.id()).orElseThrow();
        assertThat(reloadedClient.getFirstName()).isEqualTo(created.firstName());
        assertThat(reloadedClient.getLastName()).isEqualTo(created.lastName());
        assertThat(reloadedClient.getTaxIdentifier()).isEqualTo(created.taxIdentifier());
        assertThat(reloadedClient.getEmail()).isEqualTo(created.email());
        assertThat(reloadedClient.getPhoneNumber()).isEqualTo(created.phoneNumber());
        assertThat(reloadedClient.getVersion()).isEqualTo(created.version());

        var reloadedFirst = documentRepository.findById(first.id()).orElseThrow();
        var reloadedSecond = documentRepository.findById(second.id()).orElseThrow();
        assertThat(reloadedFirst.getNumber()).isEqualTo(first.number());
        assertThat(reloadedFirst.getDescription()).isEqualTo(first.description());
        assertThat(reloadedFirst.getExpirationDate()).isEqualTo(first.expirationDate());
        assertThat(reloadedFirst.getVersion()).isEqualTo(first.version());
        assertThat(reloadedSecond.getNumber()).isEqualTo(second.number());
        assertThat(reloadedSecond.getDescription()).isEqualTo(second.description());
        assertThat(reloadedSecond.getExpirationDate()).isEqualTo(second.expirationDate());
        assertThat(reloadedSecond.getVersion()).isEqualTo(second.version());
        assertThat(documentRepository.findByNumber("DOC-PATCH-ROLLBACK-UPDATED")).isEmpty();
        assertThat(documentRepository.findByNumber("DOC-PATCH-ROLLBACK-STALE")).isEmpty();
    }

    private ClientResponse createClientWithDocument(String taxIdentifier, String documentNumber) {
        ClientCreateRequest request = new ClientCreateRequest();
        request.setFirstName("Ada");
        request.setLastName("Lovelace");
        request.setTaxIdentifier(taxIdentifier);
        request.setEmail(taxIdentifier.toLowerCase() + "@example.com");
        request.setPhoneNumber("123");
        request.setDocuments(List.of(new DocumentCreateRequest(
                documentNumber, "existing document", LocalDate.of(2030, 1, 1))));
        return clientService.create(request);
    }

    private ClientResponse createClientWithDocuments(String taxIdentifier, String... documentNumbers) {
        ClientCreateRequest request = new ClientCreateRequest();
        request.setFirstName("Ada");
        request.setLastName("Lovelace");
        request.setTaxIdentifier(taxIdentifier);
        request.setEmail(taxIdentifier.toLowerCase() + "@example.com");
        request.setPhoneNumber("123");
        request.setDocuments(java.util.Arrays.stream(documentNumbers)
                .map(number -> new DocumentCreateRequest(number, "initial " + number, LocalDate.of(2030, 1, 1)))
                .toList());
        return clientService.create(request);
    }

    private String putBody(ClientResponse client, Long documentId, Long documentVersion, String documentNumber) {
        return """
                {
                  "firstName":"%s",
                  "lastName":"%s",
                  "taxIdentifier":"%s",
                  "email":"%s",
                  "phoneNumber":"%s",
                  "version":%d,
                  "documents":[{
                    "id":%d,
                    "version":%d,
                    "number":"%s",
                    "description":"attempted aggregate update",
                    "expirationDate":"2031-01-01"
                  }]
                }
                """.formatted(client.firstName(), client.lastName(), client.taxIdentifier(), client.email(),
                client.phoneNumber(), client.version(), documentId, documentVersion, documentNumber);
    }
}
