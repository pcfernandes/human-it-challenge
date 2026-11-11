package com.humanit.challenge.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.humanit.challenge.config.StartupTestHarness;
import com.humanit.challenge.domain.Client;
import com.humanit.challenge.dto.document.DocumentCreateRequest;
import com.humanit.challenge.repository.ClientRepository;
import com.humanit.challenge.repository.DocumentRepository;
import com.humanit.challenge.service.DocumentService;

/** Exercises an optimistic-lock exception raised after the service body returns, during transaction commit. */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:commit-optimistic-lock-test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
})
@AutoConfigureMockMvc(addFilters = false)
@Import(CommitTimeOptimisticLockIntegrationTest.CommitFailureController.class)
class CommitTimeOptimisticLockIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @DynamicPropertySource
    static void supplyExternals(DynamicPropertyRegistry registry) {
        StartupTestHarness.supplyDevExternals(registry);
    }

    @Test
    void commitTimeOptimisticLockFailureReturns409ProblemDetailsAndRollsBack() throws Exception {
        Client client = clientRepository.saveAndFlush(new Client("Commit", "Conflict", "TAX-COMMIT-LOCK",
                "commit-lock@example.com", "123"));

        mockMvc.perform(post("/__test/commit-conflict/{clientId}", client.getId())
                        .param("version", client.getVersion().toString()))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").value("Conflict"));

        assertThat(documentRepository.findByNumber("DOC-COMMIT-LOCK")).isEmpty();
    }

    @RestController
    @RequestMapping("/__test/commit-conflict")
    static class CommitFailureController {

        private final DocumentService documentService;

        CommitFailureController(DocumentService documentService) {
            this.documentService = documentService;
        }

        @PostMapping("/{clientId}")
        @Transactional
        void createThenFailAtCommit(@PathVariable Long clientId, @RequestParam Long version) {
            documentService.create(clientId, version,
                    new DocumentCreateRequest("DOC-COMMIT-LOCK", null, LocalDate.of(2028, 1, 1)));
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void beforeCommit(boolean readOnly) {
                    throw new org.springframework.orm.ObjectOptimisticLockingFailureException(Client.class, clientId);
                }
            });
        }
    }
}
