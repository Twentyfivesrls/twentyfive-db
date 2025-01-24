package com.twentyfive.twentyfivedb.qrGenDB.service;

import com.twentyfive.twentyfivedb.qrGenDB.repository.QrCodeObjectRepository;
import com.twentyfive.twentyfivedb.qrGenDB.utils.QrTypeUtils;
import com.twentyfive.twentyfivedb.qrGenDB.repository.QrCodeGroupRepository;
import com.twentyfive.twentyfivedb.tictic.service.ShopperService;
import com.twentyfive.twentyfivemodel.filterTicket.AutoCompleteRes;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.models.qrGenModels.QrCodeGroup;
import twentyfive.twentyfiveadapter.models.qrGenModels.QrCodeObject;
import twentyfive.twentyfiveadapter.models.tictickModels.TicTicCustomer;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class QrCodeObjectService {
    private final QrCodeObjectRepository qrCodeObjectRepository;
    private final QrStatisticsService qrStatisticsService;
    private final QrCodeGroupRepository qrCodeGroupRepository;
    private final ShopperService shopperService;
    private static final String NULL_QRCODE = "QrCodeObject is null";
    private static final String ID_NULL_QRCODE = "IdQrCode is null or empty";

    private static String TICTIC_URL = "https://tictic25.it/";

    @Value("${thresholdQrCodeCreation}")
    private long thresholdQrCodeCreation;

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


    public QrCodeObjectService(QrCodeObjectRepository qrCodeObjectRepository, QrStatisticsService qrStatisticsService, QrCodeGroupRepository qrCodeGroupRepository, ShopperService shopperService) {
        this.qrCodeObjectRepository = qrCodeObjectRepository;
        this.qrStatisticsService = qrStatisticsService;
        this.qrCodeGroupRepository = qrCodeGroupRepository;
        //TODO check if this is the right way to call ShopperService in QrCodeObjectService
        this.shopperService = shopperService;
    }

    public QrCodeObject saveQrCodeObject(QrCodeObject qrCodeObject, String username, boolean isFullyEnabled) {
        if (qrCodeObject == null) {
            throw new IllegalArgumentException("QR code object cannot be null");
        }

        if (!isFullyEnabled) {
            if(this.countByUsername(username) >= thresholdQrCodeCreation){
                throw new IllegalArgumentException("You have reached the maximum number of QR codes that can be created");
            }
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

    private long countByUsername(String username) {
        return qrCodeObjectRepository.countByUsername(username);
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
        return new ArrayList<>(qrCodeObjectList);
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

    public List<QrCodeGroup> generateQrCodeGroup(String username, String ownerId, Integer quantityGroup) {
        List<QrCodeGroup> allGroups = qrCodeGroupRepository.findAllGroupNamesByOwnerId(ownerId);

        int maxGroupNumber = allGroups.stream()
                .map(QrCodeGroup::getGroupName)
                .filter(name -> StringUtils.isNotBlank(name) && name.startsWith("Gruppo "))
                .mapToInt(name -> Integer.parseInt(name.substring(7).trim()))
                .max()
                .orElse(0);

        int startingIdIndex = Math.toIntExact(qrCodeGroupRepository.countAllDocuments());

        List<QrCodeGroup> qrCodeGroups = generateAndSaveQrCodeGroups(username, ownerId, quantityGroup, maxGroupNumber, startingIdIndex);

        // Salva tutti i QR code in un'unica chiamata
        qrCodeGroupRepository.saveAll(qrCodeGroups);

        return qrCodeGroups;
    }

    private List<QrCodeGroup> generateAndSaveQrCodeGroups(String username, String ownerId, int quantityGroup, int maxGroupNumber, int startingIdIndex) {
        String newGroupName = "Gruppo " + (maxGroupNumber + 1);
        List<QrCodeGroup> qrCodeGroups = new ArrayList<>();

        for (int i = 0; i < quantityGroup; i++) {
            QrCodeGroup qrCodeGroup = new QrCodeGroup();
            String idQrCode = String.valueOf(startingIdIndex + i + 1);
            qrCodeGroup.setIdQrCode(idQrCode);
            qrCodeGroup.setUsername(username);
            qrCodeGroup.setNameQrCode("QRCode " + idQrCode);
            qrCodeGroup.setType("link");
            qrCodeGroup.setGroupName(newGroupName);
            qrCodeGroup.setOwnerId(ownerId);
            qrCodeGroup.setIsActivated(true);
            qrCodeGroup.setLink(TICTIC_URL + "qr/" + qrCodeGroup.getIdQrCode());
            qrCodeGroups.add(qrCodeGroup);
        }
        //TODO check if this is the right way to call ShopperService in QrCodeObjectService
        this.shopperService.updateShopperCounter(username, "orderedPlates", String.valueOf(qrCodeGroups.size()));
        this.shopperService.updateShopperCounter(username, "remainingPlates", String.valueOf(qrCodeGroups.size()));
        return qrCodeGroups;
    }

    public Set<AutoCompleteRes> filterAutocompleteQrCode(String find, String ownerId) {
        Set<QrCodeGroup> qrCodes = qrCodeGroupRepository.findByOwnerIdAndAnyMatchingFields(ownerId, find);
        return qrCodes.stream().map(c -> {
            AutoCompleteRes res = new AutoCompleteRes();
            res.setValue(c.getNameQrCode());
            return res;
        }).collect(Collectors.toCollection(LinkedHashSet::new));
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
