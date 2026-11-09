package com.humanit.challenge.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.JoinColumn;

import org.flywaydb.core.Flyway;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.humanit.challenge.domain.Client;
import com.humanit.challenge.domain.Document;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true"
})
class DomainPersistenceTest {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Flyway flyway;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void flywayAppliesVersionTwoAndHibernateValidatesTheMigratedSchema() {
        assertThat(flyway.info().applied())
                .anySatisfy(migration -> assertThat(migration.getVersion().getVersion()).isEqualTo("2"));
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables " +
                        "WHERE LOWER(table_name) IN ('clients', 'documents')",
                Integer.class)).isEqualTo(2);
    }

    @Test
    void taxIdentifierIsGloballyUnique() {
        clientRepository.saveAndFlush(client("tax-global"));
        Client duplicate = client("tax-global");

        assertThatThrownBy(() -> clientRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void clientRepositoryTaxIdentifierQueriesFindExistingAndMissingValuesWithoutChangingEntities() {
        Client client = clientRepository.saveAndFlush(client("tax-repository-lookup"));
        Long originalVersion = client.getVersion();

        assertThat(clientRepository.existsByTaxIdentifier("tax-repository-lookup")).isTrue();
        assertThat(clientRepository.existsByTaxIdentifier("tax-repository-missing")).isFalse();
        assertThat(clientRepository.findByTaxIdentifier("tax-repository-lookup"))
                .contains(client);
        assertThat(clientRepository.findByTaxIdentifier("tax-repository-missing")).isEmpty();
        entityManager.flush();
        entityManager.clear();

        Client reloaded = clientRepository.findById(client.getId()).orElseThrow();
        assertThat(reloaded.getVersion()).isEqualTo(originalVersion);
        assertThat(reloaded.getTaxIdentifier()).isEqualTo("tax-repository-lookup");
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void lockByIdForcesClientVersionIncrementOnTransactionCompletion() {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        Client saved = transaction.execute(status ->
                clientRepository.saveAndFlush(client("tax-force-lock")));
        Long clientId = saved.getId();
        Long originalVersion = saved.getVersion();

        transaction.executeWithoutResult(status -> {
            assertThat(clientRepository.lockById(clientId)).isPresent();
            assertThat(clientRepository.lockById(-1L)).isEmpty();
        });

        Client reloaded = clientRepository.findById(clientId).orElseThrow();
        assertThat(reloaded.getVersion()).isGreaterThan(originalVersion);
        assertThat(reloaded.getTaxIdentifier()).isEqualTo("tax-force-lock");
        assertThat(reloaded.getEmail()).isEqualTo("client@example.com");
    }

    @Test
    void documentRepositoryQueriesFilterByOwnerAndReturnDeterministicPages() {
        Client owner = clientRepository.saveAndFlush(client("tax-document-query-owner"));
        Client otherOwner = clientRepository.saveAndFlush(client("tax-document-query-other"));
        Document first = document("number-query-first");
        Document second = document("number-query-second");
        Document third = document("number-query-third");
        owner.addDocument(first);
        owner.addDocument(second);
        otherOwner.addDocument(document("number-query-other-owner"));
        owner.addDocument(third);
        clientRepository.flush();
        Long ownerId = owner.getId();
        Long otherOwnerId = otherOwner.getId();

        assertThat(documentRepository.existsByNumber("number-query-first")).isTrue();
        assertThat(documentRepository.existsByNumber("number-query-missing")).isFalse();
        assertThat(documentRepository.findByNumber("number-query-second")).contains(second);
        assertThat(documentRepository.findByNumber("number-query-missing")).isEmpty();
        assertThat(documentRepository.findByIdAndClientId(first.getId(), ownerId)).contains(first);
        assertThat(documentRepository.findByIdAndClientId(first.getId(), otherOwnerId)).isEmpty();
        assertThat(documentRepository.findByIdAndClientId(-1L, ownerId)).isEmpty();

        Sort ascendingId = Sort.by(Sort.Direction.ASC, "id");
        Page<Document> firstPage = documentRepository.findByClientId(
                ownerId, PageRequest.of(0, 2, ascendingId));
        Page<Document> secondPage = documentRepository.findByClientId(
                ownerId, PageRequest.of(1, 2, ascendingId));
        Page<Document> otherOwnersPage = documentRepository.findByClientId(
                otherOwnerId, PageRequest.of(0, 10, ascendingId));
        Page<Document> missingOwnerPage = documentRepository.findByClientId(
                -1L, PageRequest.of(0, 10, ascendingId));

        assertThat(firstPage.getContent()).containsExactly(first, second);
        assertThat(firstPage.getTotalElements()).isEqualTo(3);
        assertThat(secondPage.getContent()).containsExactly(third);
        assertThat(secondPage.getTotalElements()).isEqualTo(3);
        assertThat(otherOwnersPage.getContent()).extracting(Document::getNumber)
                .containsExactly("number-query-other-owner");
        assertThat(missingOwnerPage.getContent()).isEmpty();
        entityManager.clear();
        Document freshlyLoaded = documentRepository.findById(first.getId()).orElseThrow();
        assertThat(entityManagerFactory.getPersistenceUnitUtil().isLoaded(freshlyLoaded, "client"))
                .isFalse();
    }

    @Test
    void documentNumberIsGloballyUniqueAcrossDifferentClients() {
        Client firstClient = clientRepository.saveAndFlush(client("tax-first"));
        Client secondClient = clientRepository.saveAndFlush(client("tax-second"));
        firstClient.addDocument(document("number-global"));
        secondClient.addDocument(document("number-global"));

        assertThatThrownBy(() -> clientRepository.flush())
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void databaseRejectsDocumentWithoutAnExistingClient() {
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO documents (number, description, expiration_date, client_id, version)
                VALUES ('orphan', NULL, DATE '2030-01-01', -99, 0)
                """))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void deletingClientThroughJpaDeletesAssociatedDocuments() {
        Client client = client("tax-cascade-jpa");
        client.addDocument(document("number-cascade-jpa"));
        clientRepository.saveAndFlush(client);
        Long clientId = client.getId();
        Long documentId = client.getDocuments().get(0).getId();

        clientRepository.deleteById(clientId);
        clientRepository.flush();
        entityManager.clear();

        assertThat(clientRepository.findById(clientId)).isEmpty();
        assertThat(documentRepository.findById(documentId)).isEmpty();
    }

    @Test
    void databaseOnDeleteCascadeDeletesDocumentsWhenClientIsDeletedOutsideJpa() {
        Client client = client("tax-cascade-db");
        client.addDocument(document("number-cascade-db"));
        clientRepository.saveAndFlush(client);
        Long clientId = client.getId();
        Long documentId = client.getDocuments().get(0).getId();
        entityManager.clear();

        jdbcTemplate.update("DELETE FROM clients WHERE id = ?", clientId);
        entityManager.clear();

        assertThat(documentRepository.findById(documentId)).isEmpty();
    }

    @Test
    void removingDocumentFromCollectionDoesNotDeleteOrTransferIt() {
        Client client = client("tax-preserve-document");
        Document document = document("number-preserve-document");
        client.addDocument(document);
        clientRepository.saveAndFlush(client);
        Long documentId = document.getId();
        Long clientId = client.getId();

        assertThat(client.removeDocument(document)).isTrue();
        assertThat(document.getClient()).isSameAs(client);
        clientRepository.flush();
        entityManager.clear();

        Document persisted = documentRepository.findById(documentId).orElseThrow();
        assertThat(persisted.getClient().getId()).isEqualTo(clientId);
        assertThat(clientRepository.findById(clientId).orElseThrow().getDocuments()).contains(persisted);
    }

    @Test
    void removeDocumentHandlesAbsentNullAndTransientDocumentsWithoutDeletingOrReassigning() {
        Client client = client("tax-remove-edge-cases");
        Document transientDocument = document("number-remove-transient");
        Document unrelatedDocument = document("number-remove-unrelated");
        client.addDocument(transientDocument);

        assertThat(client.removeDocument(null)).isFalse();
        assertThat(client.removeDocument(unrelatedDocument)).isFalse();
        assertThat(client.removeDocument(transientDocument)).isTrue();
        assertThat(transientDocument.getClient()).isSameAs(client);
        assertThat(client.getDocuments()).isEmpty();
        assertThat(documentRepository.count()).isZero();
        assertThatThrownBy(() -> client.addDocument(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void bothEntitiesIncrementTheirVersionsAndRejectStaleUpdates() {
        Client client = clientRepository.saveAndFlush(client("tax-client-lock"));
        Long initialClientVersion = client.getVersion();
        client.setEmail("changed@example.com");
        clientRepository.flush();
        assertThat(client.getVersion()).isGreaterThan(initialClientVersion);

        Document document = document("number-document-lock");
        client.addDocument(document);
        clientRepository.flush();
        Long initialDocumentVersion = document.getVersion();
        document.setDescription("changed");
        documentRepository.flush();
        assertThat(document.getVersion()).isGreaterThan(initialDocumentVersion);
    }

    @Test
    void clientUsesOptimisticLockingForStaleUpdates() {
        Client client = clientRepository.saveAndFlush(client("tax-stale-client"));
        jdbcTemplate.update("UPDATE clients SET version = version + 1 WHERE id = ?", client.getId());
        client.setEmail("stale@example.com");

        assertThatThrownBy(() -> entityManager.flush())
                .isInstanceOf(OptimisticLockException.class);
    }

    @Test
    void documentUsesOptimisticLockingForStaleUpdates() {
        Client client = client("tax-stale-document");
        Document document = document("number-stale-document");
        client.addDocument(document);
        clientRepository.saveAndFlush(client);
        jdbcTemplate.update("UPDATE documents SET version = version + 1 WHERE id = ?", document.getId());
        document.setDescription("stale");

        assertThatThrownBy(() -> entityManager.flush())
                .isInstanceOf(OptimisticLockException.class);
    }

    @Test
    void equalityIsIdBasedStableForHashCollectionsAndDoesNotInitializeAssociations() {
        Client firstUnsaved = client("tax-equality-one");
        Client secondUnsaved = client("tax-equality-two");
        int hashBeforePersistence = firstUnsaved.hashCode();
        assertThat(firstUnsaved).isNotEqualTo(secondUnsaved);

        Client persisted = clientRepository.saveAndFlush(firstUnsaved);
        entityManager.clear();
        Client loaded = clientRepository.findById(persisted.getId()).orElseThrow();
        assertThat(entityManagerFactory.getPersistenceUnitUtil().isLoaded(loaded, "documents")).isFalse();
        int persistedHash = loaded.hashCode();
        String representation = loaded.toString();

        assertThat(persistedHash).isEqualTo(hashBeforePersistence);
        assertThat(loaded).isEqualTo(persisted);
        assertThat(representation).doesNotContain("documents");
        assertThat(entityManagerFactory.getPersistenceUnitUtil().isLoaded(loaded, "documents")).isFalse();
    }

    @Test
    void documentEqualityIsIdBasedStableAndDoesNotInitializeItsClient() {
        Document firstUnsaved = document("number-equality-one");
        Document secondUnsaved = document("number-equality-two");
        int hashBeforePersistence = firstUnsaved.hashCode();
        assertThat(firstUnsaved).isNotEqualTo(secondUnsaved);

        Client owner = client("tax-document-equality");
        owner.addDocument(firstUnsaved);
        clientRepository.saveAndFlush(owner);
        Long documentId = firstUnsaved.getId();
        entityManager.clear();
        Document loaded = documentRepository.findById(documentId).orElseThrow();
        assertThat(entityManagerFactory.getPersistenceUnitUtil().isLoaded(loaded, "client")).isFalse();
        int persistedHash = loaded.hashCode();
        String representation = loaded.toString();

        assertThat(persistedHash).isEqualTo(hashBeforePersistence);
        assertThat(loaded).isEqualTo(firstUnsaved);
        assertThat(representation).doesNotContain("client");
        assertThat(entityManagerFactory.getPersistenceUnitUtil().isLoaded(loaded, "client")).isFalse();
    }

    @Test
    void associationHelpersPreventOwnershipTransfer() {
        Client owner = client("tax-owner");
        Client other = client("tax-other-owner");
        Document document = document("number-owned");
        owner.addDocument(document);

        assertThatThrownBy(() -> other.addDocument(document))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be transferred");
        assertThat(document.getClient()).isSameAs(owner);
    }

    @Test
    void documentOwnershipIsPersistentAndJoinColumnIsNotUpdatable() {
        Client owner = clientRepository.saveAndFlush(client("tax-immutable-owner"));
        Client otherOwner = clientRepository.saveAndFlush(client("tax-immutable-other"));
        Document document = document("number-immutable-owner");
        owner.addDocument(document);
        clientRepository.flush();
        Long ownerId = owner.getId();
        Long otherOwnerId = otherOwner.getId();
        Long documentId = document.getId();

        assertThatThrownBy(() -> otherOwner.addDocument(document))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be transferred");
        document.setDescription("scalar update does not change owner");
        documentRepository.flush();
        entityManager.clear();

        assertThat(jdbcTemplate.queryForObject(
                "SELECT client_id FROM documents WHERE id = ?", Long.class, documentId))
                .isEqualTo(ownerId)
                .isNotEqualTo(otherOwnerId);
        try {
            assertThat(Document.class.getDeclaredField("client")
                    .getAnnotation(JoinColumn.class).updatable()).isFalse();
        } catch (NoSuchFieldException exception) {
            throw new AssertionError("Document.client mapping field was not found", exception);
        }
    }

    @Test
    void assigningLoadedDocumentToSamePersistedClientIdentityDoesNotInitializeClientProxy() {
        Client owner = clientRepository.saveAndFlush(client("tax-proxy-owner"));
        Document document = document("number-proxy-owner");
        owner.addDocument(document);
        clientRepository.flush();
        Long ownerId = owner.getId();
        Long documentId = document.getId();
        entityManager.clear();

        Client detachedOwner = clientRepository.findById(ownerId).orElseThrow();
        detachedOwner.getDocuments().size();
        entityManager.detach(detachedOwner);
        entityManager.clear();
        Document loadedDocument = documentRepository.findById(documentId).orElseThrow();
        Client clientProxy = loadedDocument.getClient();
        assertThat(entityManagerFactory.getPersistenceUnitUtil().isLoaded(loadedDocument, "client"))
                .isFalse();
        assertThat(Hibernate.isInitialized(clientProxy)).isFalse();

        detachedOwner.addDocument(loadedDocument);

        assertThat(Hibernate.isInitialized(clientProxy)).isFalse();
        assertThat(detachedOwner.getDocuments()).contains(loadedDocument);
    }

    private static Client client(String taxIdentifier) {
        return new Client("First", "Last", taxIdentifier, "client@example.com", "555-0100");
    }

    private static Document document(String number) {
        return new Document(number, null, LocalDate.of(2030, 1, 1));
    }
}
