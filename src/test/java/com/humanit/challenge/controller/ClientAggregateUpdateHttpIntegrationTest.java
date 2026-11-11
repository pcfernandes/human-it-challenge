package com.humanit.challenge.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.humanit.challenge.config.StartupTestHarness;
import com.humanit.challenge.dto.client.ClientCreateRequest;
import com.humanit.challenge.dto.document.DocumentCreateRequest;
import com.humanit.challenge.dto.client.ClientResponse;
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
