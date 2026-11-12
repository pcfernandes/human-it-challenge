package com.humanit.challenge.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.Instant;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanit.challenge.config.AppJwtProperties;
import com.humanit.challenge.config.StartupTestHarness;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private AppJwtProperties jwtProperties;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void supplyExternals(DynamicPropertyRegistry registry) {
        StartupTestHarness.supplyDevExternals(registry);
    }

    @Test
    void validCredentialsReturnSignedHs256JwtThatCanAccessProtectedEndpoint() throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(StartupTestHarness.DEMO_USERNAME, StartupTestHarness.TEST_PASSWORD_INPUT)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        String accessToken = response.path("accessToken").asText();
        assertThat(accessToken).isNotBlank();

        // The configured decoder verifies the JWT signature and rejects expired tokens.
        Jwt jwt = jwtDecoder.decode(accessToken);
        assertThat(jwt.getHeaders().get("alg")).isEqualTo("HS256");
        assertThat(jwt.getSubject()).isEqualTo(StartupTestHarness.DEMO_USERNAME);
        assertThat(jwt.getIssuedAt()).isNotNull();
        assertThat(jwt.getExpiresAt()).isEqualTo(jwt.getIssuedAt().plus(Duration.ofMinutes(jwtProperties.expirationMinutes())));
        assertThat(jwt.getExpiresAt()).isAfter(Instant.now());

        mockMvc.perform(get("/api/clients").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void authenticatedClientAndDocumentCrudPreservesPhaseTwoContracts() throws Exception {
        String bearerToken = "Bearer " + authenticatedToken();

        MvcResult createClientResult = mockMvc.perform(post("/api/clients")
                        .header("Authorization", bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Ada","lastName":"Lovelace","taxIdentifier":"T3.4-AUTH-CLIENT",
                                 "email":"t34-auth-client@example.com","phoneNumber":"123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Ada"))
                .andExpect(jsonPath("$.documents").isArray())
                .andReturn();
        JsonNode client = objectMapper.readTree(createClientResult.getResponse().getContentAsString());
        long clientId = client.path("id").asLong();
        long clientVersion = client.path("version").asLong();

        MvcResult clientListResult = mockMvc.perform(get("/api/clients")
                        .header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andReturn();
        assertThat(objectMapper.readTree(clientListResult.getResponse().getContentAsString())
                .path("content").findValuesAsText("taxIdentifier")).contains("T3.4-AUTH-CLIENT");

        mockMvc.perform(get("/api/clients/{id}", clientId).header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clientId));

        MvcResult putClientResult = mockMvc.perform(put("/api/clients/{id}", clientId)
                        .header("Authorization", bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Augusta","lastName":"Lovelace","taxIdentifier":"T3.4-AUTH-CLIENT",
                                 "email":"t34-auth-client@example.com","phoneNumber":"123",
                                 "version":%d,"documents":[]}
                                """.formatted(clientVersion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Augusta"))
                .andReturn();
        clientVersion = objectMapper.readTree(putClientResult.getResponse().getContentAsString())
                .path("version").asLong();

        MvcResult patchClientResult = mockMvc.perform(patch("/api/clients/{id}", clientId)
                        .header("Authorization", bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":%d,\"phoneNumber\":\"456\"}".formatted(clientVersion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phoneNumber").value("456"))
                .andReturn();
        clientVersion = objectMapper.readTree(patchClientResult.getResponse().getContentAsString())
                .path("version").asLong();

        MvcResult createDocumentResult = mockMvc.perform(post("/api/clients/{clientId}/documents", clientId)
                        .header("Authorization", bearerToken)
                        .header("X-Client-Version", clientVersion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"number":"T3.4-AUTH-DOC","description":"Initial","expirationDate":"2030-01-01"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clientId").value(clientId))
                .andExpect(jsonPath("$.number").value("T3.4-AUTH-DOC"))
                .andReturn();
        JsonNode document = objectMapper.readTree(createDocumentResult.getResponse().getContentAsString());
        long documentId = document.path("id").asLong();
        long documentVersion = document.path("version").asLong();

        MvcResult documentListResult = mockMvc.perform(get("/api/clients/{clientId}/documents", clientId)
                        .header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andReturn();
        JsonNode documentList = objectMapper.readTree(documentListResult.getResponse().getContentAsString());
        assertThat(documentList.path("content").findValuesAsText("number")).contains("T3.4-AUTH-DOC");
        assertThat(documentList.path("content").findValuesAsText("clientId")).contains(Long.toString(clientId));

        mockMvc.perform(get("/api/clients/{clientId}/documents/{documentId}", clientId, documentId)
                        .header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(documentId));

        MvcResult currentClientResult = mockMvc.perform(get("/api/clients/{id}", clientId)
                        .header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andReturn();
        clientVersion = objectMapper.readTree(currentClientResult.getResponse().getContentAsString())
                .path("version").asLong();

        MvcResult putDocumentResult = mockMvc.perform(put(
                                "/api/clients/{clientId}/documents/{documentId}", clientId, documentId)
                        .header("Authorization", bearerToken)
                        .header("X-Client-Version", clientVersion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"version":%d,"number":"T3.4-AUTH-DOC-PUT","description":"Replaced",
                                 "expirationDate":"2031-01-01"}
                                """.formatted(documentVersion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("T3.4-AUTH-DOC-PUT"))
                .andReturn();
        documentVersion = objectMapper.readTree(putDocumentResult.getResponse().getContentAsString())
                .path("version").asLong();

        currentClientResult = mockMvc.perform(get("/api/clients/{id}", clientId)
                        .header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andReturn();
        clientVersion = objectMapper.readTree(currentClientResult.getResponse().getContentAsString())
                .path("version").asLong();

        MvcResult patchDocumentResult = mockMvc.perform(patch(
                                "/api/clients/{clientId}/documents/{documentId}", clientId, documentId)
                        .header("Authorization", bearerToken)
                        .header("X-Client-Version", clientVersion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":%d,\"description\":\"Patched\"}".formatted(documentVersion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Patched"))
                .andReturn();
        documentVersion = objectMapper.readTree(patchDocumentResult.getResponse().getContentAsString())
                .path("version").asLong();

        currentClientResult = mockMvc.perform(get("/api/clients/{id}", clientId)
                        .header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andReturn();
        clientVersion = objectMapper.readTree(currentClientResult.getResponse().getContentAsString())
                .path("version").asLong();

        mockMvc.perform(delete("/api/clients/{clientId}/documents/{documentId}", clientId, documentId)
                        .header("Authorization", bearerToken)
                        .header("X-Client-Version", clientVersion)
                        .header("X-Document-Version", documentVersion))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/clients/{clientId}/documents/{documentId}", clientId, documentId)
                        .header("Authorization", bearerToken))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));

        currentClientResult = mockMvc.perform(get("/api/clients/{id}", clientId)
                        .header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andReturn();
        clientVersion = objectMapper.readTree(currentClientResult.getResponse().getContentAsString())
                .path("version").asLong();
        mockMvc.perform(delete("/api/clients/{id}", clientId)
                        .header("Authorization", bearerToken)
                        .header("X-Client-Version", clientVersion))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/clients/{id}", clientId).header("Authorization", bearerToken))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void invalidCredentialsReturnGenericUnauthorizedProblem() throws Exception {
        String badPasswordResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(StartupTestHarness.DEMO_USERNAME, "wrong-test-password")))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andReturn().getResponse().getContentAsString();

        String unknownUserResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("unknown-test-user", StartupTestHarness.TEST_PASSWORD_INPUT)))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andReturn().getResponse().getContentAsString();

        assertThat(badPasswordResponse).contains("Invalid username or password.")
                .doesNotContain(StartupTestHarness.DEMO_USERNAME, "wrong-test-password",
                        StartupTestHarness.TEST_PASSWORD_INPUT, StartupTestHarness.TEST_BCRYPT_HASH);
        assertThat(unknownUserResponse).contains("Invalid username or password.")
                .doesNotContain("unknown-test-user", StartupTestHarness.TEST_PASSWORD_INPUT,
                        StartupTestHarness.TEST_BCRYPT_HASH);
    }

    private String loginJson(String username, String password) throws Exception {
        return objectMapper.writeValueAsString(new LoginPayload(username, password));
    }

    private String authenticatedToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(StartupTestHarness.DEMO_USERNAME, StartupTestHarness.TEST_PASSWORD_INPUT)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("accessToken").asText();
    }

    private record LoginPayload(String username, String password) {
    }
}
