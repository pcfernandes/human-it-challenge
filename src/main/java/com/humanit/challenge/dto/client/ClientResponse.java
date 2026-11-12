package com.humanit.challenge.dto.client;

import java.util.List;

import com.humanit.challenge.dto.document.DocumentResponse;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response contract for a Client, embedding its Documents. Producing this response
 * requires the Documents association to be loaded intentionally by the calling use case;
 * mappers never trigger lazy loading or repository access.
 */
@Schema(description = "Client representation, including its owned Documents and optimistic-lock version.")
public record ClientResponse(
        Long id,
        String firstName,
        String lastName,
        String taxIdentifier,
        String email,
        String phoneNumber,
        Long version,
        List<DocumentResponse> documents) {
}
