package com.humanit.challenge.mapper;

import java.util.Objects;

import com.humanit.challenge.domain.Client;
import com.humanit.challenge.domain.Document;
import com.humanit.challenge.dto.client.ClientDocumentPatchRequest;
import com.humanit.challenge.dto.client.ClientDocumentPutRequest;
import com.humanit.challenge.dto.document.DocumentCreateRequest;
import com.humanit.challenge.dto.document.DocumentPatchRequest;
import com.humanit.challenge.dto.document.DocumentPutRequest;
import com.humanit.challenge.dto.document.DocumentResponse;
import com.humanit.challenge.dto.patch.PatchValue;

/**
 * Dedicated mapper for {@link Document} <-> DTO transformations.
 *
 * <p>This class performs plain in-memory mapping only. It never accesses a repository,
 * never starts a transaction and never triggers lazy loading as a side effect of an update.
 * Generated identifiers, versions and ownership are never overwritten from request data.
 */
public final class DocumentMapper {

    private DocumentMapper() {
        // Utility class.
    }

    // ------------------------------------------------------------------ create

    public static Document toEntity(DocumentCreateRequest request) {
        return toEntity(request, null);
    }

    public static Document toEntity(DocumentCreateRequest request, Client owner) {
        Objects.requireNonNull(request, "request must not be null");
        Document document = new Document(request.getNumber(), request.getDescription(), request.getExpirationDate());
        associate(document, owner);
        return document;
    }

    public static Document toEntity(ClientDocumentPutRequest request, Client owner) {
        Objects.requireNonNull(request, "request must not be null");
        Document document = new Document(request.getNumber(), request.getDescription(), request.getExpirationDate());
        associate(document, owner);
        return document;
    }

    public static Document toEntity(ClientDocumentPatchRequest request, Client owner) {
        Objects.requireNonNull(request, "request must not be null");
        String number = request.getNumber().isPresentValue() ? request.getNumber().get() : null;
        String description = request.getDescription().isPresentValue() ? request.getDescription().get() : null;
        // An explicitly cleared description on a brand-new Document is represented as null.
        if (request.getDescription().isPresentNull()) {
            description = null;
        }
        java.time.LocalDate expirationDate = request.getExpirationDate().isPresentValue()
                ? request.getExpirationDate().get() : null;
        Document document = new Document(number, description, expirationDate);
        associate(document, owner);
        return document;
    }

    // ---------------------------------------------------------------- response

    public static DocumentResponse toResponse(Document document) {
        Objects.requireNonNull(document, "document must not be null");
        Long clientId = document.getClient() == null ? null : document.getClient().getId();
        return new DocumentResponse(
                document.getId(),
                clientId,
                document.getNumber(),
                document.getDescription(),
                document.getExpirationDate(),
                document.getVersion());
    }

    // ------------------------------------------------------------------ put

    public static void updateFromPut(DocumentPutRequest request, Document document) {
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(document, "document must not be null");
        document.setNumber(request.getNumber());
        document.setDescription(request.getDescription());
        document.setExpirationDate(request.getExpirationDate());
    }

    public static void updateFromPut(ClientDocumentPutRequest request, Document document) {
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(document, "document must not be null");
        document.setNumber(request.getNumber());
        document.setDescription(request.getDescription());
        document.setExpirationDate(request.getExpirationDate());
    }

    // ---------------------------------------------------------------- patch

    public static void updateFromPatch(DocumentPatchRequest request, Document document) {
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(document, "document must not be null");
        applyPatchValue(request.getNumber(), document::setNumber);
        applyOptionalDescription(request.getDescription(), document);
        applyPatchValue(request.getExpirationDate(), document::setExpirationDate);
    }

    public static void updateFromPatch(ClientDocumentPatchRequest request, Document document) {
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(document, "document must not be null");
        if (request.getNumber().isPresentValue()) {
            document.setNumber(request.getNumber().get());
        }
        if (request.getDescription().isPresentNull()) {
            document.setDescription(null);
        } else if (request.getDescription().isPresentValue()) {
            document.setDescription(request.getDescription().get());
        }
        if (request.getExpirationDate().isPresentValue()) {
            document.setExpirationDate(request.getExpirationDate().get());
        }
    }

    // ----------------------------------------------------------------- aliases

    public static Document create(DocumentCreateRequest request) {
        return toEntity(request);
    }

    public static Document create(DocumentCreateRequest request, Client owner) {
        return toEntity(request, owner);
    }

    public static DocumentResponse toDto(Document document) {
        return toResponse(document);
    }

    public static void applyPut(DocumentPutRequest request, Document document) {
        updateFromPut(request, document);
    }

    public static void applyPut(ClientDocumentPutRequest request, Document document) {
        updateFromPut(request, document);
    }

    public static void applyPatch(DocumentPatchRequest request, Document document) {
        updateFromPatch(request, document);
    }

    public static void applyPatch(ClientDocumentPatchRequest request, Document document) {
        updateFromPatch(request, document);
    }

    public static void update(DocumentPutRequest request, Document document) {
        updateFromPut(request, document);
    }

    public static void update(DocumentPatchRequest request, Document document) {
        updateFromPatch(request, document);
    }

    public static void update(Document document, DocumentPutRequest request) {
        updateFromPut(request, document);
    }

    public static void update(Document document, DocumentPatchRequest request) {
        updateFromPatch(request, document);
    }

    public static void updateFrom(DocumentPutRequest request, Document document) {
        updateFromPut(request, document);
    }

    public static void updateFrom(DocumentPatchRequest request, Document document) {
        updateFromPatch(request, document);
    }

    // ---------------------------------------------------------------- helpers

    private static void associate(Document document, Client owner) {
        if (owner != null) {
            owner.addDocument(document);
        }
    }

    private static <T> void applyPatchValue(PatchValue<T> value, java.util.function.Consumer<T> setter) {
        if (value.isPresentValue()) {
            setter.accept(value.get());
        }
        // ABSENT preserves; PRESENT_NULL is only valid for optional description and is
        // handled by the dedicated description logic below.
    }

    private static void applyOptionalDescription(PatchValue<String> value, Document document) {
        if (value.isPresentNull()) {
            document.setDescription(null);
        } else if (value.isPresentValue()) {
            document.setDescription(value.get());
        }
    }
}
