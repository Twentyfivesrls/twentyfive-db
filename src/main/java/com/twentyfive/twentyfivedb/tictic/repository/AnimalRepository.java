package com.twentyfive.twentyfivedb.tictic.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import twentyfive.twentyfiveadapter.models.tictickModels.TTAnimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Sort;

@Repository
public interface AnimalRepository extends MongoRepository<TTAnimal, String>{

    Set<TTAnimal> findByNameContainingIgnoreCase(String name);

    Set<TTAnimal> findByNameContainingIgnoreCaseAndOwnerIdContainingIgnoreCase(String name, String ownerId);

    List<TTAnimal> findByOwnerIdContainingIgnoreCase(String ownerId, Sort sort);

    @Query("{ 'email': ?0 }")
    Optional<TTAnimal> findByEmail(String email);
}
