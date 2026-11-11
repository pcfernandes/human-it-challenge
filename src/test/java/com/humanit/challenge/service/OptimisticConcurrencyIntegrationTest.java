package com.humanit.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.humanit.challenge.config.StartupTestHarness;
import com.humanit.challenge.domain.Client;
import com.humanit.challenge.dto.client.ClientCreateRequest;
import com.humanit.challenge.dto.client.ClientDocumentPutRequest;
import com.humanit.challenge.dto.client.ClientPutRequest;
import com.humanit.challenge.dto.document.DocumentCreateRequest;
import com.humanit.challenge.dto.document.DocumentPutRequest;
import com.humanit.challenge.repository.ClientRepository;
import com.humanit.challenge.repository.DocumentRepository;
import jakarta.persistence.EntityManager;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:optimistic-concurrency-test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
})
class OptimisticConcurrencyIntegrationTest {

    private static final AtomicLong SEQUENCE = new AtomicLong();

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private ClientService clientService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private EntityManager entityManager;

    @DynamicPropertySource
    static void supplyExternals(DynamicPropertyRegistry registry) {
        StartupTestHarness.supplyDevExternals(registry);
    }

    @Test
    void competingDifferentDocumentWritersWithSameClientVersionHaveOneWinnerAndNoPartialWrite() throws Exception {
        var fixture = createClientWithDocuments("DOC-A", "DOC-B");
        var first = fixture.documents().get(0);
        var second = fixture.documents().get(1);

        var validationGate = new ValidationGate();
        List<Outcome> outcomes = competeAfterVersionValidation(fixture.id(), fixture.version(), validationGate,
                (version, writer) -> {
                    var target = writer == 0 ? first : second;
                    ClientPutRequest request = putRequest(fixture, version);
                    ClientDocumentPutRequest nested = new ClientDocumentPutRequest();
                    nested.setId(target.id());
                    nested.setVersion(target.version());
                    nested.setNumber("DOC-WINNER-" + writer);
                    nested.setDescription("writer " + writer);
                    nested.setExpirationDate(LocalDate.of(2035, 1, 1));
                    request.setDocuments(List.of(nested));
                    return clientService.update(fixture.id(), request);
                });

        assertThat(validationGate.observedVersions()).containsExactly(fixture.version(), fixture.version());
        assertOneWinnerAndOptimisticLockConflict(outcomes);
        var persistedFirst = documentRepository.findById(first.id()).orElseThrow();
        var persistedSecond = documentRepository.findById(second.id()).orElseThrow();
        assertThat(List.of(persistedFirst.getNumber(), persistedSecond.getNumber()).stream()
                .filter(value -> value.startsWith("DOC-WINNER-")).count()).isEqualTo(1);
        int winningWriter = outcomes.get(0).succeeded() ? 0 : 1;
        var winningDocument = winningWriter == 0 ? persistedFirst : persistedSecond;
        var losingDocument = winningWriter == 0 ? persistedSecond : persistedFirst;
        assertThat(winningDocument.getNumber()).isEqualTo("DOC-WINNER-" + winningWriter);
        assertThat(winningDocument.getDescription()).isEqualTo("writer " + winningWriter);
        assertThat(winningDocument.getExpirationDate()).isEqualTo(LocalDate.of(2035, 1, 1));
        assertThat(losingDocument.getNumber()).startsWith(winningWriter == 0 ? "DOC-B-" : "DOC-A-");
        assertThat(losingDocument.getDescription()).isEqualTo("initial");
        assertThat(clientRepository.findById(fixture.id()).orElseThrow().getVersion()).isGreaterThan(fixture.version());
    }

    @Test
    void competingWritersOfSameDocumentConflictAndOnlyWinnerPersists() throws Exception {
        var fixture = createClientWithDocuments("DOC-SAME");
        var document = fixture.documents().get(0);

        List<Outcome> outcomes = compete(fixture.id(), fixture.version(), (clientVersion, writer) -> documentService.update(
                fixture.id(), document.id(), clientVersion,
                new DocumentPutRequest(document.version(), "DOC-SAME-WINNER-" + writer,
                        "writer " + writer, LocalDate.of(2036, 1, 1))));

        assertOneWinnerAndConflict(outcomes);
        assertThat(documentRepository.findById(document.id()).orElseThrow().getNumber())
                .startsWith("DOC-SAME-WINNER-");
    }

    @Test
    void competingScalarWritersWithSameClientVersionConflictAndOnlyWinnerPersists() throws Exception {
        var fixture = createClientWithDocuments();

        List<Outcome> outcomes = compete(fixture.id(), fixture.version(), (version, writer) -> {
            ClientPutRequest request = putRequest(fixture, version);
            request.setFirstName("Scalar winner " + writer);
            return clientService.update(fixture.id(), request);
        });

        assertOneWinnerAndConflict(outcomes);
        assertThat(clientRepository.findById(fixture.id()).orElseThrow().getFirstName())
                .startsWith("Scalar winner ");
    }

    private List<Outcome> compete(Long clientId, Long expectedClientVersion,
            BiFunction<Long, Integer, ?> mutation) throws Exception {
        return compete(clientId, expectedClientVersion, mutation, null);
    }

    private List<Outcome> competeAfterVersionValidation(Long clientId, Long expectedClientVersion,
            ValidationGate validationGate, BiFunction<Long, Integer, ?> mutation) throws Exception {
        return compete(clientId, expectedClientVersion, mutation, validationGate);
    }

