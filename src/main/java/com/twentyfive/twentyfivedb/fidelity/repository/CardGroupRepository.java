package com.twentyfive.twentyfivedb.fidelity.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import twentyfive.twentyfiveadapter.models.fidelityModels.CardGroup;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Repository
public interface CardGroupRepository extends MongoRepository<CardGroup, String> {
    Page<CardGroup> findAllByOwnerId(String ownerId, Pageable pageable);

    List<CardGroup> findAllListByOwnerId(String ownerId);

    Page<CardGroup> findAllByNameIgnoreCase(String name, Pageable pageable);

    Set<CardGroup> findAllByOwnerIdAndNameContainingIgnoreCase(String ownerId, String name);

    Set<CardGroup> findByOwnerIdAndDescriptionContainingIgnoreCase(String ownerId, String description);

    Page<CardGroup> findAllByIsActive(Boolean status, Pageable pageable);

    Page<CardGroup> findAllByExpirationDate(LocalDateTime expirationDate, Pageable pageable);
}
