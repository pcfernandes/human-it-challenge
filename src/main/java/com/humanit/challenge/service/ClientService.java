package com.humanit.challenge.service;

import java.util.List;
import java.util.Objects;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.humanit.challenge.domain.Client;
import com.humanit.challenge.domain.Document;
import com.humanit.challenge.dto.client.ClientCreateRequest;
import com.humanit.challenge.dto.client.ClientDocumentPatchRequest;
import com.humanit.challenge.dto.client.ClientDocumentPutRequest;
import com.humanit.challenge.dto.client.ClientPatchRequest;
import com.humanit.challenge.dto.client.ClientPutRequest;
import com.humanit.challenge.dto.client.ClientResponse;
import com.humanit.challenge.dto.patch.PatchValue;
import com.humanit.challenge.mapper.ClientMapper;
import com.humanit.challenge.mapper.DocumentMapper;
import com.humanit.challenge.repository.ClientRepository;
import com.humanit.challenge.repository.DocumentRepository;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final DocumentRepository documentRepository;

    public ClientService(ClientRepository clientRepository, DocumentRepository documentRepository) {
        this.clientRepository = clientRepository;
        this.documentRepository = documentRepository;
    }

    @Transactional
    public ClientResponse create(ClientCreateRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        Client client = ClientMapper.toEntity(request);
        ensureTaxIdentifierAvailable(client.getTaxIdentifier(), null);
        if (request.getDocuments() != null) {
            for (var documentRequest : request.getDocuments()) {
                ensureDocumentNumberAvailable(documentRequest.getNumber(), null);
            }
        }
        try {
            Client saved = clientRepository.saveAndFlush(client);
            return ClientMapper.toResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Integrity conflict");
        }
    }

    @Transactional(readOnly = true)
    public ClientResponse getById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found"));
        return ClientMapper.toResponse(client);
    }

    @Transactional(readOnly = true)
    public Page<ClientResponse> list(Pageable pageable) {
        return clientRepository.findAll(pageable).map(ClientMapper::toResponse);
    }

    @Transactional
    public ClientResponse update(Long id, ClientPutRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        validateExpectedVersion(request.getVersion(), "Client");
        boolean nestedDocumentsSupplied = request.getDocuments() != null && !request.getDocuments().isEmpty();
        Client client = loadClient(id, nestedDocumentsSupplied);
        enforceVersion(client, request.getVersion());
        ensureTaxIdentifierAvailable(request.getTaxIdentifier(), client.getId());
        ClientMapper.updateFromPut(request, client);
        try {
            applyPutDocuments(client, request.getDocuments());
            Client saved = clientRepository.saveAndFlush(client);
            return ClientMapper.toResponse(saved);
        } catch (DataIntegrityViolationException | ObjectOptimisticLockingFailureException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Client update conflict");
        }
    }

    @Transactional
    public ClientResponse patch(Long id, ClientPatchRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        validateExpectedVersion(request.getVersion(), "Client");
        PatchValue<List<com.humanit.challenge.dto.client.ClientDocumentPatchRequest>> docs = request.getDocuments();
        boolean nestedDocumentsSupplied = docs.isPresentValue() && docs.get() != null
                && docs.get().stream().anyMatch(this::hasNestedPatchValues);
        Client client = loadClient(id, nestedDocumentsSupplied);
        enforceVersion(client, request.getVersion());
        if (request.getTaxIdentifier().isPresentValue()) {
            ensureTaxIdentifierAvailable(request.getTaxIdentifier().get(), client.getId());
        }
        ClientMapper.updateFromPatch(request, client);
        if (docs.isPresentValue()) {
            List<com.humanit.challenge.dto.client.ClientDocumentPatchRequest> supplied = docs.get();
            if (supplied != null && !supplied.isEmpty()) {
                applyPatchDocuments(client, supplied);
            }
        }
        try {
            Client saved = clientRepository.saveAndFlush(client);
            return ClientMapper.toResponse(saved);
        } catch (DataIntegrityViolationException | ObjectOptimisticLockingFailureException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Client update conflict");
        }
    }

    @Transactional
    public void delete(Long id, Long expectedVersion) {
        validateExpectedVersion(expectedVersion, "Client");
        Client client = loadClient(id);
        enforceVersion(client, expectedVersion);
        try {
            clientRepository.delete(client);
            clientRepository.flush();
        } catch (DataIntegrityViolationException | ObjectOptimisticLockingFailureException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Client delete conflict");
        }
    }

    // ---- helpers ----

    private Client loadClient(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found"));
    }

    private void enforceVersion(Client client, Long expectedVersion) {
        if (!expectedVersion.equals(client.getVersion())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Version conflict");
        }
    }

    private Client loadClient(Long id, boolean forceIncrement) {
        if (!forceIncrement) {
            return loadClient(id);
        }
        return clientRepository.lockById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found"));
    }

    private void validateExpectedVersion(Long expectedVersion, String resource) {
        if (expectedVersion == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, resource + " version is required");
        }
        if (expectedVersion < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, resource + " version must not be negative");
        }
    }

    private void ensureTaxIdentifierAvailable(String taxIdentifier, Long currentClientId) {
        if (taxIdentifier == null) {
            return;
        }
        clientRepository.findByTaxIdentifier(taxIdentifier).ifPresent(existing -> {
            if (currentClientId == null || !existing.getId().equals(currentClientId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Tax identifier already in use");
            }
        });
    }

    private void applyPutDocuments(Client client, List<ClientDocumentPutRequest> supplied) {
        if (supplied == null || supplied.isEmpty()) {
            return;
        }
        Long clientId = client.getId();
        for (ClientDocumentPutRequest docRequest : supplied) {
            if (docRequest.getId() == null && docRequest.getVersion() == null) {
                ensureDocumentNumberAvailable(docRequest.getNumber(), null);
                Document document = DocumentMapper.toEntity(docRequest, client);
                client.addDocument(document);
            } else {
                Long documentId = docRequest.getId();
                Document existing = documentRepository.findById(documentId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
                if (existing.getClient() == null || !clientId.equals(existing.getClient().getId())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Document does not belong to client");
                }
                if (!Objects.equals(existing.getVersion(), docRequest.getVersion())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Document version conflict");
                }
                ensureDocumentNumberAvailable(docRequest.getNumber(), existing.getId());
                DocumentMapper.updateFromPut(docRequest, existing);
            }
        }
    }

    private void applyPatchDocuments(Client client, List<ClientDocumentPatchRequest> supplied) {
        if (supplied == null || supplied.isEmpty()) {
            return;
        }
        Long clientId = client.getId();
        for (ClientDocumentPatchRequest docRequest : supplied) {
            if (docRequest.getId() == null && docRequest.getVersion() == null) {
                String number = docRequest.getNumber().isPresentValue() ? docRequest.getNumber().get() : null;
                ensureDocumentNumberAvailable(number, null);
                Document document = DocumentMapper.toEntity(docRequest, client);
                client.addDocument(document);
            } else {
                Long documentId = docRequest.getId();
                Document existing = documentRepository.findById(documentId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
                if (existing.getClient() == null || !clientId.equals(existing.getClient().getId())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Document does not belong to client");
                }
                if (!Objects.equals(existing.getVersion(), docRequest.getVersion())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Document version conflict");
                }
                if (docRequest.getNumber().isPresentValue()) {
                    ensureDocumentNumberAvailable(docRequest.getNumber().get(), existing.getId());
                }
                DocumentMapper.updateFromPatch(docRequest, existing);
            }
        }
    }

    private boolean hasNestedPatchValues(ClientDocumentPatchRequest request) {
        return request.getId() == null
                || request.getNumber().isPresentValue() || request.getNumber().isPresentNull()
                || request.getDescription().isPresentValue() || request.getDescription().isPresentNull()
                || request.getExpirationDate().isPresentValue() || request.getExpirationDate().isPresentNull();
    }

    private void ensureDocumentNumberAvailable(String number, Long currentDocumentId) {
        if (number == null) {
            return;
        }
        documentRepository.findByNumber(number).ifPresent(existing -> {
            if (currentDocumentId == null || !existing.getId().equals(currentDocumentId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Document number already in use");
            }
        });
    }
}
