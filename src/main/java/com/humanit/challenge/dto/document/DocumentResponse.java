package com.humanit.challenge.dto.document;

import java.time.LocalDate;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response contract for a Document. Exposes the owning Client only as the scalar
 * {@code clientId}, so Client/Document response graphs are never recursive.
 */
@Schema(description = "Document representation. Ownership is exposed as scalar clientId; the response is not recursive.")
public record DocumentResponse(
        Long id,
        Long clientId,
        String number,
        String description,
        LocalDate expirationDate,
        Long version) {
}
