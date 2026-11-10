package com.humanit.challenge.dto.client;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * One Document entry inside {@code ClientPutRequest.documents}.
 *
 * <p>Every supplied entry carries a valid {@code number} and {@code expirationDate}.
 * Identity discriminates the operation: an existing Document supplies both {@code id}
 * and its expected {@code version}; a new Document supplies neither. A partial
 * {@code id}/{@code version} pair is invalid. Ownership resolution, unknown-ID handling
 * and version checking are performed later by the service layer, not here.
 */
public class ClientDocumentPutRequest {

    private Long id;

    @PositiveOrZero
    private Long version;

    @NotBlank
    @Size(max = 100)
    private String number;

    @Size(max = 1000)
    private String description;

    @NotNull
    private LocalDate expirationDate;

    public ClientDocumentPutRequest() {
        // Required by Jackson.
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    /**
     * True when both {@code id} and {@code version} are absent (new Document) or both are
     * present (existing Document). A single supplied value is rejected.
     */
    @JsonIgnore
    @AssertTrue(message = "id and version must both be supplied for an existing document, or both omitted for a new document")
    public boolean isIdentityPairConsistent() {
        return (id == null) == (version == null);
    }
}
