package com.twentyfive.twentyfivedb.fidelity.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import twentyfive.twentyfiveadapter.models.fidelityModels.Card;

import java.util.List;
import java.util.Set;

@Repository
public interface CardRepository extends MongoRepository<Card, String> {
    Page<Card> getAllById(String id, Pageable pageable);

    Page<Card> findAllByNameIgnoreCase(String name, Pageable pageable);

    Set<Card> findAllByNameContainingIgnoreCase(String name);

    List<Card> findAllByCardGroupId(String groupId);

    List<Card> findAllByCardGroupIdAndOwnerId(String groupId, String ownerId);

    List<Card> findAllByOwnerId(String ownerId);

    Page<Card> findAllByIsActive(Boolean status, Pageable pageable);
}
