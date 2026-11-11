package com.humanit.challenge.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import com.humanit.challenge.config.AppPaginationProperties;
import com.humanit.challenge.dto.common.PageResponse;
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
    private final AppPaginationProperties paginationProperties;

    public DocumentController(DocumentService documentService, AppPaginationProperties paginationProperties) {
        this.documentService = documentService;
        this.paginationProperties = paginationProperties;
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
    @Operation(summary = "List client documents", description = "Returns a page scoped to the Client and ordered "
            + "by document ID ascending. Defaults to page 0 and size 20; size is limited to 100. Invalid pagination "
            + "returns HTTP 400; a missing Client returns HTTP 404.")
    @ApiResponse(responseCode = "200", description = "Paginated document collection")
    @ApiResponse(responseCode = "400", description = "Invalid page or size (Problem Details)")
    @ApiResponse(responseCode = "404", description = "Client not found (Problem Details)")
    public PageResponse<DocumentResponse> list(
            @PathVariable Long clientId,
            @Parameter(description = "Zero-based page index", schema = @Schema(type = "integer", defaultValue = "0", minimum = "0"))
            @RequestParam(name = "page", required = false) Integer page,
            @Parameter(description = "Page size (1–100)", schema = @Schema(type = "integer", defaultValue = "20", minimum = "1", maximum = "100"))
            @RequestParam(name = "size", required = false) Integer size) {
        int resolvedPage = page == null ? paginationProperties.defaultPage() : page;
        int resolvedSize = size == null ? paginationProperties.defaultPageSize() : size;
        if (resolvedPage < 0 || resolvedSize < 1 || resolvedSize > paginationProperties.maxPageSize()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid page or size");
        }
        Pageable pageable = PageRequest.of(resolvedPage, resolvedSize, Sort.by(Sort.Direction.ASC, "id"));
        Page<DocumentResponse> result = documentService.list(clientId, pageable);
        return PageResponse.from(result);
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
