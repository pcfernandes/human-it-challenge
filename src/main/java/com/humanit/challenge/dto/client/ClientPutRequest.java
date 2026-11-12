package com.humanit.challenge.dto.client;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request contract for a full replacement of editable Client fields
 * ({@code PUT /clients/{id}}).
 *
 * <p>{@code version} is the expected Client version and is required. {@code documents}
 * follows collection-preserving semantics: an omitted or empty collection preserves all
 * existing Documents, while an explicit {@code documents: null} is invalid. The field
 * is initialized to an empty list so that an omitted property keeps that default, and
 * Jackson's explicit-null binding ({@code setDocuments(null)}) is what {@code @NotNull}
 * rejects.
 */
@Schema(description = "Full replacement input for editable Client fields. Requires a nonnegative expected version. "
        + "Documents are collection-preserving: omitted or empty preserves current Documents; explicit null is invalid.")
public class ClientPutRequest {

    @NotBlank
    @Schema(minLength = 1, maxLength = 100, description = "Required nonblank given name.")
    @Size(max = 100)
    private String firstName;

    @NotBlank
    @Schema(minLength = 1, maxLength = 100, description = "Required nonblank family name.")
    @Size(max = 100)
    private String lastName;

    @NotBlank
    @Schema(minLength = 1, maxLength = 50, description = "Required globally unique tax identifier.")
    @Size(max = 50)
    private String taxIdentifier;

    @NotBlank
    @Schema(minLength = 1, maxLength = 255, format = "email", description = "Required valid email address.")
    @Email
    @Size(max = 255)
    private String email;

    @NotBlank
    @Schema(minLength = 1, maxLength = 50, description = "Required nonblank phone number.")
    @Size(max = 50)
    private String phoneNumber;

    @NotNull
    @PositiveOrZero
    private Long version;

    @NotNull
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            description = "Optional collection. Omission or an empty array preserves all existing Documents; null is invalid.")
    private List<@NotNull @Valid ClientDocumentPutRequest> documents = new ArrayList<>();

    public ClientPutRequest() {
        // Required by Jackson.
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getTaxIdentifier() {
        return taxIdentifier;
    }

    public void setTaxIdentifier(String taxIdentifier) {
        this.taxIdentifier = taxIdentifier;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public List<ClientDocumentPutRequest> getDocuments() {
        return documents;
    }

    /**
     * Stores the supplied value unchanged. An explicit {@code null} must remain visible
     * to Bean Validation so that it can be rejected.
     */
    public void setDocuments(List<ClientDocumentPutRequest> documents) {
        this.documents = documents;
    }
}
