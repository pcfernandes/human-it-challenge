package com.humanit.challenge.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanit.challenge.config.StartupTestHarness;
import com.humanit.challenge.dto.client.ClientCreateRequest;
import com.humanit.challenge.dto.client.ClientResponse;
import com.humanit.challenge.dto.document.DocumentCreateRequest;
import com.humanit.challenge.repository.DocumentRepository;
import com.humanit.challenge.service.ClientService;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:n-plus-one-query-count-test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.jpa.properties.hibernate.generate_statistics=true"
})
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class NPlusOneQueryCountTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClientService clientService;

    @MockitoSpyBean
    private DocumentRepository documentRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void supplyExternals(DynamicPropertyRegistry registry) {
        StartupTestHarness.supplyDevExternals(registry);
    }

    @Test
    void clientPagesUseAtMostThreeStatementsAndOneBulkDocumentQuery() throws Exception {
        List<Long> clientIds = new ArrayList<>();
        Map<Long, ClientResponse> expectedClients = new HashMap<>();
        for (int clientIndex = 0; clientIndex < 100; clientIndex++) {
            int documentCount = clientIndex == 0 ? 5 : clientIndex == 1 ? 2 : clientIndex == 2 ? 0 : 1;
            List<DocumentCreateRequest> documents = new ArrayList<>();
            for (int documentIndex = 0; documentIndex < documentCount; documentIndex++) {
                String number = "PERF-DOC-" + clientIndex + "-" + documentIndex;
                documents.add(new DocumentCreateRequest(number, "diagnostic document " + documentIndex,
                        LocalDate.of(2030, 1, 1).plusDays(documentIndex)));
            }

            ClientCreateRequest request = new ClientCreateRequest();
            request.setFirstName("Perf");
            request.setLastName("Client " + clientIndex);
            request.setTaxIdentifier("PERF-TAX-" + clientIndex);
            request.setEmail("perf-client-" + clientIndex + "@example.com");
            request.setPhoneNumber("123");
            request.setDocuments(documents);
            ClientResponse created = clientService.create(request);
            clientIds.add(created.id());
            expectedClients.put(created.id(), created);
        }

        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        assertThat(statistics.isStatisticsEnabled()).isTrue();

        for (int clientCount : List.of(1, 5, 20, 100)) {
            entityManager.flush();
            entityManager.clear();
            statistics.clear();
            clearInvocations(documentRepository);

            MvcResult result = mockMvc.perform(get("/api/clients").param("page", "0")
                            .param("size", Integer.toString(clientCount)))
                    .andExpect(status().isOk())
                    .andReturn();
            long sqlCount = statistics.getPrepareStatementCount();

            JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
            JsonNode content = response.path("content");
            assertThat(content.size()).isEqualTo(clientCount);
            assertThat(sqlCount).as("SQL statements for page size %d", clientCount).isLessThanOrEqualTo(3);
            assertThat(response.path("page").asInt()).isZero();
            assertThat(response.path("size").asInt()).isEqualTo(clientCount);
            assertThat(response.path("totalElements").asLong()).isEqualTo(100);
            assertThat(response.path("totalPages").asInt()).isEqualTo(100 / clientCount);
            assertThat(content).extracting(node -> node.path("id").asLong())
                    .containsExactlyElementsOf(clientIds.subList(0, clientCount));

            for (int index = 0; index < content.size(); index++) {
                JsonNode client = content.get(index);
                ClientResponse expected = expectedClients.get(clientIds.get(index));
                assertThat(client.path("firstName").asText()).isEqualTo(expected.firstName());
                assertThat(client.path("lastName").asText()).isEqualTo(expected.lastName());
                assertThat(client.path("taxIdentifier").asText()).isEqualTo(expected.taxIdentifier());
                assertThat(client.path("email").asText()).isEqualTo(expected.email());
                assertThat(client.path("phoneNumber").asText()).isEqualTo(expected.phoneNumber());
                assertThat(client.path("version").asLong()).isEqualTo(expected.version());
                JsonNode documentNodes = client.path("documents");
                assertThat(documentNodes.size()).isEqualTo(expected.documents().size());
                for (int documentIndex = 0; documentIndex < expected.documents().size(); documentIndex++) {
                    var expectedDocument = expected.documents().get(documentIndex);
                    JsonNode actualDocument = documentNodes.get(documentIndex);
                    assertThat(actualDocument.path("id").asLong()).isEqualTo(expectedDocument.id());
                    assertThat(actualDocument.path("clientId").asLong()).isEqualTo(expected.id());
                    assertThat(actualDocument.path("number").asText()).isEqualTo(expectedDocument.number());
                    assertThat(actualDocument.path("description").asText()).isEqualTo(expectedDocument.description());
                    assertThat(actualDocument.path("expirationDate").asText())
                            .isEqualTo(expectedDocument.expirationDate().toString());
                    assertThat(actualDocument.path("version").asLong()).isEqualTo(expectedDocument.version());
                }
            }

            verify(documentRepository, times(1)).findByClient_IdInOrderByIdAsc(argThat(ids ->
                    ids.equals(clientIds.subList(0, clientCount))));
            System.out.printf("Hibernate SQL count [GET /api/clients, clients=%d]: %d%n", clientCount, sqlCount);
        }

        entityManager.flush();
        entityManager.clear();
        statistics.clear();
        clearInvocations(documentRepository);
        MvcResult secondPageResult = mockMvc.perform(get("/api/clients").param("page", "1").param("size", "20"))
                .andExpect(status().isOk())
                .andReturn();
        long secondPageSqlCount = statistics.getPrepareStatementCount();
        JsonNode secondPage = objectMapper.readTree(secondPageResult.getResponse().getContentAsString());
        assertThat(secondPageSqlCount).isLessThanOrEqualTo(3);
        assertThat(secondPage.path("page").asInt()).isEqualTo(1);
        assertThat(secondPage.path("size").asInt()).isEqualTo(20);
        assertThat(secondPage.path("totalElements").asLong()).isEqualTo(100);
        assertThat(secondPage.path("totalPages").asInt()).isEqualTo(5);
        assertThat(secondPage.path("content")).hasSize(20);
        assertThat(secondPage.path("content")).extracting(node -> node.path("id").asLong())
                .containsExactlyElementsOf(clientIds.subList(20, 40));
        verify(documentRepository, times(1)).findByClient_IdInOrderByIdAsc(argThat(ids ->
                ids.equals(clientIds.subList(20, 40))));
        System.out.printf("Hibernate SQL count [GET /api/clients, page=1,size=20]: %d%n", secondPageSqlCount);

        entityManager.flush();
        entityManager.clear();
        statistics.clear();
        clearInvocations(documentRepository);
        MvcResult emptyPageResult = mockMvc.perform(get("/api/clients").param("page", "5").param("size", "20"))
                .andExpect(status().isOk())
                .andReturn();
        long emptyPageSqlCount = statistics.getPrepareStatementCount();
        JsonNode emptyPage = objectMapper.readTree(emptyPageResult.getResponse().getContentAsString());
        assertThat(emptyPage.path("content").size()).isZero();
        assertThat(emptyPage.path("page").asInt()).isEqualTo(5);
        assertThat(emptyPage.path("size").asInt()).isEqualTo(20);
        assertThat(emptyPage.path("totalElements").asLong()).isEqualTo(100);
        assertThat(emptyPage.path("totalPages").asInt()).isEqualTo(5);
        verify(documentRepository, times(0)).findByClient_IdInOrderByIdAsc(anyCollection());
        System.out.printf("Hibernate SQL count [GET /api/clients, empty page]: %d%n", emptyPageSqlCount);
    }
}
