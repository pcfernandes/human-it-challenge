package com.humanit.challenge.dto.client;

import java.util.List;
import java.util.Objects;

import com.humanit.challenge.dto.patch.PatchRequired;
import com.humanit.challenge.dto.patch.PatchValue;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request contract for a partial update of a Client ({@code PATCH /clients/{id}}).
 *
 * <p>{@code version} is the expected Client version and is required.
 *
 * <p>Scalar semantics: an omitted property keeps the existing value; an explicit {@code null}
 * is invalid for every Client scalar.
 *
 * <p>{@code documents} semantics: an omitted or empty collection preserves all existing
 * Documents; an explicit {@code documents: null} is invalid. Supplied entries are validated
 * through {@link ClientDocumentPatchRequest}. No implicit Document deletion or ownership
 * transfer is expressed by this contract.
 */
@Schema(description = "Partial Client update input. Omitted fields remain unchanged. Required scalar nulls are invalid. "
        + "Omitted/empty documents preserves existing Documents; documents:null is invalid.")
public class ClientPatchRequest {

    @PatchRequired
    @Schema(implementation = String.class, minLength = 1, maxLength = 100, description = "Optional patch value; omitted preserves the current value, null/blank is invalid.")
    private PatchValue<@NotBlank @Size(max = 100) String> firstName = PatchValue.absent();

    @PatchRequired
    @Schema(implementation = String.class, minLength = 1, maxLength = 100, description = "Optional patch value; omitted preserves the current value, null/blank is invalid.")
    private PatchValue<@NotBlank @Size(max = 100) String> lastName = PatchValue.absent();

    @PatchRequired
    @Schema(implementation = String.class, minLength = 1, maxLength = 50, description = "Optional patch value; omitted preserves the current value, null/blank is invalid.")
    private PatchValue<@NotBlank @Size(max = 50) String> taxIdentifier = PatchValue.absent();

    @PatchRequired
    @Schema(implementation = String.class, format = "email", minLength = 1, maxLength = 255, description = "Optional patch value; omitted preserves the current value, null/blank or invalid email is rejected.")
    private PatchValue<@NotBlank @Email @Size(max = 255) String> email = PatchValue.absent();

    @PatchRequired
    @Schema(implementation = String.class, minLength = 1, maxLength = 50, description = "Optional patch value; omitted preserves the current value, null/blank is invalid.")
    private PatchValue<@NotBlank @Size(max = 50) String> phoneNumber = PatchValue.absent();

    @NotNull
    @PositiveOrZero
    private Long version;

    @PatchRequired
    @Schema(implementation = ClientDocumentPatchRequest[].class, description = "Optional patch value; omitted or an empty array preserves Documents, null is invalid.")
    private PatchValue<List<@NotNull @Valid ClientDocumentPatchRequest>> documents = PatchValue.absent();

    public ClientPatchRequest() {
        // Required by Jackson.
    }

    public PatchValue<String> getFirstName() {
        return firstName;
    }

    public void setFirstName(PatchValue<String> firstName) {
        this.firstName = Objects.requireNonNull(firstName, "firstName patch value must not be null");
    }

    public PatchValue<String> getLastName() {
        return lastName;
    }

    public void setLastName(PatchValue<String> lastName) {
        this.lastName = Objects.requireNonNull(lastName, "lastName patch value must not be null");
    }

    public PatchValue<String> getTaxIdentifier() {
        return taxIdentifier;
    }

    public void setTaxIdentifier(PatchValue<String> taxIdentifier) {
        this.taxIdentifier = Objects.requireNonNull(taxIdentifier, "taxIdentifier patch value must not be null");
    }

    public PatchValue<String> getEmail() {
        return email;
    }

    public void setEmail(PatchValue<String> email) {
        this.email = Objects.requireNonNull(email, "email patch value must not be null");
    }

    public PatchValue<String> getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(PatchValue<String> phoneNumber) {
        this.phoneNumber = Objects.requireNonNull(phoneNumber, "phoneNumber patch value must not be null");
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public PatchValue<List<ClientDocumentPatchRequest>> getDocuments() {
        return documents;
    }

    public void setDocuments(PatchValue<List<ClientDocumentPatchRequest>> documents) {
        this.documents = Objects.requireNonNull(documents, "documents patch value must not be null");
    }
}
