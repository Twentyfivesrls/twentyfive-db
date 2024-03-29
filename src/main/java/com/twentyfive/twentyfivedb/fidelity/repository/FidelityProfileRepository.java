package com.twentyfive.twentyfivedb.fidelity.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import twentyfive.twentyfiveadapter.models.fidelityModels.ProfileFidelity;

@Repository
public interface FidelityProfileRepository extends MongoRepository<ProfileFidelity, String> {
    ProfileFidelity findAllByImageName(String imageName);

    ProfileFidelity findByOwnerId(String ownerId);
}
