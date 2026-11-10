package com.humanit.challenge.dto.document;

import java.time.LocalDate;

/**
 * Response contract for a Document. Exposes the owning Client only as the scalar
 * {@code clientId}, so Client/Document response graphs are never recursive.
 */
public record DocumentResponse(
        Long id,
        Long clientId,
        String number,
        String description,
        LocalDate expirationDate,
        Long version) {
}
