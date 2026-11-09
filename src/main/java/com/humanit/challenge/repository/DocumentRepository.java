package com.humanit.challenge.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.humanit.challenge.domain.Document;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    boolean existsByNumber(String number);

    Optional<Document> findByNumber(String number);

    Page<Document> findByClientId(Long clientId, Pageable pageable);

    Optional<Document> findByIdAndClientId(Long documentId, Long clientId);
}
