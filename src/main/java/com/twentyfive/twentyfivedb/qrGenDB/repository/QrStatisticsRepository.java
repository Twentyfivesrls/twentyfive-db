package com.twentyfive.twentyfivedb.qrGenDB.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import twentyfive.twentyfiveadapter.adapter.Document.QrGenDocumentDB.QrStatisticsDocumentDB;

import java.util.List;

@Repository
public interface QrStatisticsRepository extends MongoRepository<QrStatisticsDocumentDB, String> {

    List<QrStatisticsDocumentDB> findByIdQrCodeObject(String idQrCodeObject);


    void deleteAllByIdQrCodeObject(String idQrCode);
}
