package com.twentyfive.twentyfivedb.qrGenDB.service;

import com.twentyfive.twentyfivedb.qrGenDB.repository.QrCodeObjectRepository;
import com.twentyfive.twentyfivedb.qrGenDB.utils.QrTypeUtils;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.models.qrGenModels.QrCodeObject;

import java.util.*;
import java.util.function.Function;

@Service
@Slf4j
public class QrCodeObjectService {
    private final QrCodeObjectRepository qrCodeObjectRepository;
    private final QrStatisticsService qrStatisticsService;

    private static final String NULL_QRCODE = "QrCodeObject is null";
    private static final String ID_NULL_QRCODE = "IdQrCode is null or empty";

    private static final Map<String, Function<QrCodeObject, String>> qrCodeTypeHandlers = new HashMap<>();

    static {
        qrCodeTypeHandlers.put("link", QrTypeUtils::handleLinkType);
        qrCodeTypeHandlers.put("phone", QrTypeUtils::handlePhoneType);
        qrCodeTypeHandlers.put("sms", QrTypeUtils::handleSmsType);
        qrCodeTypeHandlers.put("email", QrTypeUtils::handleEmailType);
        qrCodeTypeHandlers.put("whatsapp", QrTypeUtils::handleWhatsappType);
        qrCodeTypeHandlers.put("wifi", QrTypeUtils::handleWifiType);
        qrCodeTypeHandlers.put("text", QrTypeUtils::handleTextType);
    }


    public QrCodeObjectService(QrCodeObjectRepository qrCodeObjectRepository, QrStatisticsService qrStatisticsService) {
        this.qrCodeObjectRepository = qrCodeObjectRepository;
        this.qrStatisticsService = qrStatisticsService;
    }

    public QrCodeObject saveQrCodeObject(QrCodeObject qrCodeObject, String username) {
        if (qrCodeObject == null) {
            throw new IllegalArgumentException("QR code object cannot be null");
        }

        log.info("QrType: " + qrCodeObject.getType());

        String qrType = qrCodeObject.getType();
        if (qrType == null || qrType.trim().isEmpty()) {
            throw new IllegalArgumentException("QR code type cannot be null or empty");
        }
        qrType = qrType.trim().toLowerCase();

        Function<QrCodeObject, String> handler = qrCodeTypeHandlers.get(qrType);

        if (handler == null) {
            throw new IllegalArgumentException("Unsupported QR code type: " + qrType);
        }

        String destinationUrl = handler.apply(qrCodeObject);

        qrCodeObject.setLink(destinationUrl);
        qrCodeObject.setUsername(username);
        qrCodeObject.setIsActivated(true);

        return qrCodeObjectRepository.save(qrCodeObject);
    }

    public QrCodeObject getQrCodeObjectById(String idQrCode) {
        if (StringUtils.isBlank(idQrCode)) {
            log.error(ID_NULL_QRCODE);
            throw new IllegalArgumentException(ID_NULL_QRCODE);
        }
        return qrCodeObjectRepository.findById(idQrCode).orElse(null);
    }

    public List<QrCodeObject> getAllQrCodeObject(String username) {
        List<QrCodeObject> qrCodeObjectList = qrCodeObjectRepository.findAllByUsername(username);
        List<QrCodeObject> mapList = new ArrayList<>();
        for (QrCodeObject qrCodeObject : qrCodeObjectList) {
            mapList.add(qrCodeObject);
        }
        return mapList;
    }

