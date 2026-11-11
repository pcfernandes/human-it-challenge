package com.humanit.challenge.security;

import java.net.URI;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

/** Writes generic Problem Details for failures raised before MVC controller handling. */
final class SecurityProblemHandlers {

    private final ObjectMapper objectMapper;

    SecurityProblemHandlers(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, exception) -> {
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
            writeProblem(response, HttpStatus.UNAUTHORIZED, "Unauthorized",
                    "Authentication is required to access this resource.", request.getRequestURI());
        };
    }

    AccessDeniedHandler accessDeniedHandler() {
        return (request, response, exception) -> writeProblem(response, HttpStatus.FORBIDDEN, "Forbidden",
                "You are not permitted to access this resource.", request.getRequestURI());
    }

    private void writeProblem(jakarta.servlet.http.HttpServletResponse response, HttpStatus status,
            String title, String detail, String requestUri) throws java.io.IOException {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setInstance(URI.create(requestUri));

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), problem);
    }
}
