package com.humanit.challenge.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    private record LoginPayload(String username, String password) {
    }
}
