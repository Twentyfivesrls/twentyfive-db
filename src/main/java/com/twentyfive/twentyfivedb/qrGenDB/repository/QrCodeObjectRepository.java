package com.twentyfive.twentyfivedb.qrGenDB.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import twentyfive.twentyfiveadapter.models.qrGenModels.QrCodeObject;

import java.util.List;

@Repository
public interface QrCodeObjectRepository extends MongoRepository<QrCodeObject,String> {

    Page<QrCodeObject> findByUsername(String username, Pageable pageable);

    List<QrCodeObject> findAllByUsername(String username);

    long countByUsername(String username);

}
