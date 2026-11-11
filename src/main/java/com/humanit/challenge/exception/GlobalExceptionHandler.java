package com.humanit.challenge.exception;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.persistence.OptimisticLockException;

/**
 * Cross-cutting RFC 9457 (Problem Details) error handling for all controllers.
 *
 * <p>Extends {@link ResponseEntityExceptionHandler} so that framework-raised HTTP
 * exceptions (validation, malformed body, type mismatch, not found, method not
 * allowed, ...) keep their intended status codes instead of being collapsed into
 * a generic 500. Only the genuinely unexpected case falls through to the
 * catch-all handler.</p>
 *
 * <p>No stack traces, exception class names, SQL details, secrets or rejected
 * values are ever copied to a response body. Unexpected exceptions are logged
 * server-side only.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ProblemDetail buildProblem(HttpStatus status, String title, String detail, WebRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setInstance(URI.create(extractPath(request)));
        return problem;
    }

    private String extractPath(WebRequest request) {
        String description = request.getDescription(false);
        if (description != null && description.startsWith("uri=")) {
            return description.substring(4);
        }
        return description == null ? "" : description;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail problem = buildProblem(HttpStatus.BAD_REQUEST, "Validation failed",
                "Request validation failed.", request);
        List<Map<String, String>> errors = new ArrayList<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            Map<String, String> entry = new LinkedHashMap<>();
            entry.put("field", fe.getField());
            entry.put("message", fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value");
            errors.add(entry);
        }
        problem.setProperty("errors", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            org.springframework.http.converter.HttpMessageNotReadableException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        ProblemDetail problem = buildProblem(HttpStatus.BAD_REQUEST, "Bad request",
                "Malformed or unreadable request body.", request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            org.springframework.beans.TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String paramName = (ex instanceof MethodArgumentTypeMismatchException matm) ? matm.getName() : "parameter";
        ProblemDetail problem = buildProblem(HttpStatus.BAD_REQUEST, "Bad request",
                "Invalid value for parameter '" + paramName + "'.", request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(
            org.springframework.web.servlet.resource.NoResourceFoundException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        ProblemDetail problem = buildProblem(HttpStatus.NOT_FOUND, "Not found",
                "The requested resource was not found.", request);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            org.springframework.web.HttpRequestMethodNotSupportedException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        ProblemDetail problem = buildProblem(HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed",
                "The HTTP method is not supported for this endpoint.", request);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        ProblemDetail problem = buildProblem(HttpStatus.BAD_REQUEST, "Validation failed",
                "Request validation failed.", request);
        List<Map<String, String>> errors = new ArrayList<>();
        for (ConstraintViolation<?> v : ex.getConstraintViolations()) {
            Map<String, String> entry = new LinkedHashMap<>();
            entry.put("field", v.getPropertyPath().toString());
            entry.put("message", v.getMessage());
            errors.add(entry);
        }
        problem.setProperty("errors", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    public ResponseEntity<ProblemDetail> handleResponseStatus(
            org.springframework.web.server.ResponseStatusException ex, WebRequest request) {
        HttpStatusCode statusCode = ex.getStatusCode();
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String detail = (ex.getReason() != null && !ex.getReason().isBlank())
                ? ex.getReason()
                : status.getReasonPhrase();
        ProblemDetail problem = buildProblem(status, status.getReasonPhrase(), detail, request);
        return ResponseEntity.status(statusCode)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler({OptimisticLockingFailureException.class, OptimisticLockException.class})
    public ResponseEntity<ProblemDetail> handleOptimisticLock(Exception ex, WebRequest request) {
        ProblemDetail problem = buildProblem(HttpStatus.CONFLICT, "Conflict",
                "The resource was modified concurrently. Please retry the request.", request);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception ex, WebRequest request) {
        log.error("Unexpected error while handling request", ex);
        ProblemDetail problem = buildProblem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error",
                "An unexpected error occurred.", request);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }
}
