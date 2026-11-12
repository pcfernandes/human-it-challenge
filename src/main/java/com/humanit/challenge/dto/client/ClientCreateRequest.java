package com.humanit.challenge.dto.client;

import java.util.ArrayList;
import java.util.List;

import com.humanit.challenge.dto.document.DocumentCreateRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request contract for {@code POST /clients}. No version is required on creation.
 * {@code documents} is optional: an omitted collection, an empty collection and an
 * explicit {@code documents: null} are all treated as "no Documents to create".
 */
@Schema(description = "Client creation input. Required text fields are nonblank and bounded; email must be valid. "
        + "The optional documents collection creates initial owned Documents. On POST only, null is treated as omitted.")
public class ClientCreateRequest {

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
    @Schema(minLength = 1, maxLength = 50, description = "Required nonblank phone number; no country-specific format is imposed.")
    @Size(max = 50)
    private String phoneNumber;

    private List<@NotNull @Valid DocumentCreateRequest> documents = new ArrayList<>();

    public ClientCreateRequest() {
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

    public List<DocumentCreateRequest> getDocuments() {
        return documents;
    }

    /**
     * POST-only rule: an explicit {@code null} collection is normalized to an empty list,
     * which is equivalent to an omitted collection.
     */
    public void setDocuments(List<DocumentCreateRequest> documents) {
        this.documents = documents == null ? new ArrayList<>() : documents;
    }
}
