package com.twentyfive.twentyfivedb.qrGenDB.controller;


import com.twentyfive.twentyfivedb.qrGenDB.repository.QrCodeObjectRepository;
import com.twentyfive.twentyfivedb.qrGenDB.service.QrCodeObjectService;
import com.twentyfive.twentyfivedb.qrGenDB.service.QrStatisticsService;
import com.twentyfive.twentyfivedb.qrGenDB.utils.MethodUtils;
import com.twentyfive.twentyfivedb.qrGenDB.utils.QrTypeUtils;
import com.twentyfive.twentyfivemodel.dto.qrGenDto.ResponseImage;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twentyfive.twentyfiveadapter.models.qrGenModels.QrCodeObject;

import java.util.Base64;
import java.util.List;

@RequestMapping("/qr_code")
@RestController
public class QrCodeObjectController {

    @Value("${qrgen.base.url}")
    private String baseUrl;
    public static final int DEFAULT_QR_WIDTH = 350;
    public static final int DEFAULT_QR_HEIGHT = 350;
    private final QrCodeObjectService qrCodeObjectService;
    private final QrCodeObjectRepository qrCodeObjectRepository;

    public QrCodeObjectController(QrCodeObjectService qrCodeObjectService, QrCodeObjectRepository qrCodeObjectRepository) {
        this.qrCodeObjectService = qrCodeObjectService;
        this.qrCodeObjectRepository = qrCodeObjectRepository;
    }

    @GetMapping("/allByUsername")
    public ResponseEntity<Page<QrCodeObject>> getAllQrCodeObjectByIdUser(@RequestParam(defaultValue = "0") int page,
                                                                         @RequestParam(defaultValue = "10") int size,
                                                                         @RequestParam(defaultValue = "username") String username) {

        List<QrCodeObject> qrCodeObjectDocumentDBList = qrCodeObjectService.getObjectsByUsername(username, page, size);
        Page<QrCodeObject> qrCodeObjectPage = new PageImpl<>(qrCodeObjectDocumentDBList);

        return ResponseEntity.status(HttpStatus.OK).body(qrCodeObjectPage);
    }

    @GetMapping("/qrCodeObjectById/{idQrCode}")
    public ResponseEntity<QrCodeObject> getQrCodeObjectById(@PathVariable String idQrCode) {
        return ResponseEntity.status(HttpStatus.OK).body(qrCodeObjectService.getQrCodeObjectById(idQrCode));
    }

    @PostMapping("/save")
    public ResponseEntity<QrCodeObject> saveQrCodeObject(@RequestBody QrCodeObject qrCodeObject, @RequestParam(value = "username") String username) {
        if (qrCodeObject == null) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        qrCodeObjectService.saveQrCodeObject(qrCodeObject, username);
        return ResponseEntity.status(HttpStatus.OK).body(qrCodeObject);
    }


    @GetMapping(value = "/generateQRCode/{codeText}/{description}")
    public ResponseEntity<byte[]> generateQRCode(@PathVariable String codeText, @PathVariable String description, @PathVariable String username) throws Exception {

        String directory;
        List<QrCodeObject> qrCodeObjectList = qrCodeObjectService.getAllQrCodeObject(username);
        int sizeOfList = qrCodeObjectList.size();

        if (StringUtils.isBlank(codeText)) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
        } else {
            directory = sizeOfList + ".png";
        }

        MethodUtils.generateQRCodeImage(codeText, 350, 350, directory);

        return ResponseEntity.status(HttpStatus.OK).body(MethodUtils.getQRCodeImage(codeText, 350, 350));
    }

    @DeleteMapping("/delete/{idQrCode}")
    public ResponseEntity<QrCodeObject> deleteQrCodeObject(@PathVariable String idQrCode) {
        qrCodeObjectService.deleteQrCodeObjectAndStats(idQrCode);
        return ResponseEntity.ok().build();
    }


    @PostMapping(value = "/generateAndDownloadQRCode")
    public ResponseEntity<QrCodeObject> download(@RequestBody QrCodeObject qrCodeObject, @RequestParam(value = "username") String username) {
        try {
            QrCodeObject savedQrCode = qrCodeObjectService.saveQrCodeObject(qrCodeObject, username);

            if (savedQrCode == null) {
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return new ResponseEntity<>(savedQrCode, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PutMapping("/update/{idQrCode}")
    ResponseEntity<QrCodeObject> updateQrCodeObject(@PathVariable String idQrCode, @RequestBody QrCodeObject qrCodeObject) {
        if (idQrCode == null || idQrCode.isEmpty())
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);

        //TODO: controll qrcodemodel

        qrCodeObjectService.updateQrCodeObject(idQrCode, qrCodeObject);
        return ResponseEntity.status(HttpStatus.OK).body(qrCodeObject);
    }

    @GetMapping("/download/{idQrCode}")
    public ResponseEntity<ResponseImage> downloadQrCodeBase64(@PathVariable String idQrCode) {
        try {
            QrCodeObject qrCodeObject = qrCodeObjectRepository.findById(idQrCode).orElse(null);

            if (qrCodeObject == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            String togenerate;

            if ("wifi".equalsIgnoreCase(qrCodeObject.getType())) {
                togenerate = QrTypeUtils.handleWifiType(qrCodeObject);
            } else {
                togenerate = baseUrl + "crudStats/" + idQrCode;
            }

            byte[] bytes = MethodUtils.generateQrCodeImage(togenerate, DEFAULT_QR_WIDTH, DEFAULT_QR_HEIGHT);
            String base64 = Base64.getEncoder().encodeToString(bytes);
            base64 = "data:image/png;base64," + base64;

            ResponseImage response = new ResponseImage();
            response.setImageBase64(base64);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
        }
    }

    /*@GetMapping("/download/{idQrCode}")
    public ResponseEntity<ResponseImage> downloadQrCodeBase64(@PathVariable String idQrCode) {
        try {
            String togenerate = baseUrl + "crudStats/" + idQrCode;
            byte[] bytes = MethodUtils.generateQrCodeImage(togenerate, DEFAULT_QR_WIDTH, DEFAULT_QR_HEIGHT);
            String base64 = Base64.getEncoder().encodeToString(bytes);
            base64 = "data:image/png;base64," + base64;
            ResponseImage response = new ResponseImage();
            response.setImageBase64(base64);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
        }
    }*/

    @GetMapping("/all")
    public ResponseEntity<List<QrCodeObject>> getAllQrCodeObject(@RequestParam("username") String username) {
        return ResponseEntity.status(HttpStatus.OK).body(qrCodeObjectService.getAllQrCodeObject(username));
    }

}
