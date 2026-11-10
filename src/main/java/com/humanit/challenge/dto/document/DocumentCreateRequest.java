package com.humanit.challenge.dto.document;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request contract for creating a Document under an existing Client
 * ({@code POST /clients/{clientId}/documents}). The expected Client version travels in
 * the {@code X-Client-Version} header and is therefore not part of this body.
 */
public class DocumentCreateRequest {

    @NotBlank
    @Size(max = 100)
    private String number;

    @Size(max = 1000)
    private String description;

    @NotNull
    private LocalDate expirationDate;

    public DocumentCreateRequest() {
        // Required by Jackson.
    }

    public DocumentCreateRequest(String number, String description, LocalDate expirationDate) {
        this.number = number;
        this.description = description;
        this.expirationDate = expirationDate;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }
}
