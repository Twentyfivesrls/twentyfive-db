package com.twentyfive.twentyfivedb.qrGenDB.controller;


import com.twentyfive.twentyfivedb.qrGenDB.service.QrStatisticsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twentyfive.twentyfiveadapter.models.qrGenModels.QrStatistics;

import java.util.List;

@RequestMapping("/qr_statistics")
@RestController
public class QrStatisticsController {

    private final QrStatisticsService qrStatisticsService;

    public QrStatisticsController(QrStatisticsService qrStatisticsService) {
        this.qrStatisticsService = qrStatisticsService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<QrStatistics>> getAllQrStatistics() {

        return ResponseEntity.status(HttpStatus.OK).body(qrStatisticsService.getAllQrStatistics());
    }


    @PostMapping("/save")
    public ResponseEntity<QrStatistics> saveQrStatistics(@RequestBody QrStatistics qrStatistics) {
        if (qrStatistics == null)
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

        qrStatisticsService.saveQrStatistics(qrStatistics);
        return ResponseEntity.status(HttpStatus.OK).body(qrStatistics);
    }


    @GetMapping("/qrStatisticsById/{idQrCode}")
    public ResponseEntity<List<QrStatistics>> getQrStatisticsById(@PathVariable String idQrCode) {
        if (qrStatisticsService.getQrStatisticsByIdQrCodeObject(idQrCode) == null)
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);

        return ResponseEntity.status(HttpStatus.OK).body(qrStatisticsService.getQrStatisticsByIdQrCodeObject(idQrCode));
    }

    @GetMapping("/qrStatisticsById/desktopSize/{idQrCode}")
    public ResponseEntity<Integer> getQrStatisticsByIdDesktopSize(@PathVariable String idQrCode) {
        if (qrStatisticsService.getQrStatisticsByIdQrCodeObject(idQrCode) == null)
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);


        List<QrStatistics> d = qrStatisticsService.getQrStatisticsByIdQrCodeObject(idQrCode);
        Integer desktop = 0;
        for (QrStatistics qr : d) {
            if (qr.getDevice().equals("Desktop")) {
                desktop++;
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(desktop);

    }

}
