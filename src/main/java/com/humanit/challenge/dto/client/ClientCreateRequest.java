package com.humanit.challenge.dto.client;

import java.util.ArrayList;
import java.util.List;

import com.humanit.challenge.dto.document.DocumentCreateRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request contract for {@code POST /clients}. No version is required on creation.
 * {@code documents} is optional: an omitted collection, an empty collection and an
 * explicit {@code documents: null} are all treated as "no Documents to create".
 */
public class ClientCreateRequest {

    @NotBlank
    @Size(max = 100)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    private String lastName;

    @NotBlank
    @Size(max = 50)
    private String taxIdentifier;

    @NotBlank
    @Email
    @Size(max = 255)
    private String email;

    @NotBlank
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
