package com.twentyfive.twentyfivedb.tictic.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import twentyfive.twentyfiveadapter.models.tictickModels.TTAnimal;
import java.util.List;

@Repository
public interface AnimalRepository extends MongoRepository<TTAnimal, String>{

    TTAnimal findByMicrochipCode(String microchipCode);

    List<TTAnimal> findAllByOwnerId(String ownerId);
}
