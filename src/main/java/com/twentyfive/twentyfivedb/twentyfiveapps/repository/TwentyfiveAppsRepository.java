package com.twentyfive.twentyfivedb.twentyfiveapps.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import twentyfive.twentyfiveadapter.adapter.Document.TwentyfiveAppsDocumentDB.TwentyfiveApp;

@Repository
public interface TwentyfiveAppsRepository extends MongoRepository<TwentyfiveApp, String> {
}
