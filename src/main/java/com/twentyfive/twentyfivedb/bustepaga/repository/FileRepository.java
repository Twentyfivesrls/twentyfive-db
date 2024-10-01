package com.twentyfive.twentyfivedb.bustepaga.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import twentyfive.twentyfiveadapter.models.bustepagaModels.BPFile;


@Repository
public interface FileRepository extends MongoRepository<BPFile, String> {

    Page<BPFile> getAllByEmployeeId(String employeeId, Pageable pageable);

    void deleteAllByEmployeeId(String employeeId);
}
