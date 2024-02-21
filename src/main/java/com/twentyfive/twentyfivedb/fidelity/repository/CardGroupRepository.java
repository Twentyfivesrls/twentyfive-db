package com.twentyfive.twentyfivedb.fidelity.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import twentyfive.twentyfiveadapter.adapter.Document.FidelityDocumentDB.CardGroup;

import java.time.LocalDateTime;

@Repository
public interface CardGroupRepository extends MongoRepository<CardGroup, String> {
    Page<CardGroup> getAllByOwnerId(String ownerId, Pageable pageable);

    Page<CardGroup> findAllByNameIgnoreCase(String name, Pageable pageable);

    Page<CardGroup> findAllByIsActive(Boolean status, Pageable pageable);

    Page<CardGroup> findAllByExpirationDate(LocalDateTime expirationDate, Pageable pageable);
}
