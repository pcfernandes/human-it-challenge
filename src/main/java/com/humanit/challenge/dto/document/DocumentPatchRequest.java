package com.humanit.challenge.dto.document;

import java.time.LocalDate;
import java.util.Objects;

import com.humanit.challenge.dto.patch.PatchRequired;
import com.humanit.challenge.dto.patch.PatchValue;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Request contract for a partial update of a Document
 * ({@code PATCH /clients/{clientId}/documents/{documentId}}).
 *
 * <p>{@code version} is the expected Document version and is required. The expected Client
 * version travels in the {@code X-Client-Version} header and is not part of this body.
 *
 * <p>Scalar semantics: an omitted property keeps the existing value; an explicit {@code null}
 * is invalid for {@code number} and {@code expirationDate}; an explicit {@code null} for
 * {@code description} clears it.
 */
public class DocumentPatchRequest {

    @NotNull
    @PositiveOrZero
    private Long version;

    @PatchRequired
    private PatchValue<@NotBlank @Size(max = 100) String> number = PatchValue.absent();

    private PatchValue<@Size(max = 1000) String> description = PatchValue.absent();

    @PatchRequired
    private PatchValue<LocalDate> expirationDate = PatchValue.absent();

    public DocumentPatchRequest() {
        // Required by Jackson.
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
}
