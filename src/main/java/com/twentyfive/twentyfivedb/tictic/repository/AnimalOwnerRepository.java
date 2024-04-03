package com.twentyfive.twentyfivedb.tictic.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import twentyfive.twentyfiveadapter.models.tictickModels.TTAnimalOwner;

@Repository
public interface AnimalOwnerRepository extends MongoRepository<TTAnimalOwner, String> {
    TTAnimalOwner findByEmail(String email);
}
