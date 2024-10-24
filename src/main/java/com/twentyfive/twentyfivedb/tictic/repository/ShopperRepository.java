package com.twentyfive.twentyfivedb.tictic.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import twentyfive.twentyfiveadapter.models.tictickModels.TicTicShopper;

import java.util.Optional;

@Repository
public interface ShopperRepository extends MongoRepository<TicTicShopper, String> {
    Optional<TicTicShopper> findByOwnerId(String ownerId);
}
