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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
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
import com.humanit.challenge.dto.client.ClientCreateRequest;
import com.humanit.challenge.dto.client.ClientPatchRequest;
import com.humanit.challenge.dto.client.ClientPutRequest;
import com.humanit.challenge.dto.client.ClientResponse;
import com.humanit.challenge.dto.common.PageResponse;
import com.humanit.challenge.service.ClientService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/clients")
@Tag(name = "Clients", description = "Protected Client operations and nested aggregate updates.")
@SecurityRequirement(name = "bearerAuth")
public class ClientController {

    private final ClientService clientService;
    private final AppPaginationProperties paginationProperties;

    public ClientController(ClientService clientService, AppPaginationProperties paginationProperties) {
        this.clientService = clientService;
        this.paginationProperties = paginationProperties;
    }

    @PostMapping
    @Operation(summary = "Create a client", description = "Creates a Client and optionally its initial Documents. "
            + "No version is required. For this operation only, omitted, empty, or null documents means no Documents.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Client created"),
            @ApiResponse(responseCode = "400", description = "Invalid request (Problem Details)"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token (Problem Details)"),
            @ApiResponse(responseCode = "403", description = "Access denied (Problem Details)"),
            @ApiResponse(responseCode = "409", description = "Tax identifier or Document number conflict (Problem Details)"),
            @ApiResponse(responseCode = "500", description = "Unexpected error (Problem Details)")
    })
    public ResponseEntity<ClientResponse> create(@Valid @RequestBody ClientCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clientService.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a client", description = "Returns one Client with its Documents. Document ownership "
            + "is represented by the nested response; Documents cannot be transferred between Clients.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client found"),
            @ApiResponse(responseCode = "400", description = "Malformed Client identifier (Problem Details)"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token (Problem Details)"),
            @ApiResponse(responseCode = "403", description = "Access denied (Problem Details)"),
            @ApiResponse(responseCode = "404", description = "Client not found (Problem Details)"),
            @ApiResponse(responseCode = "500", description = "Unexpected error (Problem Details)")
    })
    public ClientResponse getById(@PathVariable Long id) {
        return clientService.getById(id);
    }

    @GetMapping
    @Operation(summary = "List clients", description = "Returns a page ordered by client ID ascending. "
            + "Defaults to page 0 and size 20; size is limited to 100. Invalid pagination returns HTTP 400.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated client collection"),
            @ApiResponse(responseCode = "400", description = "Invalid page or size (Problem Details)"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token (Problem Details)"),
            @ApiResponse(responseCode = "403", description = "Access denied (Problem Details)"),
            @ApiResponse(responseCode = "500", description = "Unexpected error (Problem Details)")
    })
    public PageResponse<ClientResponse> list(
            @Parameter(description = "Zero-based page index", schema = @Schema(type = "integer", defaultValue = "0", minimum = "0"))
            @RequestParam(name = "page", required = false) Integer page,
            @Parameter(description = "Page size (1–100)", schema = @Schema(type = "integer", defaultValue = "20", minimum = "1", maximum = "100"))
            @RequestParam(name = "size", required = false) Integer size) {
        Pageable pageable = paginationRequest(page, size);
        Page<ClientResponse> result = clientService.list(pageable);
        return PageResponse.from(result);
    }

    private Pageable paginationRequest(Integer page, Integer size) {
        int resolvedPage = page == null ? paginationProperties.defaultPage() : page;
        int resolvedSize = size == null ? paginationProperties.defaultPageSize() : size;
        if (resolvedPage < 0 || resolvedSize < 1 || resolvedSize > paginationProperties.maxPageSize()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid page or size");
        }
        return PageRequest.of(resolvedPage, resolvedSize, Sort.by(Sort.Direction.ASC, "id"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Replace editable client fields", description = "PUT replaces required editable scalar "
            + "fields and requires the expected Client version in the body. Omitted or empty documents preserves "
            + "existing Documents; supplied entries update by id/version or create without id/version. Omitted "
            + "existing Documents are preserved; deletion is explicit through DELETE. Explicit documents:null is invalid. "
            + "Unknown nested IDs return 404; cross-Client association or stale versions return 409. Changes are atomic.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client updated"),
            @ApiResponse(responseCode = "400", description = "Invalid body, field validation, or version (Problem Details)"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token (Problem Details)"),
            @ApiResponse(responseCode = "403", description = "Access denied (Problem Details)"),
            @ApiResponse(responseCode = "404", description = "Client or nested Document not found (Problem Details)"),
            @ApiResponse(responseCode = "409", description = "Uniqueness, ownership, or optimistic-lock conflict (Problem Details)"),
            @ApiResponse(responseCode = "500", description = "Unexpected error (Problem Details)")
    })
    public ClientResponse update(@PathVariable Long id, @Valid @RequestBody ClientPutRequest request) {
        ClientResponse updated = clientService.update(id, request);
        if (request.getDocuments() != null && !request.getDocuments().isEmpty()) {
            // JPA may apply OPTIMISTIC_FORCE_INCREMENT at transaction completion.
            return clientService.getById(id);
        }
        return updated;
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Partially update a client", description = "Only supplied scalar fields are changed; omitted "
            + "fields remain unchanged. The expected Client version is required. Omitted/empty documents preserves "
            + "existing Documents; null is invalid. Existing nested Documents require id and version and are patched "
            + "partially; new entries omit both and require number and expirationDate. No implicit deletion or ownership "
            + "transfer occurs. Explicit null is invalid for required values; description:null clears description. "
            + "Changes are atomic.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client updated"),
            @ApiResponse(responseCode = "400", description = "Invalid body, field validation, or version (Problem Details)"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token (Problem Details)"),
            @ApiResponse(responseCode = "403", description = "Access denied (Problem Details)"),
            @ApiResponse(responseCode = "404", description = "Client or nested Document not found (Problem Details)"),
            @ApiResponse(responseCode = "409", description = "Uniqueness, ownership, or optimistic-lock conflict (Problem Details)"),
            @ApiResponse(responseCode = "500", description = "Unexpected error (Problem Details)")
    })
    public ClientResponse patch(@PathVariable Long id, @Valid @RequestBody ClientPatchRequest request) {
        ClientResponse updated = clientService.patch(id, request);
        if (request.getDocuments().isPresentValue() && request.getDocuments().get() != null
                && !request.getDocuments().get().isEmpty()) {
            return clientService.getById(id);
        }
        return updated;
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a client", description = "Deletes the Client and all owned Documents atomically. "
            + "Requires the expected Client version in X-Client-Version.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Client and owned Documents deleted"),
            @ApiResponse(responseCode = "400", description = "Missing, malformed, or invalid version header (Problem Details)"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token (Problem Details)"),
            @ApiResponse(responseCode = "403", description = "Access denied (Problem Details)"),
            @ApiResponse(responseCode = "404", description = "Client not found (Problem Details)"),
            @ApiResponse(responseCode = "409", description = "Stale version or integrity conflict (Problem Details)"),
            @ApiResponse(responseCode = "500", description = "Unexpected error (Problem Details)")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Client identifier", required = true) @PathVariable Long id,
            @Parameter(name = "X-Client-Version", in = ParameterIn.HEADER, description = "Expected current Client version", required = true, schema = @Schema(type = "integer", format = "int64", minimum = "0"))
            @RequestHeader("X-Client-Version") Long expectedClientVersion) {
        clientService.delete(id, expectedClientVersion);
        return ResponseEntity.noContent().build();
    }
}
