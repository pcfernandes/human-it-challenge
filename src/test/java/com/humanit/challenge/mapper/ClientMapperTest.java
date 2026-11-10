package com.humanit.challenge.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.humanit.challenge.domain.Client;
import com.humanit.challenge.domain.Document;
import com.humanit.challenge.dto.client.ClientCreateRequest;
import com.humanit.challenge.dto.client.ClientPatchRequest;
import com.humanit.challenge.dto.client.ClientPutRequest;
import com.humanit.challenge.dto.client.ClientResponse;
import com.humanit.challenge.dto.document.DocumentCreateRequest;
import com.humanit.challenge.dto.document.DocumentPatchRequest;
import com.humanit.challenge.dto.document.DocumentPutRequest;
import com.humanit.challenge.dto.document.DocumentResponse;
import com.humanit.challenge.dto.patch.PatchValue;

class ClientMapperTest {

    @Test
    void createMapsScalarsAndNestedDocuments() {
        ClientCreateRequest request = new ClientCreateRequest();
        request.setFirstName("Ada");
        request.setLastName("Lovelace");
        request.setTaxIdentifier("TAX-1");
        request.setEmail("ada@example.com");
        request.setPhoneNumber("123");
        request.setDocuments(List.of(new DocumentCreateRequest("DOC-1", "desc", LocalDate.of(2027, 1, 1))));

        Client client = ClientMapper.toEntity(request);

        assertEquals("Ada", client.getFirstName());
        assertEquals("Lovelace", client.getLastName());
        assertEquals("TAX-1", client.getTaxIdentifier());
        assertEquals("ada@example.com", client.getEmail());
        assertEquals("123", client.getPhoneNumber());
        assertEquals(1, client.getDocuments().size());
        assertEquals("DOC-1", client.getDocuments().get(0).getNumber());
        assertSame(client, client.getDocuments().get(0).getClient());
    }

    @Test
    void responseMapsNestedDocuments() {
        Client client = new Client("Ada", "Lovelace", "TAX-1", "ada@example.com", "123");
        Document document = new Document("DOC-1", "desc", LocalDate.of(2027, 1, 1));
        client.addDocument(document);

        ClientResponse response = ClientMapper.toResponse(client);

        assertEquals(client.getId(), response.id());
        assertEquals("Ada", response.firstName());
        assertEquals(1, response.documents().size());
        assertEquals("DOC-1", response.documents().get(0).number());
        assertEquals(client.getId(), response.documents().get(0).clientId());
    }

    @Test
    void putUpdatesScalarsWithoutChangingIdentityOrVersion() {
        Client client = new Client("Old", "Name", "OLD", "old@example.com", "000");
        Long idBefore = client.getId();
        Long versionBefore = client.getVersion();

        ClientPutRequest request = new ClientPutRequest();
        request.setFirstName("New");
        request.setLastName("Name");
        request.setTaxIdentifier("NEW");
        request.setEmail("new@example.com");
        request.setPhoneNumber("999");
        request.setVersion(5L);

        ClientMapper.updateFromPut(request, client);

        assertEquals("New", client.getFirstName());
        assertEquals("new@example.com", client.getEmail());
        assertEquals(idBefore, client.getId());
        assertEquals(versionBefore, client.getVersion());
    }

    @Test
    void patchPreservesOmittedFields() {
        Client client = new Client("Ada", "Lovelace", "TAX-1", "ada@example.com", "123");

        ClientPatchRequest request = new ClientPatchRequest();
        request.setVersion(0L);

        ClientMapper.updateFromPatch(request, client);

        assertEquals("Ada", client.getFirstName());
        assertEquals("Lovelace", client.getLastName());
        assertEquals("TAX-1", client.getTaxIdentifier());
        assertEquals("ada@example.com", client.getEmail());
        assertEquals("123", client.getPhoneNumber());
    }

    @Test
    void patchUpdatesSuppliedFields() {
        Client client = new Client("Ada", "Lovelace", "TAX-1", "ada@example.com", "123");

        ClientPatchRequest request = new ClientPatchRequest();
        request.setVersion(0L);
        request.setFirstName(PatchValue.of("Grace"));
        request.setEmail(PatchValue.of("grace@example.com"));

        ClientMapper.updateFromPatch(request, client);

        assertEquals("Grace", client.getFirstName());
        assertEquals("grace@example.com", client.getEmail());
        assertEquals("Lovelace", client.getLastName());
    }
}
