package com.twentyfive.twentyfivedb.qrGenDB.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import twentyfive.twentyfiveadapter.adapter.Document.QrGenDocumentDB.QrCodeObjectDocumentDB;

import java.util.List;

@Repository
public interface QrCodeObjectRepository extends MongoRepository<QrCodeObjectDocumentDB,String> {

    Page<QrCodeObjectDocumentDB> findByUsername(String username, Pageable pageable);

    List<QrCodeObjectDocumentDB> findAllByUsername(String username);
}
