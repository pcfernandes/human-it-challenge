package com.humanit.challenge.domain;

import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

@Entity
@Table(name = "documents", uniqueConstraints = @UniqueConstraint(
        name = "uk_documents_number", columnNames = "number"))
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "number", nullable = false, length = 100)
    private String number;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_documents_client"))
    private Client client;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected Document() {
        // Required by JPA.
    }

    public Document(String number, String description, LocalDate expirationDate) {
        this.number = number;
        this.description = description;
        this.expirationDate = expirationDate;
    }

    public Long getId() {
        return id;
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

    public Client getClient() {
        return client;
    }

    public Long getVersion() {
        return version;
    }

    void assignClient(Client client) {
        Objects.requireNonNull(client, "client must not be null");
        if (this.client != null) {
            if (this.client == client) {
                return;
            }
            Long existingClientId = this.client.getId();
            Long requestedClientId = client.getId();
            if (existingClientId == null || !existingClientId.equals(requestedClientId)) {
                throw new IllegalArgumentException("Document ownership cannot be transferred");
            }
            return;
        }
        this.client = client;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Document that)) {
            return false;
        }
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        // Generated IDs change from null to a value after persistence; class hash is stable.
        return Document.class.hashCode();
    }

    @Override
    public String toString() {
        return "Document{" +
                "id=" + id +
                ", number='" + number + '\'' +
                ", description='" + description + '\'' +
                ", expirationDate=" + expirationDate +
                ", version=" + version +
                '}';
    }
}
