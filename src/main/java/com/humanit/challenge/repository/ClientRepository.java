package com.humanit.challenge.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.humanit.challenge.domain.Client;

import jakarta.persistence.LockModeType;

public interface ClientRepository extends JpaRepository<Client, Long> {

    boolean existsByTaxIdentifier(String taxIdentifier);

    Optional<Client> findByTaxIdentifier(String taxIdentifier);

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("select c from Client c where c.id = :id")
    Optional<Client> lockById(@Param("id") Long id);
}
