package com.humanit.challenge.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.humanit.challenge.domain.Client;
import com.humanit.challenge.domain.Document;
import com.humanit.challenge.dto.document.DocumentCreateRequest;
import com.humanit.challenge.dto.document.DocumentPatchRequest;
import com.humanit.challenge.dto.document.DocumentPutRequest;
import com.humanit.challenge.dto.document.DocumentResponse;
import com.humanit.challenge.dto.patch.PatchValue;

class DocumentMapperTest {

    @Test
    void createMapsFieldsAndOwner() {
        Client owner = new Client("Ada", "Lovelace", "TAX-1", "ada@example.com", "123");
        DocumentCreateRequest request = new DocumentCreateRequest("DOC-1", "desc", LocalDate.of(2027, 1, 1));

        Document document = DocumentMapper.toEntity(request, owner);

        assertEquals("DOC-1", document.getNumber());
        assertEquals("desc", document.getDescription());
        assertEquals(LocalDate.of(2027, 1, 1), document.getExpirationDate());
        assertSame(owner, document.getClient());
    }

    @Test
    void responseMapsScalarClientId() {
        Client owner = new Client("Ada", "Lovelace", "TAX-1", "ada@example.com", "123");
        Document document = new Document("DOC-1", "desc", LocalDate.of(2027, 1, 1));
        owner.addDocument(document);

        DocumentResponse response = DocumentMapper.toResponse(document);

        assertEquals(document.getId(), response.id());
        assertEquals(owner.getId(), response.clientId());
        assertEquals("DOC-1", response.number());
        assertEquals("desc", response.description());
    }

    @Test
    void putUpdatesFieldsWithoutChangingIdentityOrVersion() {
        Document document = new Document("OLD", "old", LocalDate.of(2026, 1, 1));
        Long idBefore = document.getId();
        Long versionBefore = document.getVersion();

        DocumentPutRequest request = new DocumentPutRequest();
        request.setVersion(3L);
        request.setNumber("NEW");
        request.setDescription("new");
        request.setExpirationDate(LocalDate.of(2028, 1, 1));

        DocumentMapper.updateFromPut(request, document);

        assertEquals("NEW", document.getNumber());
        assertEquals("new", document.getDescription());
        assertEquals(LocalDate.of(2028, 1, 1), document.getExpirationDate());
        assertEquals(idBefore, document.getId());
        assertEquals(versionBefore, document.getVersion());
    }

    @Test
    void patchPreservesOmittedFields() {
        Document document = new Document("DOC-1", "desc", LocalDate.of(2027, 1, 1));

        DocumentPatchRequest request = new DocumentPatchRequest();
        request.setVersion(0L);

        DocumentMapper.updateFromPatch(request, document);

        assertEquals("DOC-1", document.getNumber());
        assertEquals("desc", document.getDescription());
        assertEquals(LocalDate.of(2027, 1, 1), document.getExpirationDate());
    }

    @Test
    void patchUpdatesSuppliedFields() {
        Document document = new Document("DOC-1", "desc", LocalDate.of(2027, 1, 1));

        DocumentPatchRequest request = new DocumentPatchRequest();
        request.setVersion(0L);
        request.setNumber(PatchValue.of("DOC-2"));
        request.setExpirationDate(PatchValue.of(LocalDate.of(2028, 1, 1)));

        DocumentMapper.updateFromPatch(request, document);

        assertEquals("DOC-2", document.getNumber());
        assertEquals(LocalDate.of(2028, 1, 1), document.getExpirationDate());
        assertEquals("desc", document.getDescription());
    }

    @Test
    void patchDescriptionNullClearsDescription() {
        Document document = new Document("DOC-1", "desc", LocalDate.of(2027, 1, 1));

        DocumentPatchRequest request = new DocumentPatchRequest();
        request.setVersion(0L);
        request.setDescription(PatchValue.presentNull());

        DocumentMapper.updateFromPatch(request, document);

        assertNull(document.getDescription());
        assertEquals("DOC-1", document.getNumber());
    }
}
