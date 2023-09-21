package com.twentyfive.twentyfivedb.linktreeDB.repositories;

import com.twentyfive.twentyfivemodel.models.linktreeModels.LinkTree;
import org.springframework.data.mongodb.repository.MongoRepository;
import twentyfive.twentyfiveadapter.adapter.Document.UserLinkDocumentDB;

import java.util.ArrayList;
import java.util.Optional;

public interface UserLinkRepository extends MongoRepository<UserLinkDocumentDB, String> {
    ArrayList<LinkTree> findLinkTreesByUserId(String userId);
    boolean existsByUserId(String userId);
    Optional<UserLinkDocumentDB> findById(String id);
    Optional<UserLinkDocumentDB> findByUserId(String userId);
}
