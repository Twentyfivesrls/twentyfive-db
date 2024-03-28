package com.twentyfive.twentyfivedb.bustepaga.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import twentyfive.twentyfiveadapter.models.bustepagaModels.BPConfiguration;


import java.util.List;

@Repository
public interface ConfigurationsRepository extends MongoRepository<BPConfiguration, String> {

    List<BPConfiguration> getAllByType(String type);
}
