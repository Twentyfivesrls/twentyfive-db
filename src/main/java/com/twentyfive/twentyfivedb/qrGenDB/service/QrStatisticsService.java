package com.twentyfive.twentyfivedb.qrGenDB.service;


import com.twentyfive.twentyfivedb.qrGenDB.repository.QrStatisticsRepository;
import com.twentyfive.twentyfivemodel.models.qrGenModels.QrStatistics;
import io.micrometer.common.util.StringUtils;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.adapter.Document.QrGenDocumentDB.QrStatisticsDocumentDB;
import twentyfive.twentyfiveadapter.adapter.Mapper.TwentyFiveMapper;

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

        qrStatisticsRepository.save(TwentyFiveMapper.INSTANCE.qrStatisticsToQrStatisticsDocumentDB(qrStatistics));
    }


    public List<QrStatistics> getAllQrStatistics() {



        List<QrStatisticsDocumentDB> qrStatisticsDocumentDBList = qrStatisticsRepository.findAll();
        List<QrStatistics> mapList = new ArrayList<>();
        for (QrStatisticsDocumentDB qrStatisticsDocumentDB : qrStatisticsDocumentDBList) {
            mapList.add(TwentyFiveMapper.INSTANCE.qrStatisticsDocumentDBToQrStatistics(qrStatisticsDocumentDB));
        }

        return mapList;
    }


    public List<QrStatistics> getQrStatisticsByIdQrCodeObject(String idQrCodeObject) {
        if (idQrCodeObject == null || idQrCodeObject.isEmpty())
            throw new IllegalArgumentException("IdQrCodeObject is null or empty");

        List<QrStatisticsDocumentDB> qrStatisticsDocumentDBList = qrStatisticsRepository.findByIdQrCodeObject(idQrCodeObject);
        List<QrStatistics> mapList = new ArrayList<>();
        for (QrStatisticsDocumentDB qrStatisticsDocumentDB : qrStatisticsDocumentDBList) {
            mapList.add(TwentyFiveMapper.INSTANCE.qrStatisticsDocumentDBToQrStatistics(qrStatisticsDocumentDB));
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
