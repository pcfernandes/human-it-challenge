package com.humanit.challenge.service;

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
import com.humanit.challenge.dto.document.DocumentCreateRequest;
import com.humanit.challenge.dto.document.DocumentPatchRequest;
import com.humanit.challenge.dto.document.DocumentPutRequest;
import com.humanit.challenge.dto.document.DocumentResponse;
import com.humanit.challenge.mapper.DocumentMapper;
import com.humanit.challenge.repository.ClientRepository;
import com.humanit.challenge.repository.DocumentRepository;

@Service
public class DocumentService {

    private final ClientRepository clientRepository;
    private final DocumentRepository documentRepository;

    public DocumentService(ClientRepository clientRepository, DocumentRepository documentRepository) {
        this.clientRepository = clientRepository;
        this.documentRepository = documentRepository;
    }

    @Transactional
    public DocumentResponse create(Long clientId, Long expectedClientVersion, DocumentCreateRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        Client client = lockClient(clientId, expectedClientVersion);
        ensureNumberAvailable(request.getNumber(), null);
        try {
            Document saved = documentRepository.saveAndFlush(DocumentMapper.toEntity(request, client));
            clientRepository.flush();
            return DocumentMapper.toResponse(saved);
        } catch (DataIntegrityViolationException | ObjectOptimisticLockingFailureException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Document create conflict");
        }
    }

    @Transactional(readOnly = true)
    public DocumentResponse getById(Long clientId, Long documentId) {
        return DocumentMapper.toResponse(loadDocument(clientId, documentId));
    }

    @Transactional(readOnly = true)
    public Page<DocumentResponse> list(Long clientId, Pageable pageable) {
        if (!clientRepository.existsById(clientId)) {
            throw notFound("Client not found");
        }
        return documentRepository.findByClientId(clientId, pageable).map(DocumentMapper::toResponse);
    }

    @Transactional
    public DocumentResponse update(Long clientId, Long documentId, Long expectedClientVersion,
            DocumentPutRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        lockClient(clientId, expectedClientVersion);
        Document document = loadDocument(clientId, documentId);
        enforceDocumentVersion(document, request.getVersion());
        ensureNumberAvailable(request.getNumber(), document.getId());
        try {
            DocumentMapper.updateFromPut(request, document);
            Document saved = documentRepository.saveAndFlush(document);
            clientRepository.flush();
            return DocumentMapper.toResponse(saved);
        } catch (DataIntegrityViolationException | ObjectOptimisticLockingFailureException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Document update conflict");
        }
    }

    @Transactional
    public DocumentResponse patch(Long clientId, Long documentId, Long expectedClientVersion,
            DocumentPatchRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        lockClient(clientId, expectedClientVersion);
        Document document = loadDocument(clientId, documentId);
        enforceDocumentVersion(document, request.getVersion());
        if (request.getNumber().isPresentValue()) {
            ensureNumberAvailable(request.getNumber().get(), document.getId());
        }
        try {
            DocumentMapper.updateFromPatch(request, document);
            Document saved = documentRepository.saveAndFlush(document);
            clientRepository.flush();
            return DocumentMapper.toResponse(saved);
        } catch (DataIntegrityViolationException | ObjectOptimisticLockingFailureException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Document update conflict");
        }
    }

    @Transactional
    public void delete(Long clientId, Long documentId, Long expectedClientVersion, Long expectedDocumentVersion) {
        lockClient(clientId, expectedClientVersion);
        Document document = loadDocument(clientId, documentId);
        enforceDocumentVersion(document, expectedDocumentVersion);
        try {
            documentRepository.delete(document);
            documentRepository.flush();
            clientRepository.flush();
        } catch (DataIntegrityViolationException | ObjectOptimisticLockingFailureException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Document delete conflict");
        }
    }

    private Client lockClient(Long clientId, Long expectedVersion) {
        if (expectedVersion == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Client version is required");
        }
        if (expectedVersion < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Client version must not be negative");
        }
        Client client = clientRepository.lockById(clientId)
                .orElseThrow(() -> notFound("Client not found"));
        if (!expectedVersion.equals(client.getVersion())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Client version conflict");
        }
        return client;
    }

    private Document loadDocument(Long clientId, Long documentId) {
        return documentRepository.findByIdAndClientId(documentId, clientId)
                .orElseThrow(() -> notFound("Document not found"));
    }

    private void enforceDocumentVersion(Document document, Long expectedVersion) {
        if (expectedVersion == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Document version is required");
        }
        if (expectedVersion < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Document version must not be negative");
        }
        if (!expectedVersion.equals(document.getVersion())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Document version conflict");
        }
    }

    private void ensureNumberAvailable(String number, Long currentDocumentId) {
        documentRepository.findByNumber(number).ifPresent(existing -> {
            if (currentDocumentId == null || !currentDocumentId.equals(existing.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Document number already in use");
            }
        });
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }
}
