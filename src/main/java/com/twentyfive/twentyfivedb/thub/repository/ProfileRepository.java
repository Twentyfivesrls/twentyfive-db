package com.twentyfive.twentyfivedb.thub.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import twentyfive.twentyfiveadapter.adapter.Document.ThubDocumentDB.ThubProfile;

@Repository
public interface ProfileRepository extends MongoRepository<ThubProfile, String> {
    ThubProfile findByUsername(String username);


}
