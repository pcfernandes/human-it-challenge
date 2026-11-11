package com.humanit.challenge.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
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

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.humanit.challenge.dto.document.DocumentCreateRequest;
import com.humanit.challenge.dto.document.DocumentPatchRequest;
import com.humanit.challenge.dto.document.DocumentPutRequest;
import com.humanit.challenge.dto.document.DocumentResponse;
import com.humanit.challenge.config.AppPaginationProperties;
import com.humanit.challenge.exception.GlobalExceptionHandler;
import com.humanit.challenge.service.DocumentService;

@WebMvcTest(DocumentController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@EnableConfigurationProperties(AppPaginationProperties.class)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentService documentService;

    @Test
    void postDocumentReturnsCreated() throws Exception {
        when(documentService.create(eq(7L), eq(3L), any(DocumentCreateRequest.class)))
                .thenReturn(response(12L, 7L, 0L));

        mockMvc.perform(post("/api/clients/7/documents")
                        .header("X-Client-Version", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"number":"DOC-12","description":"Passport","expirationDate":"2030-01-01"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.clientId").value(7));
        verify(documentService).create(eq(7L), eq(3L), any(DocumentCreateRequest.class));
    }

    @Test
    void listAndGetDocumentAreScopedToClient() throws Exception {
        DocumentResponse document = response(12L, 7L, 0L);
        when(documentService.list(eq(7L), any(Pageable.class))).thenAnswer(invocation -> {
            Pageable pageable = invocation.getArgument(1);
            return new PageImpl<>(List.of(document), pageable, 1);
        });
        when(documentService.getById(7L, 12L)).thenReturn(document);

        mockMvc.perform(get("/api/clients/7/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.content[0].id").value(12))
                .andExpect(jsonPath("$.content[0].clientId").value(7))
                .andExpect(jsonPath("$.content[0].number").value("DOC-12"))
                .andExpect(jsonPath("$.content[0].expirationDate").value("2030-01-01"));
        mockMvc.perform(get("/api/clients/7/documents").param("page", "2").param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.size").value(3));
        mockMvc.perform(get("/api/clients/7/documents").param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(100));
        mockMvc.perform(get("/api/clients/7/documents/12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.clientId").value(7));

        org.mockito.ArgumentCaptor<Pageable> pageableCaptor = org.mockito.ArgumentCaptor.forClass(Pageable.class);
        verify(documentService, org.mockito.Mockito.times(3)).list(eq(7L), pageableCaptor.capture());
        assertThat(pageableCaptor.getAllValues()).allSatisfy(pageable ->
                assertThat(pageable.getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "id")));
        assertThat(pageableCaptor.getAllValues().get(0).getPageNumber()).isZero();
        assertThat(pageableCaptor.getAllValues().get(0).getPageSize()).isEqualTo(20);
        assertThat(pageableCaptor.getAllValues().get(1).getPageNumber()).isEqualTo(2);
        assertThat(pageableCaptor.getAllValues().get(1).getPageSize()).isEqualTo(3);
        verify(documentService).getById(7L, 12L);
    }

    @Test
    void emptyAndOutOfRangeDocumentPagesReturnEmptyPageEnvelope() throws Exception {
        when(documentService.list(eq(7L), any(Pageable.class))).thenAnswer(invocation -> {
            Pageable pageable = invocation.getArgument(1);
            long totalElements = pageable.getPageNumber() == 0 ? 0 : 1;
            return new PageImpl<>(List.of(), pageable, totalElements);
        });

        mockMvc.perform(get("/api/clients/7/documents"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{\"content\":[],\"page\":0,\"size\":20,\"totalElements\":0,\"totalPages\":0}", true));
        mockMvc.perform(get("/api/clients/7/documents").param("page", "2").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{\"content\":[],\"page\":2,\"size\":1,\"totalElements\":1,\"totalPages\":1}", true));
    }

    @Test
    void invalidDocumentPaginationReturnsBadRequestProblemDetails() throws Exception {
        mockMvc.perform(get("/api/clients/7/documents").param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400));
        mockMvc.perform(get("/api/clients/7/documents").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        mockMvc.perform(get("/api/clients/7/documents").param("size", "0"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/clients/7/documents").param("size", "bad"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        verifyNoInteractions(documentService);
    }

    @Test
    void missingClientDocumentListReturnsNotFoundProblemDetails() throws Exception {
        when(documentService.list(eq(999L), any(Pageable.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found"));

        mockMvc.perform(get("/api/clients/999/documents"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404));
        verify(documentService).list(eq(999L), any(Pageable.class));
    }

    @Test
    void putAndPatchDelegateExpectedVersionsAndRequests() throws Exception {
        when(documentService.update(eq(7L), eq(12L), eq(3L), any(DocumentPutRequest.class)))
                .thenReturn(response(12L, 7L, 1L));
        when(documentService.patch(eq(7L), eq(12L), eq(3L), any(DocumentPatchRequest.class)))
                .thenReturn(response(12L, 7L, 2L));

        mockMvc.perform(put("/api/clients/7/documents/12")
                        .header("X-Client-Version", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"version":0,"number":"DOC-12","description":"Passport",
                                 "expirationDate":"2030-01-01"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(1));
        mockMvc.perform(patch("/api/clients/7/documents/12")
                        .header("X-Client-Version", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"version":1,"description":null}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(2));

        verify(documentService).update(eq(7L), eq(12L), eq(3L), any(DocumentPutRequest.class));
        verify(documentService).patch(eq(7L), eq(12L), eq(3L), any(DocumentPatchRequest.class));
    }

    @Test
    void deleteDocumentReturnsNoContentAndPassesBothVersions() throws Exception {
        mockMvc.perform(delete("/api/clients/7/documents/12")
                        .header("X-Client-Version", "3")
                        .header("X-Document-Version", "4"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(documentService).delete(7L, 12L, 3L, 4L);
    }

    @Test
    void missingAndNegativeVersionInputsReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/clients/7/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"number":"DOC-12","expirationDate":"2030-01-01"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));

        mockMvc.perform(put("/api/clients/7/documents/12")
                        .header("X-Client-Version", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"version":-1,"number":"DOC-12","expirationDate":"2030-01-01"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));

        verifyNoInteractions(documentService);
    }

    @Test
    void requiredDocumentVersionHeadersRejectMissingAndMalformedValues() throws Exception {
        mockMvc.perform(post("/api/clients/7/documents")
                        .header("X-Client-Version", "not-a-version")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"number":"DOC-12","expirationDate":"2030-01-01"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        mockMvc.perform(put("/api/clients/7/documents/12")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"version":0,"number":"DOC-12","expirationDate":"2030-01-01"}
                                """))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/clients/7/documents/12")
                        .header("X-Client-Version", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"number":"DOC-12","expirationDate":"2030-01-01"}
                                """))
                .andExpect(status().isBadRequest());
        mockMvc.perform(patch("/api/clients/7/documents/12")
                        .header("X-Client-Version", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(delete("/api/clients/7/documents/12"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(delete("/api/clients/7/documents/12").header("X-Client-Version", "3"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(delete("/api/clients/7/documents/12")
                        .header("X-Client-Version", "not-a-version")
                        .header("X-Document-Version", "4"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(delete("/api/clients/7/documents/12")
                        .header("X-Client-Version", "3")
                        .header("X-Document-Version", "not-a-version"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(documentService);
    }

    @Test
    void malformedDocumentBodyVersionsReturnBadRequest() throws Exception {
        mockMvc.perform(put("/api/clients/7/documents/12")
                        .header("X-Client-Version", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"version":"bad","number":"DOC-12","expirationDate":"2030-01-01"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        mockMvc.perform(patch("/api/clients/7/documents/12")
                        .header("X-Client-Version", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":\"bad\",\"description\":\"updated\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        verifyNoInteractions(documentService);
    }

    @Test
    void negativeClientVersionReturnsBadRequestProblemDetails() throws Exception {
        when(documentService.create(eq(7L), eq(-1L), any(DocumentCreateRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Client version must not be negative"));

        mockMvc.perform(post("/api/clients/7/documents")
                        .header("X-Client-Version", "-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"number":"DOC-12","expirationDate":"2030-01-01"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400));
        verify(documentService).create(eq(7L), eq(-1L), any(DocumentCreateRequest.class));
    }

    @Test
    void serviceNotFoundAndConflictErrorsRemainProblemDetails() throws Exception {
        when(documentService.getById(7L, 99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
        doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Document number already in use"))
                .when(documentService).delete(7L, 12L, 3L, 4L);

        mockMvc.perform(get("/api/clients/7/documents/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404));
        mockMvc.perform(delete("/api/clients/7/documents/12")
                        .header("X-Client-Version", "3")
                        .header("X-Document-Version", "4"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(409));
    }

    private DocumentResponse response(Long id, Long clientId, Long version) {
        return new DocumentResponse(id, clientId, "DOC-" + id, "Passport", LocalDate.of(2030, 1, 1), version);
    }
}
