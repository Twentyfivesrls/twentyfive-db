package com.twentyfive.twentyfivedb.qrGenDB.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import twentyfive.twentyfiveadapter.models.qrGenModels.QrStatistics;

import java.util.List;

@Repository
public interface QrStatisticsRepository extends MongoRepository<QrStatistics, String> {

    List<QrStatistics> findByIdQrCodeObject(String idQrCodeObject);


    void deleteAllByIdQrCodeObject(String idQrCode);
}
