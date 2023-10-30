package com.twentyfive.twentyfivedb.twentyfively.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import twentyfive.twentyfiveadapter.adapter.Document.ShortenLinkDocumentDB.ShortenLinkDocumentDB;

import java.util.List;
import java.util.Optional;

public interface ShortenLinkRepository extends MongoRepository<twentyfive.twentyfiveadapter.adapter.Document.ShortenLinkDocumentDB.ShortenLinkDocumentDB, String> {
    List<ShortenLinkDocumentDB> findAllByUserId(String userId);

    Optional<ShortenLinkDocumentDB> findByShortUrl(String current);
}
