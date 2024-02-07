package com.twentyfive.twentyfivedb.bustepaga.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import twentyfive.twentyfiveadapter.adapter.Document.BustePagaDocumentDB.BPSetting;

import java.util.Optional;

@Repository
public interface SettingsRepository extends MongoRepository<BPSetting, String> {

    Optional<BPSetting> getByUserId(String userId);

}
