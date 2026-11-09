package com.humanit.challenge.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Test-only controller used by {@link GlobalExceptionHandlerTest}. Lives under
 * {@code src/test} and must never be shipped in production source.
 */
@RestController
@RequestMapping("/__test/errors")
@Validated
public class TestErrorController {

    public record SampleRequest(@NotBlank String name, @Email String email) {
    }

    @PostMapping("/validate")
    public ResponseEntity<String> validate(@Valid @RequestBody SampleRequest body) {
        return ResponseEntity.ok("ok");
    }

    @GetMapping("/type-mismatch/{id}")
    public ResponseEntity<String> typeMismatch(@PathVariable Long id) {
        return ResponseEntity.ok("id=" + id);
    }

    @GetMapping("/throw-runtime")
    public ResponseEntity<String> throwRuntime() {
        throw new RuntimeException("secret internal message");
    }

    @GetMapping("/throw-response-status")
    public ResponseEntity<String> throwResponseStatus() {
        throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Resource not found");
    }

    @GetMapping("/constraint")
    public ResponseEntity<String> constraint(@RequestParam(required = false) @Min(1) Integer q) {
        return ResponseEntity.ok("ok");
    }
}
