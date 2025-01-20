package com.twentyfive.twentyfivedb.twentyfively.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import twentyfive.twentyfiveadapter.models.twlyModels.ShortenLink;


import java.util.List;
import java.util.Optional;

public interface ShortenLinkRepository extends MongoRepository<ShortenLink, String> {
    List<ShortenLink> findAllByUserId(String userId);

    List<ShortenLink> findAllByUserIdAndDeletedOrderByCreatedAtDesc(String userId, boolean b);

    Optional<ShortenLink> findByShortUrl(String current);

    List<ShortenLink> findAllByUserIdAndDeleted(String userId, boolean b);
}
