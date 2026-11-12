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
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

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
@Tag(name = "Documents", description = "Protected Document operations scoped to their owning Client.")
@SecurityRequirement(name = "bearerAuth")
public class DocumentController {

    private final DocumentService documentService;
    private final AppPaginationProperties paginationProperties;

    public DocumentController(DocumentService documentService, AppPaginationProperties paginationProperties) {
        this.documentService = documentService;
        this.paginationProperties = paginationProperties;
    }

    @PostMapping
    @Operation(summary = "Create a document", description = "Creates a Document owned by the specified Client. "
            + "The expected Client version is required in X-Client-Version. Document numbers are globally unique.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Document created"),
            @ApiResponse(responseCode = "400", description = "Invalid request or version header (Problem Details)"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token (Problem Details)"),
            @ApiResponse(responseCode = "403", description = "Access denied (Problem Details)"),
            @ApiResponse(responseCode = "404", description = "Client not found (Problem Details)"),
            @ApiResponse(responseCode = "409", description = "Duplicate number or stale Client version (Problem Details)"),
            @ApiResponse(responseCode = "500", description = "Unexpected error (Problem Details)")
    })
    public ResponseEntity<DocumentResponse> create(
            @Parameter(description = "Owning Client identifier", required = true) @PathVariable Long clientId,
            @Parameter(name = "X-Client-Version", in = ParameterIn.HEADER, description = "Expected current Client version", required = true, schema = @Schema(type = "integer", format = "int64", minimum = "0"))
            @RequestHeader("X-Client-Version") Long expectedClientVersion,
            @Valid @RequestBody DocumentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.create(clientId, expectedClientVersion, request));
    }

    @GetMapping
    @Operation(summary = "List client documents", description = "Returns a page scoped to the Client and ordered "
            + "by document ID ascending. Defaults to page 0 and size 20; size is limited to 100. Invalid pagination "
            + "returns HTTP 400; a missing Client returns HTTP 404.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated document collection"),
            @ApiResponse(responseCode = "400", description = "Invalid page or size (Problem Details)"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token (Problem Details)"),
            @ApiResponse(responseCode = "403", description = "Access denied (Problem Details)"),
            @ApiResponse(responseCode = "404", description = "Client not found (Problem Details)"),
            @ApiResponse(responseCode = "500", description = "Unexpected error (Problem Details)")
    })
    public PageResponse<DocumentResponse> list(
            @Parameter(description = "Owning Client identifier", required = true) @PathVariable Long clientId,
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
    @Operation(summary = "Get a document", description = "Returns a Document only when it belongs to the specified "
            + "Client. Access under a non-owning Client ID returns 404.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Document found"),
            @ApiResponse(responseCode = "400", description = "Malformed Client or Document identifier (Problem Details)"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token (Problem Details)"),
            @ApiResponse(responseCode = "403", description = "Access denied (Problem Details)"),
            @ApiResponse(responseCode = "404", description = "Client/Document not found under this Client (Problem Details)"),
            @ApiResponse(responseCode = "500", description = "Unexpected error (Problem Details)")
    })
    public DocumentResponse getById(@PathVariable Long clientId, @PathVariable Long documentId) {
        return documentService.getById(clientId, documentId);
    }

    @PutMapping("/{documentId}")
    @Operation(summary = "Replace editable document fields", description = "Replaces all editable Document fields. "
            + "Document version is required in the body and expected Client version in X-Client-Version. Ownership "
            + "cannot change. Required number and expirationDate must be supplied; description is optional. "
            + "The update is atomic; stale versions or duplicate numbers return 409.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Document updated"),
            @ApiResponse(responseCode = "400", description = "Invalid body or version (Problem Details)"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token (Problem Details)"),
            @ApiResponse(responseCode = "403", description = "Access denied (Problem Details)"),
            @ApiResponse(responseCode = "404", description = "Client/Document not found under this Client (Problem Details)"),
            @ApiResponse(responseCode = "409", description = "Duplicate number or stale version (Problem Details)"),
            @ApiResponse(responseCode = "500", description = "Unexpected error (Problem Details)")
    })
    public DocumentResponse update(
            @Parameter(description = "Owning Client identifier", required = true) @PathVariable Long clientId,
            @Parameter(description = "Document identifier", required = true) @PathVariable Long documentId,
            @Parameter(name = "X-Client-Version", in = ParameterIn.HEADER, description = "Expected current Client version", required = true, schema = @Schema(type = "integer", format = "int64", minimum = "0"))
            @RequestHeader("X-Client-Version") Long expectedClientVersion,
            @Valid @RequestBody DocumentPutRequest request) {
        return documentService.update(clientId, documentId, expectedClientVersion, request);
    }

    @PatchMapping("/{documentId}")
    @Operation(summary = "Partially update a document", description = "Only supplied editable fields are changed; "
            + "omitted fields remain unchanged. Document version is required in the body and expected Client version "
            + "in X-Client-Version. Explicit null is invalid for number/expirationDate; description:null clears it. "
            + "Ownership cannot change and the operation is atomic.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Document updated"),
            @ApiResponse(responseCode = "400", description = "Invalid body or version (Problem Details)"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token (Problem Details)"),
            @ApiResponse(responseCode = "403", description = "Access denied (Problem Details)"),
            @ApiResponse(responseCode = "404", description = "Client/Document not found under this Client (Problem Details)"),
            @ApiResponse(responseCode = "409", description = "Duplicate number or stale version (Problem Details)"),
            @ApiResponse(responseCode = "500", description = "Unexpected error (Problem Details)")
    })
    public DocumentResponse patch(
            @Parameter(description = "Owning Client identifier", required = true) @PathVariable Long clientId,
            @Parameter(description = "Document identifier", required = true) @PathVariable Long documentId,
            @Parameter(name = "X-Client-Version", in = ParameterIn.HEADER, description = "Expected current Client version", required = true, schema = @Schema(type = "integer", format = "int64", minimum = "0"))
            @RequestHeader("X-Client-Version") Long expectedClientVersion,
            @Valid @RequestBody DocumentPatchRequest request) {
        return documentService.patch(clientId, documentId, expectedClientVersion, request);
    }

    @DeleteMapping("/{documentId}")
    @Operation(summary = "Delete a document", description = "Deletes only this Document from the specified Client. "
            + "Both expected Client and Document versions are required in X-Client-Version and X-Document-Version. "
            + "Deletion and version checks are atomic.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Document deleted"),
            @ApiResponse(responseCode = "400", description = "Missing, malformed, or invalid version header (Problem Details)"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token (Problem Details)"),
            @ApiResponse(responseCode = "403", description = "Access denied (Problem Details)"),
            @ApiResponse(responseCode = "404", description = "Client/Document not found under this Client (Problem Details)"),
            @ApiResponse(responseCode = "409", description = "Stale version or integrity conflict (Problem Details)"),
            @ApiResponse(responseCode = "500", description = "Unexpected error (Problem Details)")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Owning Client identifier", required = true) @PathVariable Long clientId,
            @Parameter(description = "Document identifier", required = true) @PathVariable Long documentId,
            @Parameter(name = "X-Client-Version", in = ParameterIn.HEADER, description = "Expected current Client version", required = true, schema = @Schema(type = "integer", format = "int64", minimum = "0"))
            @RequestHeader("X-Client-Version") Long expectedClientVersion,
            @Parameter(name = "X-Document-Version", in = ParameterIn.HEADER, description = "Expected current Document version", required = true, schema = @Schema(type = "integer", format = "int64", minimum = "0"))
            @RequestHeader("X-Document-Version") Long expectedDocumentVersion) {
        documentService.delete(clientId, documentId, expectedClientVersion, expectedDocumentVersion);
        return ResponseEntity.noContent().build();
    }
}
