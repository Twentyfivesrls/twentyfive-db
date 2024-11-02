package com.twentyfive.twentyfivedb.qrGenDB.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import twentyfive.twentyfiveadapter.models.qrGenModels.QrCodeGroup;

import java.util.List;

@Repository
public interface QrCodeGroupRepository extends MongoRepository<QrCodeGroup, String> {
    @Query(value = "{}", fields = "{ 'groupName' : 1 }")
    List<QrCodeGroup> findAllGroupNames();

    boolean existsByOwnerId(String ownerId);

    Page<QrCodeGroup> findByOwnerId(String ownerId, Pageable pageable);
}