    private List<Outcome> compete(Long clientId, Long expectedClientVersion,
            BiFunction<Long, Integer, ?> mutation, ValidationGate validationGate) throws Exception {
        CountDownLatch transactionsReady = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        List<Future<Outcome>> futures = new ArrayList<>();
        try {
            for (int writer = 0; writer < 2; writer++) {
                final int writerId = writer;
                futures.add(executor.submit(() -> {
                    try {
                        Object result = transaction.execute(status -> {
                            transactionsReady.countDown();
                            await(start);
                            if (validationGate != null) {
                                Client loaded = clientRepository.findById(clientId).orElseThrow();
                                validationGate.observedVersions().add(loaded.getVersion());
                                if (!expectedClientVersion.equals(loaded.getVersion())) {
                                    throw new IllegalStateException("Client version changed before both writers validated");
                                }
                                validationGate.validatedWriters().countDown();
                                await(validationGate.release());
                                entityManager.clear();
                            }
                            return mutation.apply(expectedClientVersion, writerId);
                        });
                        return Outcome.success(result);
                    } catch (RuntimeException exception) {
                        return Outcome.failure(exception);
                    }
                }));
            }
            assertThat(transactionsReady.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            if (validationGate != null) {
                assertThat(validationGate.validatedWriters().await(10, TimeUnit.SECONDS)).isTrue();
                assertThat(validationGate.observedVersions()).hasSize(2);
                assertThat(validationGate.observedVersions())
                        .containsExactly(expectedClientVersion, expectedClientVersion);
                validationGate.release().countDown();
            }
            return List.of(futures.get(0).get(20, TimeUnit.SECONDS), futures.get(1).get(20, TimeUnit.SECONDS));
        } finally {
            start.countDown();
            if (validationGate != null) {
                validationGate.release().countDown();
            }
            executor.shutdownNow();
            assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
        }
    }

    private void assertOneWinnerAndConflict(List<Outcome> outcomes) {
        assertThat(outcomes).filteredOn(Outcome::succeeded).hasSize(1);
        Outcome losing = outcomes.stream().filter(outcome -> !outcome.succeeded()).findFirst().orElseThrow();
        assertThat(isConflict(losing.failure())).isTrue();
    }

    private void assertOneWinnerAndOptimisticLockConflict(List<Outcome> outcomes) {
        assertThat(outcomes).hasSize(2).filteredOn(Outcome::succeeded).hasSize(1);
        Outcome losing = outcomes.stream().filter(outcome -> !outcome.succeeded()).findFirst().orElseThrow();
        assertThat(hasOptimisticLockCause(losing.failure())).isTrue();
    }

    private boolean hasOptimisticLockCause(Throwable failure) {
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof OptimisticLockingFailureException
                    || cause instanceof jakarta.persistence.OptimisticLockException) {
                return true;
            }
        }
        return false;
    }

    private boolean isConflict(Throwable failure) {
        if (failure instanceof ResponseStatusException statusException) {
            return statusException.getStatusCode().equals(HttpStatus.CONFLICT);
        }
        return failure instanceof OptimisticLockingFailureException
                || failure instanceof jakarta.persistence.OptimisticLockException;
    }

    private void await(CountDownLatch latch) {
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting for concurrent writer start");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while coordinating concurrent writers", exception);
        }
    }

    private com.humanit.challenge.dto.client.ClientResponse createClientWithDocuments(String... numbers) {
        long suffix = SEQUENCE.incrementAndGet();
        ClientCreateRequest create = new ClientCreateRequest();
        create.setFirstName("Concurrency");
        create.setLastName("Test");
        create.setTaxIdentifier("TAX-CONCURRENCY-" + suffix);
        create.setEmail("concurrency" + suffix + "@example.com");
        create.setPhoneNumber("123");
        create.setDocuments(java.util.Arrays.stream(numbers)
                .map(number -> new DocumentCreateRequest(number + "-" + suffix, "initial", LocalDate.of(2030, 1, 1)))
                .toList());
        return clientService.create(create);
    }

    private ClientPutRequest putRequest(com.humanit.challenge.dto.client.ClientResponse client, Long version) {
        ClientPutRequest request = new ClientPutRequest();
        request.setFirstName(client.firstName());
        request.setLastName(client.lastName());
        request.setTaxIdentifier(client.taxIdentifier());
        request.setEmail(client.email());
        request.setPhoneNumber(client.phoneNumber());
        request.setVersion(version);
        return request;
    }

    private record Outcome(Object result, Throwable failure) {
        static Outcome success(Object result) {
            return new Outcome(result, null);
        }

        static Outcome failure(Throwable failure) {
            return new Outcome(null, failure);
        }

        boolean succeeded() {
            return failure == null;
        }
    }

    private static final class ValidationGate {
        private final CountDownLatch validatedWriters = new CountDownLatch(2);
        private final CountDownLatch release = new CountDownLatch(1);
        private final ConcurrentLinkedQueue<Long> observedVersions = new ConcurrentLinkedQueue<>();

        CountDownLatch validatedWriters() {
            return validatedWriters;
        }

        CountDownLatch release() {
            return release;
        }

        ConcurrentLinkedQueue<Long> observedVersions() {
            return observedVersions;
        }
    }
}
