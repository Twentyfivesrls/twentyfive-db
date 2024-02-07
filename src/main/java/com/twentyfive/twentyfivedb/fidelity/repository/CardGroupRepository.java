package com.twentyfive.twentyfivedb.fidelity.repository;

import com.twentyfive.twentyfivemodel.models.fidelityModels.CardGroup;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardGroupRepository extends MongoRepository<CardGroup, String> {
}
