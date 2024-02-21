package com.twentyfive.twentyfivedb.fidelity.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import twentyfive.twentyfiveadapter.adapter.Document.FidelityDocumentDB.Card;
import twentyfive.twentyfiveadapter.adapter.Document.FidelityDocumentDB.CardGroup;

@Repository
public interface CardRepository extends MongoRepository<Card, String> {
    Page<Card> getAllById(String id, Pageable pageable);

    Page<Card> findAllByNameIgnoreCase(String name, Pageable pageable);

    Page<Card> findAllByIsActive(Boolean status, Pageable pageable);

}
