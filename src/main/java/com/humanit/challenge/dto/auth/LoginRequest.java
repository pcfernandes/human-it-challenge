package com.humanit.challenge.dto.auth;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Login credentials. Both username and password are required and nonblank.")
public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password) {
}
