package com.twentyfive.twentyfivedb.qrGenDB.service;

import com.twentyfive.twentyfivedb.qrGenDB.repository.QrStatisticsRepository;
import io.micrometer.common.util.StringUtils;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.models.qrGenModels.QrStatistics;

import java.util.ArrayList;
import java.util.List;


@Service
public class QrStatisticsService {

    private final QrStatisticsRepository qrStatisticsRepository;

    public QrStatisticsService(QrStatisticsRepository qrStatisticsRepository) {
        this.qrStatisticsRepository = qrStatisticsRepository;
    }



    public void saveQrStatistics(QrStatistics qrStatistics) {

        if (qrStatistics == null)
            throw new IllegalArgumentException("QrStatistics is null");

        qrStatisticsRepository.save(qrStatistics);
    }


    public List<QrStatistics> getAllQrStatistics() {
        List<QrStatistics> qrStatisticsList = qrStatisticsRepository.findAll();
        List<QrStatistics> mapList = new ArrayList<>();
        for (QrStatistics qrStatistics : qrStatisticsList) {
            mapList.add(qrStatistics);
        }

        return mapList;
    }


    public List<QrStatistics> getQrStatisticsByIdQrCodeObject(String idQrCodeObject) {
        if (idQrCodeObject == null || idQrCodeObject.isEmpty())
            throw new IllegalArgumentException("IdQrCodeObject is null or empty");

        List<QrStatistics> qrStatisticsList = qrStatisticsRepository.findByIdQrCodeObject(idQrCodeObject);
        List<QrStatistics> mapList = new ArrayList<>();
        for (QrStatistics qrStatistics : qrStatisticsList) {
            mapList.add(qrStatistics);
        }

        return mapList;
    }

    public void deleteAllStatsOfQrCodeObject(String idQrCode) {
        if(StringUtils.isBlank(idQrCode)) {
            throw new IllegalArgumentException("IdQrCode is null or empty");
        }
        qrStatisticsRepository.deleteAllByIdQrCodeObject(idQrCode);
    }
}