    public List<QrCodeObject> getObjectsByUsername(String username, Integer pageNumber, Integer pageSize) {
        if (StringUtils.isBlank(username)) {
            log.error("IdUser is null or empty");
            throw new IllegalArgumentException("IdUser is null or empty");
        }
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<QrCodeObject> qrCodeObjectPage = qrCodeObjectRepository.findByUsername(username, pageable);
        List<QrCodeObject> mapList = new ArrayList<>();
        for (QrCodeObject QrCodeObject : qrCodeObjectPage.getContent()) {
            mapList.add(QrCodeObject);
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

    private void copyQrCodeProperties(QrCodeObject source, QrCodeObject target) {
        target.setName(source.getName());
        target.setLink(source.getLink());
        target.setDescription(source.getDescription());
        target.setSimpleText(source.getSimpleText());
        target.setPhoneNumber(source.getPhoneNumber());
        target.setEmailTo(source.getEmailTo());
        target.setEmailSubject(source.getEmailSubject());
        target.setEmailBody(source.getEmailBody());
        target.setSmsNumber(source.getSmsNumber());
        target.setSmsMessage(source.getSmsMessage());
        target.setWhatsappNumber(source.getWhatsappNumber());
        target.setWhatsappMessage(source.getWhatsappMessage());
        target.setWifiSSID(source.getWifiSSID());
        target.setWifiPassword(source.getWifiPassword());
        target.setWifiEncryption(source.getWifiEncryption());
        target.isWifiHidden();
        target.setIsActivated(source.getIsActivated());
        System.out.println(target);
    }

    public void updateQrCodeObject(String idQrCode, QrCodeObject qrCodeObject) {
        QrCodeObject existingQrCodeObject = qrCodeObjectRepository.findById(idQrCode)
                .orElseThrow(() -> new IllegalArgumentException(NULL_QRCODE));

        String qrType = Optional.ofNullable(qrCodeObject.getType())
                .map(String::trim)
                .map(String::toLowerCase)
                .orElseThrow(() -> new IllegalArgumentException("QR code type cannot be null or empty"));

        Function<QrCodeObject, String> handler = qrCodeTypeHandlers.get(qrType);
        if (handler == null) {
            throw new IllegalArgumentException("Unsupported QR code type: " + qrType);
        }

        String destinationUrl = handler.apply(qrCodeObject);
        qrCodeObject.setLink(destinationUrl);

        copyQrCodeProperties(qrCodeObject, existingQrCodeObject);
        qrCodeObjectRepository.save(existingQrCodeObject);
    }


    /*public void updateQrCodeObject(String idQrCode, QrCodeObject qrCodeObject) {
        QrCodeObject qrcodeObject = qrCodeObjectRepository.findById(idQrCode).orElse(null);
        if (qrcodeObject == null) {
            log.error(NULL_QRCODE);
            throw new IllegalArgumentException(NULL_QRCODE);
        }

        String qrType = qrCodeObject.getType();
        if (qrType == null || qrType.trim().isEmpty()) {
            throw new IllegalArgumentException("QR code type cannot be null or empty");
        }
        qrType = qrType.trim().toLowerCase();

        Function<QrCodeObject, String> handler = qrCodeTypeHandlers.get(qrType);

        if (handler == null) {
            throw new IllegalArgumentException("Unsupported QR code type: " + qrType);
        }

        String destinationUrl = handler.apply(qrCodeObject);

        qrcodeObject.setName(qrCodeObject.getName());
        qrcodeObject.setLink(destinationUrl);
        qrcodeObject.setDescription(qrCodeObject.getDescription());
        qrcodeObject.setSimpleText(qrCodeObject.getSimpleText());
        qrcodeObject.setPhoneNumber(qrCodeObject.getPhoneNumber());
        qrcodeObject.setEmailTo(qrCodeObject.getEmailTo());
        qrcodeObject.setEmailSubject(qrCodeObject.getEmailSubject());
        qrCodeObject.setEmailBody(qrCodeObject.getEmailBody());
        qrCodeObject.setSmsNumber(qrCodeObject.getSmsNumber());
        qrCodeObject.setSmsMessage(qrCodeObject.getSmsMessage());
        qrCodeObject.setWifiSSID(qrCodeObject.getWifiSSID());
        qrCodeObject.setWifiPassword(qrCodeObject.getWifiPassword());
        qrCodeObject.setWifiEncryption(qrCodeObject.getWifiEncryption());
        qrCodeObject.isWifiHidden();
        qrcodeObject.setIsActivated(qrCodeObject.getIsActivated());
        System.out.println(qrCodeObject);
        qrCodeObjectRepository.save(qrcodeObject);
    }*/
}
