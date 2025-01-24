package com.twentyfive.twentyfivedb.qrGenDB.controller;


import com.google.zxing.WriterException;
import com.twentyfive.twentyfivedb.qrGenDB.repository.QrCodeObjectRepository;
import com.twentyfive.twentyfivedb.qrGenDB.service.QrCodeObjectService;
import com.twentyfive.twentyfivedb.qrGenDB.service.QrCodePdfService;
import com.twentyfive.twentyfivedb.qrGenDB.service.QrCodeSvgService;
import com.twentyfive.twentyfivedb.qrGenDB.utils.MethodUtils;
import com.twentyfive.twentyfivedb.qrGenDB.utils.QrTypeUtils;
import com.twentyfive.twentyfivemodel.dto.qrGenDto.ResponseImage;
import com.twentyfive.twentyfivemodel.filterTicket.AutoCompleteRes;
import io.micrometer.common.util.StringUtils;
import jakarta.ws.rs.Produces;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twentyfive.twentyfiveadapter.models.qrGenModels.QrCodeGroup;
import twentyfive.twentyfiveadapter.models.qrGenModels.QrCodeObject;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@RequestMapping("/qr_code")
@RestController
public class QrCodeObjectController {

    @Value("${qrgen.base.url}")
    private String baseUrl;
    public static final int DEFAULT_QR_WIDTH = 350;
    public static final int DEFAULT_QR_HEIGHT = 350;
    private final QrCodeObjectService qrCodeObjectService;
    private final QrCodeObjectRepository qrCodeObjectRepository;
    private final QrCodePdfService qrCodePdfService;

    private final QrCodeSvgService qrCodeSvgService;


    public QrCodeObjectController(QrCodeSvgService qrCodeSvgService, QrCodeObjectService qrCodeObjectService, QrCodeObjectRepository qrCodeObjectRepository, QrCodePdfService qrCodePdfService) {
        this.qrCodeObjectService = qrCodeObjectService;
        this.qrCodeObjectRepository = qrCodeObjectRepository;
        this.qrCodePdfService = qrCodePdfService;
        this.qrCodeSvgService = qrCodeSvgService;


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
    public ResponseEntity<QrCodeObject> saveQrCodeObject(@RequestBody QrCodeObject qrCodeObject, @RequestParam(value = "username") String username, @RequestParam("isFullyEnabled") boolean isFullyEnabled) {
        if (qrCodeObject == null) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        qrCodeObjectService.saveQrCodeObject(qrCodeObject, username, isFullyEnabled);
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

    @GetMapping(value = "/downloadQrGroup", produces = MediaType.APPLICATION_PDF_VALUE)
    @ResponseBody
    public ResponseEntity<byte[]> downloadQrGroup(
            @RequestParam("username") String username,
            @RequestParam("groupNumber") String groupNumber) throws Exception {

        byte[] pdfBytes = qrCodePdfService.generateQrCodePdf(username, groupNumber);
        String fileName = String.format("QrCodeGroup_%s_%s.svg", username, groupNumber.replace(" ", "_"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", fileName);
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    @GetMapping("/downloadQrGroupSvg")
    public ResponseEntity<byte[]> downloadQrGroupSvg(@RequestParam("username") String username,
                                                     @RequestParam("groupNumber") String groupNumber) {
        try {
            byte[] svgBytes = qrCodeSvgService.generateQrCodeSvg(username, groupNumber);
            String fileName = "QrCodeGroup_" + groupNumber.replace(" ", "_") + ".svg";

            // Set headers for file download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentLength(svgBytes.length);

            // Return the SVG as a downloadable file
            return ResponseEntity.ok().headers(headers).body(svgBytes);

        } catch (IOException | WriterException e) {
            // Handle errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/delete/{idQrCode}")
    public ResponseEntity<QrCodeObject> deleteQrCodeObject(@PathVariable String idQrCode) {
        qrCodeObjectService.deleteQrCodeObjectAndStats(idQrCode);
        return ResponseEntity.ok().build();
    }


    @PostMapping(value = "/generateAndDownloadQRCode")
    public ResponseEntity<QrCodeObject> download(@RequestBody QrCodeObject qrCodeObject, @RequestParam(value = "username") String username, @RequestParam("isFullyEnabled") boolean isFullyEnabled) {
        try {
            QrCodeObject savedQrCode = qrCodeObjectService.saveQrCodeObject(qrCodeObject, username, isFullyEnabled);

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
    ResponseEntity<QrCodeObject> updateQrCodeObject(@PathVariable String idQrCode, @RequestParam("isFullyEnabled") boolean isFullyEnabled, @RequestBody QrCodeObject qrCodeObject) {
        if (idQrCode == null || idQrCode.isEmpty())
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);

        if (!isFullyEnabled) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
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

    @PostMapping("/generateQrGroup")
    public ResponseEntity<List<QrCodeGroup>> generateQrCodeGroup(@RequestParam String username, @RequestParam("ownerId") String ownerId, @RequestParam("quantityGroup") Integer quantityGroup) {
        try {
            List<QrCodeGroup> qrCodeGroup = qrCodeObjectService.generateQrCodeGroup(username, ownerId, quantityGroup);
            return ResponseEntity.status(HttpStatus.OK).body(qrCodeGroup);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/filter/qrcode/autocomplete")
    public ResponseEntity<Set<AutoCompleteRes>> filterAutocompleteQrCode(@RequestParam("ownerId") String ownerId, @RequestParam("filterObject") String filterObject) {
        return new ResponseEntity<>(qrCodeObjectService.filterAutocompleteQrCode(filterObject, ownerId), HttpStatus.OK);
    }


}
