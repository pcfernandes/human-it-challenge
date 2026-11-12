package com.humanit.challenge.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import com.humanit.challenge.config.StartupTestHarness;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigurationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @DynamicPropertySource
    static void supplyExternals(DynamicPropertyRegistry registry) {
        StartupTestHarness.supplyDevExternals(registry);
    }

    @Test
    void acceptsValidSignedJwtAndDoesNotCreateSession() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/clients")
                        .header("Authorization", "Bearer " + token(Instant.now().plusSeconds(120))))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result.getRequest().getSession(false)).isNull();
        assertThat(result.getResponse().getCookie("JSESSIONID")).isNull();
    }

    @Test
    void rejectsJwtSignedWithDifferentSecretWithoutLeakingSensitiveValues() throws Exception {
        String invalidToken = new org.springframework.security.oauth2.jwt.NimbusJwtEncoder(
                new com.nimbusds.jose.jwk.source.ImmutableSecret<>("another-secret-key-with-32-bytes".getBytes()))
                .encode(JwtEncoderParameters.from(
                        JwsHeader.with(MacAlgorithm.HS256).build(), claims(Instant.now().plusSeconds(120))))
                .getTokenValue();

        String body = mockMvc.perform(get("/api/clients").header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andReturn().getResponse().getContentAsString();

        assertThat(body).doesNotContain(invalidToken, StartupTestHarness.VALID_SECRET_32_BYTES, "another-secret");
    }

    @Test
    void rejectsExpiredJwt() throws Exception {
        mockMvc.perform(get("/api/clients")
                        .header("Authorization", "Bearer " + token(Instant.now().minusSeconds(30))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointWithoutJwtReturnsSafeUnauthorizedProblem() throws Exception {
        String body = mockMvc.perform(get("/api/clients"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andReturn().getResponse().getContentAsString();

        assertThat(body).contains("Unauthorized", "Authentication is required")
                .doesNotContain(StartupTestHarness.VALID_SECRET_32_BYTES,
                        StartupTestHarness.TEST_PASSWORD_INPUT, StartupTestHarness.TEST_BCRYPT_HASH, "accessToken");
    }

    @Test
    void demoUserCredentialMatchesWithBcryptAndRejectsWrongPassword() {
        String storedHash = userDetailsService.loadUserByUsername(StartupTestHarness.DEMO_USERNAME).getPassword();
        assertThat(storedHash).startsWith("$2");
        assertThat(passwordEncoder.matches(StartupTestHarness.TEST_PASSWORD_INPUT, storedHash)).isTrue();
        assertThat(passwordEncoder.matches("incorrect-test-input", storedHash)).isFalse();

        assertThat(authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(
                StartupTestHarness.DEMO_USERNAME, StartupTestHarness.TEST_PASSWORD_INPUT)).isAuthenticated()).isTrue();
        assertThatThrownBy(() -> authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(
                StartupTestHarness.DEMO_USERNAME, "incorrect-test-input")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void loginAndOpenApiRoutesArePublicBeforeLoginEndpointIsImplemented() throws Exception {
        mockMvc.perform(post("/auth/login"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    private String token(Instant expiresAt) {
        return jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims(expiresAt))).getTokenValue();
    }

    private JwtClaimsSet claims(Instant expiresAt) {
        Instant issuedAt = expiresAt.isBefore(Instant.now())
                ? expiresAt.minusSeconds(60)
                : Instant.now().minusSeconds(1);
        return JwtClaimsSet.builder()
                .issuer("human-it-challenge-test")
                .subject("test-user")
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .build();
    }
}
