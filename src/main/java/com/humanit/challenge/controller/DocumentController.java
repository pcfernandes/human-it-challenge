package com.humanit.challenge.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.humanit.challenge.dto.document.DocumentCreateRequest;
import com.humanit.challenge.dto.document.DocumentPatchRequest;
import com.humanit.challenge.dto.document.DocumentPutRequest;
import com.humanit.challenge.dto.document.DocumentResponse;
import com.humanit.challenge.service.DocumentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/clients/{clientId}/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    public ResponseEntity<DocumentResponse> create(
            @PathVariable Long clientId,
            @RequestHeader("X-Client-Version") Long expectedClientVersion,
            @Valid @RequestBody DocumentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.create(clientId, expectedClientVersion, request));
    }

    @GetMapping
    public Page<DocumentResponse> list(@PathVariable Long clientId, Pageable pageable) {
        return documentService.list(clientId, pageable);
    }

    @GetMapping("/{documentId}")
    public DocumentResponse getById(@PathVariable Long clientId, @PathVariable Long documentId) {
        return documentService.getById(clientId, documentId);
    }

    @PutMapping("/{documentId}")
    public DocumentResponse update(
            @PathVariable Long clientId,
            @PathVariable Long documentId,
            @RequestHeader("X-Client-Version") Long expectedClientVersion,
            @Valid @RequestBody DocumentPutRequest request) {
        return documentService.update(clientId, documentId, expectedClientVersion, request);
    }

    @PatchMapping("/{documentId}")
    public DocumentResponse patch(
            @PathVariable Long clientId,
            @PathVariable Long documentId,
            @RequestHeader("X-Client-Version") Long expectedClientVersion,
            @Valid @RequestBody DocumentPatchRequest request) {
        return documentService.patch(clientId, documentId, expectedClientVersion, request);
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long clientId,
            @PathVariable Long documentId,
            @RequestHeader("X-Client-Version") Long expectedClientVersion,
            @RequestHeader("X-Document-Version") Long expectedDocumentVersion) {
        documentService.delete(clientId, documentId, expectedClientVersion, expectedDocumentVersion);
        return ResponseEntity.noContent().build();
    }
}
