package com.humanit.challenge.dto.document;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Request contract for a full replacement of a Document's editable fields
 * ({@code PUT /clients/{clientId}/documents/{documentId}}). The expected Document
 * version is required in the body; the expected Client version travels in the
 * {@code X-Client-Version} header. {@code description} is optional and may be null.
 */
public class DocumentPutRequest {

    @NotNull
    @PositiveOrZero
    private Long version;

    @NotBlank
    @Size(max = 100)
    private String number;

    @Size(max = 1000)
    private String description;

    @NotNull
    private LocalDate expirationDate;

    public DocumentPutRequest() {
        // Required by Jackson.
    }

    public DocumentPutRequest(Long version, String number, String description, LocalDate expirationDate) {
        this.version = version;
        this.number = number;
        this.description = description;
        this.expirationDate = expirationDate;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
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
