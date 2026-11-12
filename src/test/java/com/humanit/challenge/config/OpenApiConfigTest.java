package com.humanit.challenge.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import io.swagger.v3.oas.models.OpenAPI;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiConfigTest {

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        StartupTestHarness.supplyDevExternals(registry);
    }

    @Autowired
    private OpenAPI openAPI;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void openApiBeanExistsWithExpectedMetadata() {
        assertThat(openAPI).isNotNull();
        assertThat(openAPI.getInfo()).isNotNull();
        assertThat(openAPI.getInfo().getTitle()).isNotBlank();
        assertThat(openAPI.getInfo().getVersion()).isNotBlank();
        assertThat(openAPI.getInfo().getDescription()).isNotBlank();
    }

    @Test
    void generatedDocumentCoversAllOperationsAndDeclaresBearerSecurity() throws Exception {
        String body = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andReturn().getResponse().getContentAsString();
        JsonNode document = objectMapper.readTree(body);

        JsonNode paths = document.path("paths");
        assertThat(countBusinessOperations(paths)).isEqualTo(13);
        assertThat(document.path("components").path("securitySchemes").path("bearerAuth").path("type").asText())
                .isEqualTo("http");
        assertThat(document.path("components").path("securitySchemes").path("bearerAuth").path("scheme").asText())
                .isEqualTo("bearer");
        assertThat(document.path("components").path("securitySchemes").path("bearerAuth").path("bearerFormat").asText())
                .isEqualTo("JWT");

        paths.fields().forEachRemaining(path -> path.getValue().fields().forEachRemaining(operation -> {
            if (path.getKey().startsWith("/__test/")) {
                return;
            }
            if ("/auth/login".equals(path.getKey())) {
                assertThat(operation.getValue().has("security")).isFalse();
            } else {
                assertThat(operation.getValue().path("security").toString())
                        .contains("bearerAuth");
            }
        }));
    }

    @Test
    void patchSchemasExposeWireValuesAndSwaggerUiIsPublic() throws Exception {
        String body = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode schemas = objectMapper.readTree(body).path("components").path("schemas");

        assertThat(schemas.path("ClientPatchRequest").path("properties").path("firstName").path("type").asText())
                .isEqualTo("string");
        assertThat(schemas.path("ClientPatchRequest").path("properties").path("documents").path("type").asText())
                .isEqualTo("array");
        assertThat(schemas.path("ClientDocumentPatchRequest").path("properties").path("description").path("type").asText())
                .isEqualTo("string");
        assertThat(schemas.path("DocumentPatchRequest").path("properties").path("expirationDate").path("type").asText())
                .isEqualTo("string");
        assertThat(schemas.path("ClientPatchRequest").path("properties").path("firstName").path("maxLength").asInt())
                .isEqualTo(100);
        assertThat(schemas.has("PatchValue")).isFalse();

        JsonNode document = objectMapper.readTree(body);
        JsonNode clientCreate = document.path("paths").path("/api/clients").path("post");
        assertThat(clientCreate.path("requestBody").path("content").path("application/json")
                .path("schema").path("$ref").asText()).isEqualTo("#/components/schemas/ClientCreateRequest");
        assertThat(clientCreate.path("responses").path("201").path("content").path("*/*")
                .path("schema").path("$ref").asText()).isEqualTo("#/components/schemas/ClientResponse");
        assertThat(schemas.path("ClientCreateRequest").path("properties").path("firstName").path("minLength").asInt())
                .isEqualTo(1);
        assertThat(schemas.path("ClientPutRequest").path("required").toString()).doesNotContain("documents");

        JsonNode validationProblem = clientCreate.path("responses").path("400");
        assertThat(validationProblem.path("content").has("application/problem+json")).isTrue();
        assertThat(validationProblem.path("content").path("application/problem+json").path("schema")
                .path("$ref").asText()).isEqualTo("#/components/schemas/ProblemDetail");

        JsonNode documentDelete = document.path("paths").path("/api/clients/{clientId}/documents/{documentId}")
                .path("delete").path("parameters");
        assertThat(hasParameter(documentDelete, "X-Client-Version")).isTrue();
        assertThat(hasParameter(documentDelete, "X-Document-Version")).isTrue();

        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    void generatedApiDocumentsCanonicalPathsLoginConstraintsAndLoginServerError() throws Exception {
        String body = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode document = objectMapper.readTree(body);
        JsonNode paths = document.path("paths");

        assertThat(paths.path("/api/clients").has("get")).isTrue();
        assertThat(paths.path("/api/clients").has("post")).isTrue();
        assertThat(paths.path("/api/clients/{id}").has("get")).isTrue();
        assertThat(paths.path("/api/clients/{id}").has("put")).isTrue();
        assertThat(paths.path("/api/clients/{id}").has("patch")).isTrue();
        assertThat(paths.path("/api/clients/{id}").has("delete")).isTrue();
        assertThat(paths.path("/api/clients/{clientId}/documents").has("get")).isTrue();
        assertThat(paths.path("/api/clients/{clientId}/documents").has("post")).isTrue();
        assertThat(paths.path("/api/clients/{clientId}/documents/{documentId}").has("get")).isTrue();
        assertThat(paths.path("/api/clients/{clientId}/documents/{documentId}").has("put")).isTrue();
        assertThat(paths.path("/api/clients/{clientId}/documents/{documentId}").has("patch")).isTrue();
        assertThat(paths.path("/api/clients/{clientId}/documents/{documentId}").has("delete")).isTrue();

        JsonNode schemas = document.path("components").path("schemas");
        JsonNode loginSchema = schemas.path("LoginRequest");
        assertThat(loginSchema.path("properties").path("username").path("minLength").asInt())
                .isGreaterThanOrEqualTo(1);
        assertThat(loginSchema.path("properties").path("password").path("minLength").asInt())
                .isGreaterThanOrEqualTo(1);
        assertThat(loginSchema.path("required").toString()).contains("username", "password");

        JsonNode loginServerError = paths.path("/auth/login").path("post").path("responses").path("500");
        assertThat(loginServerError.path("content").path("application/problem+json").path("schema")
                .path("$ref").asText()).isEqualTo("#/components/schemas/ProblemDetail");
    }

    private int countBusinessOperations(JsonNode paths) {
        int count = 0;
        var pathIterator = paths.fields();
        while (pathIterator.hasNext()) {
            var path = pathIterator.next();
            if (!path.getKey().startsWith("/api/") && !"/auth/login".equals(path.getKey())) {
                continue;
            }
            var operations = path.getValue().fields();
            while (operations.hasNext()) {
                operations.next();
                count++;
            }
        }
        return count;
    }

    private boolean hasParameter(JsonNode parameters, String expectedName) {
        for (JsonNode parameter : parameters) {
            if (expectedName.equals(parameter.path("name").asText())) {
                return true;
            }
        }
        return false;
    }
}
