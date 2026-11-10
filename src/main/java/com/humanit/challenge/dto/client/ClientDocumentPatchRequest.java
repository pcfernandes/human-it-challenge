package com.humanit.challenge.dto.client;

import java.time.LocalDate;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.humanit.challenge.dto.patch.PatchRequired;
import com.humanit.challenge.dto.patch.PatchValue;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * One Document entry inside {@code ClientPatchRequest.documents}.
 *
 * <p>Identity discriminates the operation:
 * <ul>
 *   <li>Existing Document: {@code id} and expected {@code version} are both supplied. Scalar
 *       fields use partial-update semantics (omitted values remain unchanged).</li>
 *   <li>New Document: neither {@code id} nor {@code version} is supplied. {@code number} and
 *       {@code expirationDate} must then be supplied with a value.</li>
 * </ul>
 * A partial {@code id}/{@code version} pair is invalid. Ownership, unknown-ID handling,
 * version comparison and aggregate merge belong to the service layer and are not checked here.
 */
public class ClientDocumentPatchRequest {

    private Long id;

    @PositiveOrZero
    private Long version;

    @PatchRequired
    private PatchValue<@NotBlank @Size(max = 100) String> number = PatchValue.absent();

    private PatchValue<@Size(max = 1000) String> description = PatchValue.absent();

    @PatchRequired
    private PatchValue<LocalDate> expirationDate = PatchValue.absent();

    public ClientDocumentPatchRequest() {
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

    public PatchValue<String> getNumber() {
        return number;
    }

    public void setNumber(PatchValue<String> number) {
        this.number = Objects.requireNonNull(number, "number patch value must not be null");
    }

    public PatchValue<String> getDescription() {
        return description;
    }

    public void setDescription(PatchValue<String> description) {
        this.description = Objects.requireNonNull(description, "description patch value must not be null");
    }

    public PatchValue<LocalDate> getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(PatchValue<LocalDate> expirationDate) {
        this.expirationDate = Objects.requireNonNull(expirationDate, "expirationDate patch value must not be null");
    }

    /** Both {@code id} and {@code version} are present, or both are absent. */
    @JsonIgnore
    @AssertTrue(message = "id and version must both be supplied for an existing document, or both omitted for a new document")
    public boolean isIdentityPairConsistent() {
        return (id == null) == (version == null);
    }

    /** A new Document (no identity supplied) must provide {@code number} and {@code expirationDate}. */
    @JsonIgnore
    @AssertTrue(message = "number and expirationDate must be supplied with a value for a new document")
    public boolean isNewDocumentFieldsComplete() {
        if (id != null || version != null) {
            return true;
        }
        return number.isPresentValue() && expirationDate.isPresentValue();
    }
}
