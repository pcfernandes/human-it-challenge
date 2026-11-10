package com.humanit.challenge.mapper;

import java.util.List;
import java.util.Objects;

import com.humanit.challenge.domain.Client;
import com.humanit.challenge.domain.Document;
import com.humanit.challenge.dto.client.ClientCreateRequest;
import com.humanit.challenge.dto.client.ClientPatchRequest;
import com.humanit.challenge.dto.client.ClientPutRequest;
import com.humanit.challenge.dto.client.ClientResponse;
import com.humanit.challenge.dto.document.DocumentResponse;
import com.humanit.challenge.dto.patch.PatchValue;

/**
 * Dedicated mapper for {@link Client} <-> DTO transformations.
 *
 * <p>Plain in-memory mapping only: no repository access, no transactions, no lazy loading
 * as a side effect of an update, no ownership resolution and no aggregate orchestration.
 * Generated identifiers, versions and ownership are never overwritten from request data.
 */
public final class ClientMapper {

    private ClientMapper() {
        // Utility class.
    }

    // ------------------------------------------------------------------ create

    public static Client toEntity(ClientCreateRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        Client client = new Client(
                request.getFirstName(),
                request.getLastName(),
                request.getTaxIdentifier(),
                request.getEmail(),
                request.getPhoneNumber());
        if (request.getDocuments() != null) {
            for (var documentRequest : request.getDocuments()) {
                Document document = DocumentMapper.toEntity(documentRequest, client);
                // addDocument is idempotent and preserves the owning-side invariant.
                client.addDocument(document);
            }
        }
        return client;
    }

    // ---------------------------------------------------------------- response

    public static ClientResponse toResponse(Client client) {
        Objects.requireNonNull(client, "client must not be null");
        List<DocumentResponse> documents = client.getDocuments().stream()
                .map(DocumentMapper::toResponse)
                .toList();
        return new ClientResponse(
                client.getId(),
                client.getFirstName(),
                client.getLastName(),
                client.getTaxIdentifier(),
                client.getEmail(),
                client.getPhoneNumber(),
                client.getVersion(),
                documents);
    }

    // ------------------------------------------------------------------ put

    public static void updateFromPut(ClientPutRequest request, Client client) {
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(client, "client must not be null");
        client.setFirstName(request.getFirstName());
        client.setLastName(request.getLastName());
        client.setTaxIdentifier(request.getTaxIdentifier());
        client.setEmail(request.getEmail());
        client.setPhoneNumber(request.getPhoneNumber());
        // Nested Documents are intentionally not replaced here. Their create/update
        // orchestration (identity matching, version checks, ownership) belongs to the
        // service layer, which supplies the exact managed targets to DocumentMapper.
    }

    // ---------------------------------------------------------------- patch

    public static void updateFromPatch(ClientPatchRequest request, Client client) {
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(client, "client must not be null");
        applyStringPatch(request.getFirstName(), client::setFirstName);
        applyStringPatch(request.getLastName(), client::setLastName);
        applyStringPatch(request.getTaxIdentifier(), client::setTaxIdentifier);
        applyStringPatch(request.getEmail(), client::setEmail);
        applyStringPatch(request.getPhoneNumber(), client::setPhoneNumber);
        // The documents collection is not replaced here; ABSENT preserves it and any
        // supplied entries are orchestrated by the service layer.
    }

    // ----------------------------------------------------------------- aliases

    public static Client create(ClientCreateRequest request) {
        return toEntity(request);
    }

    public static ClientResponse toDto(Client client) {
        return toResponse(client);
    }

    public static void applyPut(ClientPutRequest request, Client client) {
        updateFromPut(request, client);
    }

    public static void applyPatch(ClientPatchRequest request, Client client) {
        updateFromPatch(request, client);
    }

    public static void update(ClientPutRequest request, Client client) {
        updateFromPut(request, client);
    }

    public static void update(ClientPatchRequest request, Client client) {
        updateFromPatch(request, client);
    }

    public static void update(Client client, ClientPutRequest request) {
        updateFromPut(request, client);
    }

    public static void update(Client client, ClientPatchRequest request) {
        updateFromPatch(request, client);
    }

    public static void updateFrom(ClientPutRequest request, Client client) {
        updateFromPut(request, client);
    }

    public static void updateFrom(ClientPatchRequest request, Client client) {
        updateFromPatch(request, client);
    }

    // ---------------------------------------------------------------- helpers

    private static void applyStringPatch(PatchValue<String> value, java.util.function.Consumer<String> setter) {
        if (value.isPresentValue()) {
            setter.accept(value.get());
        }
        // ABSENT preserves the current value; PRESENT_NULL is invalid for required
        // Client scalars and is rejected by validation before reaching the mapper.
    }
}
