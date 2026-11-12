package com.humanit.challenge.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Bearer access token returned after successful login.")
public record TokenResponse(String accessToken, String tokenType) {
}
