package com.humanit.challenge.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

class SecurityProblemHandlersTest {

    @Test
    void forbiddenHandlerReturnsSafeProblemDetailsWithoutExceptionContent() throws Exception {
        String secret = "sensitive-token-value";
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/clients");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new SecurityProblemHandlers(new ObjectMapper()).accessDeniedHandler()
                .handle(request, response, new AccessDeniedException(secret));

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).startsWith("application/problem+json");
        assertThat(response.getContentAsString()).contains("Forbidden", "not permitted")
                .doesNotContain(secret);
    }
}
