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
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

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
public class ClientController {

    private final ClientService clientService;
    private final AppPaginationProperties paginationProperties;

    public ClientController(ClientService clientService, AppPaginationProperties paginationProperties) {
        this.clientService = clientService;
        this.paginationProperties = paginationProperties;
    }

    @PostMapping
    public ResponseEntity<ClientResponse> create(@Valid @RequestBody ClientCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clientService.create(request));
    }

    @GetMapping("/{id}")
    public ClientResponse getById(@PathVariable Long id) {
        return clientService.getById(id);
    }

    @GetMapping
    @Operation(summary = "List clients", description = "Returns a page ordered by client ID ascending. "
            + "Defaults to page 0 and size 20; size is limited to 100. Invalid pagination returns HTTP 400.")
    @ApiResponse(responseCode = "200", description = "Paginated client collection")
    @ApiResponse(responseCode = "400", description = "Invalid page or size (Problem Details)")
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
    public ClientResponse update(@PathVariable Long id, @Valid @RequestBody ClientPutRequest request) {
        ClientResponse updated = clientService.update(id, request);
        if (request.getDocuments() != null && !request.getDocuments().isEmpty()) {
            // JPA may apply OPTIMISTIC_FORCE_INCREMENT at transaction completion.
            return clientService.getById(id);
        }
        return updated;
    }

    @PatchMapping("/{id}")
    public ClientResponse patch(@PathVariable Long id, @Valid @RequestBody ClientPatchRequest request) {
        ClientResponse updated = clientService.patch(id, request);
        if (request.getDocuments().isPresentValue() && request.getDocuments().get() != null
                && !request.getDocuments().get().isEmpty()) {
            return clientService.getById(id);
        }
        return updated;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestHeader("X-Client-Version") Long expectedClientVersion) {
        clientService.delete(id, expectedClientVersion);
        return ResponseEntity.noContent().build();
    }
}
