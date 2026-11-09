package com.humanit.challenge.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

@Entity
@Table(name = "clients", uniqueConstraints = @UniqueConstraint(
        name = "uk_clients_tax_identifier", columnNames = "tax_identifier"))
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "tax_identifier", nullable = false, length = 50)
    private String taxIdentifier;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "phone_number", nullable = false, length = 50)
    private String phoneNumber;

    @OneToMany(mappedBy = "client", cascade = {
            CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH
    }, fetch = FetchType.LAZY)
    private List<Document> documents = new ArrayList<>();

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected Client() {
        // Required by JPA.
    }

    public Client(String firstName, String lastName, String taxIdentifier, String email, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.taxIdentifier = taxIdentifier;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public Long getId() {
        return id;
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

    public List<Document> getDocuments() {
        return Collections.unmodifiableList(documents);
    }

    public Long getVersion() {
        return version;
    }

    /**
     * Associates a new Document without permitting ownership transfer. An already
     * associated Document may only be added again to this same Client.
     */
    public void addDocument(Document document) {
        Objects.requireNonNull(document, "document must not be null");
        document.assignClient(this);
        if (!documents.contains(document)) {
            documents.add(document);
        }
    }

    /**
     * Removes the Document from this in-memory collection only. It does not clear
     * the owning side or delete the row. Service-layer deletion must pair this call
     * with an explicit {@code DocumentRepository.delete(document)} in the same
     * transaction; collection-preserving updates must not call this method.
     */
    public boolean removeDocument(Document document) {
        return documents.remove(document);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Client that)) {
            return false;
        }
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        // Generated IDs change from null to a value after persistence; class hash is stable.
        return Client.class.hashCode();
    }

    @Override
    public String toString() {
        return "Client{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", taxIdentifier='" + taxIdentifier + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", version=" + version +
                '}';
    }
}
