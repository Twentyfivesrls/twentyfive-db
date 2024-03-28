package com.twentyfive.twentyfivedb.thub.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import twentyfive.twentyfiveadapter.models.thubModels.ThubProfile;


import java.util.Optional;

@Repository
public interface ThubProfileRepository extends MongoRepository<ThubProfile, String> {
    Optional<ThubProfile> findByUsername(String username);

}
