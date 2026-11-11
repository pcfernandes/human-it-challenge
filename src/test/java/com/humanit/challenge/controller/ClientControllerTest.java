package com.humanit.challenge.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.humanit.challenge.dto.client.ClientCreateRequest;
import com.humanit.challenge.dto.client.ClientPatchRequest;
import com.humanit.challenge.dto.client.ClientPutRequest;
import com.humanit.challenge.dto.client.ClientResponse;
import com.humanit.challenge.exception.GlobalExceptionHandler;
import com.humanit.challenge.service.ClientService;

@WebMvcTest(ClientController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService clientService;

    @Test
    void postAndGetClient() throws Exception {
        ClientResponse client = response(42L, "Ada");
        when(clientService.create(any(ClientCreateRequest.class))).thenReturn(client);
        when(clientService.getById(42L)).thenReturn(client);

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Ada","lastName":"Lovelace","taxIdentifier":"TAX-42",
                                 "email":"ada@example.com","phoneNumber":"123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.firstName").value("Ada"));

        mockMvc.perform(get("/api/clients/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.firstName").value("Ada"));
        verify(clientService).create(any(ClientCreateRequest.class));
        verify(clientService).getById(42L);
    }

    @Test
    void listClients() throws Exception {
        when(clientService.list()).thenReturn(List.of(response(42L, "Ada"), response(43L, "Grace")));

        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Ada"))
                .andExpect(jsonPath("$[1].firstName").value("Grace"));
        verify(clientService).list();
    }

    @Test
    void putAndPatchClient() throws Exception {
        when(clientService.update(org.mockito.ArgumentMatchers.eq(42L), any(ClientPutRequest.class)))
                .thenReturn(response(42L, "Grace"));
        when(clientService.patch(org.mockito.ArgumentMatchers.eq(42L), any(ClientPatchRequest.class)))
                .thenReturn(response(42L, "Grace Hopper"));

        mockMvc.perform(put("/api/clients/42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Grace","lastName":"Hopper","taxIdentifier":"TAX-42",
                                 "email":"grace@example.com","phoneNumber":"999","version":0,"documents":[]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Grace"));

        mockMvc.perform(patch("/api/clients/42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":0,\"firstName\":\"Grace Hopper\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Grace Hopper"));
        verify(clientService).update(org.mockito.ArgumentMatchers.eq(42L), any(ClientPutRequest.class));
        verify(clientService).patch(org.mockito.ArgumentMatchers.eq(42L), any(ClientPatchRequest.class));
    }

    @Test
    void deleteClientReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/clients/42").header("X-Client-Version", "0"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(clientService).delete(42L, 0L);
    }

    @Test
    void deleteClientRequiresWellFormedVersionHeader() throws Exception {
        mockMvc.perform(delete("/api/clients/42"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400));

        mockMvc.perform(delete("/api/clients/42").header("X-Client-Version", "not-a-version"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400));
        verifyNoInteractions(clientService);
    }

    @Test
    void invalidCreateReturnsBadRequestProblemDetails() throws Exception {
        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"","lastName":"Lovelace","taxIdentifier":"TAX",
                                 "email":"ada@example.com","phoneNumber":"123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400));
        verifyNoInteractions(clientService);
    }

    @Test
    void malformedClientUpdateVersionsReturnBadRequestProblemDetails() throws Exception {
        mockMvc.perform(put("/api/clients/42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Ada","lastName":"Lovelace","taxIdentifier":"TAX-42",
                                 "email":"ada@example.com","phoneNumber":"123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        mockMvc.perform(patch("/api/clients/42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Grace\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        mockMvc.perform(put("/api/clients/42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Ada","lastName":"Lovelace","taxIdentifier":"TAX-42",
                                 "email":"ada@example.com","phoneNumber":"123","version":"bad"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        mockMvc.perform(patch("/api/clients/42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":\"bad\",\"firstName\":\"Grace\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        verifyNoInteractions(clientService);
    }

    @Test
    void missingClientReturnsNotFound() throws Exception {
        when(clientService.getById(999L)).thenThrow(new ResponseStatusException(
                org.springframework.http.HttpStatus.NOT_FOUND, "Client not found"));

        mockMvc.perform(get("/api/clients/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void versionConflictReturnsConflict() throws Exception {
        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "Version conflict"))
                .when(clientService).update(org.mockito.ArgumentMatchers.eq(42L), any(ClientPutRequest.class));

        mockMvc.perform(put("/api/clients/42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Grace","lastName":"Hopper","taxIdentifier":"TAX-42",
                                 "email":"grace@example.com","phoneNumber":"999","version":0,"documents":[]}
                                """))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(409));
    }

    private ClientResponse response(Long id, String firstName) {
        return new ClientResponse(id, firstName, "Lovelace", "TAX-42", "ada@example.com", "123", 0L, List.of());
    }
}
