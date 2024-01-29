package com.twentyfive.twentyfivedb.twentyfiveapps.repository;

import com.twentyfive.twentyfivemodel.models.twentyfiveappsModels.TwentyfiveApp;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TwentyfiveAppsRepository extends MongoRepository<TwentyfiveApp, String> {
    Page<TwentyfiveApp> getAllApps(PageRequest pageable);
    List<TwentyfiveApp> getAllApps();
}
