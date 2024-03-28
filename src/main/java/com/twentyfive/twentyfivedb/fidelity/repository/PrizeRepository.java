package com.twentyfive.twentyfivedb.fidelity.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import twentyfive.twentyfiveadapter.models.fidelityModels.Premio;

@Repository
public interface PrizeRepository extends MongoRepository<Premio, String> {

    Page<Premio> findAllByCardIdIgnoreCase(String id, Pageable pageable);

    Premio findTopByCardIdOrderByCardCompleteDesc(String cardId);
}
