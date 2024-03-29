package com.twentyfive.twentyfivedb.qrGenDB.service;


import com.twentyfive.twentyfivedb.qrGenDB.repository.QrCodeObjectRepository;
import com.twentyfive.twentyfivemodel.models.qrGenModels.QrCodeObject;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.adapter.Document.QrGenDocumentDB.QrCodeObjectDocumentDB;
import twentyfive.twentyfiveadapter.adapter.Mapper.TwentyFiveMapper;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
public class QrCodeObjectService {
    private final QrCodeObjectRepository qrCodeObjectRepository;
    private final QrStatisticsService qrStatisticsService;

    private static final String NULL_QRCODE = "QrCodeObject is null";
    private static final String ID_NULL_QRCODE = "IdQrCode is null or empty";


    public QrCodeObjectService(QrCodeObjectRepository qrCodeObjectRepository, QrStatisticsService qrStatisticsService) {
        this.qrCodeObjectRepository = qrCodeObjectRepository;
        this.qrStatisticsService = qrStatisticsService;
    }

    public QrCodeObjectDocumentDB saveQrCodeObject(QrCodeObject qrCodeObject, String username) {
        if (qrCodeObject == null) {
            throw new IllegalArgumentException(NULL_QRCODE);
        }

        String destinationUrl = qrCodeObject.getLink();
        try {
            URI uri = new URI(destinationUrl);
            if (uri.getScheme() == null) {
                destinationUrl = "http://" + destinationUrl;
            }
            qrCodeObject.setLink(destinationUrl);
            qrCodeObject.setUsername(username);
        } catch (Exception e) {
            log.error("Error in parsing link");
        }
        return qrCodeObjectRepository.save(TwentyFiveMapper.INSTANCE.qrCodeObjectToQrCodeObjectDocumentDB(qrCodeObject));
    }

    public QrCodeObject getQrCodeObjectById(String idQrCode) {
        if (StringUtils.isBlank(idQrCode)) {
            log.error(ID_NULL_QRCODE);
            throw new IllegalArgumentException(ID_NULL_QRCODE);
        }
        QrCodeObjectDocumentDB qrDocument = qrCodeObjectRepository.findById(idQrCode).orElse(null);
        return TwentyFiveMapper.INSTANCE.qrCodeObjectDocumentDBToQrCodeObject(qrDocument);
    }

    public List<QrCodeObject> getAllQrCodeObject(String username) {
        List<QrCodeObjectDocumentDB> qrCodeObjectDocumentDBList = qrCodeObjectRepository.findAllByUsername(username);
        List<QrCodeObject> mapList = new ArrayList<>();
        for (QrCodeObjectDocumentDB qrCodeObjectDocumentDB : qrCodeObjectDocumentDBList) {
            mapList.add(TwentyFiveMapper.INSTANCE.qrCodeObjectDocumentDBToQrCodeObject(qrCodeObjectDocumentDB));
        }
        return mapList;
    }

    public List<QrCodeObject> getObjectsByUsername(String username, Integer pageNumber, Integer pageSize) {
        if (StringUtils.isBlank(username)) {
            log.error("IdUser is null or empty");
            throw new IllegalArgumentException("IdUser is null or empty");
        }
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<QrCodeObjectDocumentDB> qrCodeObjectDocumentDBPage = qrCodeObjectRepository.findByUsername(username, pageable);
        List<QrCodeObject> mapList = new ArrayList<>();
        for (QrCodeObjectDocumentDB qrCodeObjectDocumentDB : qrCodeObjectDocumentDBPage.getContent()) {
            mapList.add(TwentyFiveMapper.INSTANCE.qrCodeObjectDocumentDBToQrCodeObject(qrCodeObjectDocumentDB));
        }
        return mapList;
    }

    public void deleteQrCodeObjectAndStats(String idQrCode) {
        if (StringUtils.isBlank(idQrCode)) {
            log.error(ID_NULL_QRCODE);
            throw new IllegalArgumentException(ID_NULL_QRCODE);
        } else {
            qrCodeObjectRepository.deleteById(idQrCode);
            qrStatisticsService.deleteAllStatsOfQrCodeObject(idQrCode);

        }
    }

    public void updateQrCodeObject(String idQrCode, QrCodeObject qrCodeObject) {
        if (StringUtils.isBlank(idQrCode)) {
            log.error(ID_NULL_QRCODE);
            throw new IllegalArgumentException(ID_NULL_QRCODE);
        }

        QrCodeObjectDocumentDB qrcodeObject = qrCodeObjectRepository.findById(idQrCode).orElse(null);
        if (qrcodeObject == null) {
            log.error(NULL_QRCODE);
            throw new IllegalArgumentException(NULL_QRCODE);
        }
        qrcodeObject.setName(qrCodeObject.getName());
        qrcodeObject.setLink(qrCodeObject.getLink());
        qrcodeObject.setDescription(qrCodeObject.getDescription());
        qrcodeObject.setIsActivated(qrCodeObject.getIsActivated());
        qrCodeObjectRepository.save(qrcodeObject);

    }
}
