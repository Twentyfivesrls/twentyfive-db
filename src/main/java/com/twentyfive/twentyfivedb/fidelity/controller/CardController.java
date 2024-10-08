package com.twentyfive.twentyfivedb.fidelity.controller;

import com.twentyfive.twentyfivedb.fidelity.exceptions.ExpiredCard;
import com.twentyfive.twentyfivedb.fidelity.exceptions.InvalidCard;
import com.twentyfive.twentyfivedb.fidelity.exceptions.NotFoundException;
import com.twentyfive.twentyfivedb.fidelity.service.CardService;
import com.twentyfive.twentyfivedb.fidelity.service.ExportExcelService;
import com.twentyfive.twentyfivedb.ticketDB.utils.MethodUtils;
import com.twentyfive.twentyfivemodel.dto.qrGenDto.ResponseImage;
import com.twentyfive.twentyfivemodel.filterTicket.AutoCompleteRes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twentyfive.twentyfiveadapter.dto.fidelityDto.FilterCardGroupRequest;
import twentyfive.twentyfiveadapter.models.fidelityModels.Card;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/card")
public class CardController {

    @Value("${fidelity.base.url}")
    private String baseUrl;

    public static final int DEFAULT_QR_WIDTH = 350;
    public static final int DEFAULT_QR_HEIGHT = 350;
    private final ExportExcelService exportService;
    private final CardService cardService;


    public CardController(ExportExcelService exportService, CardService cardService) {
        this.exportService = exportService;
        this.cardService = cardService;
    }

    @PostMapping("/filter")
    public ResponseEntity<Page<Card>> getCardListFilteredPagination(@RequestBody FilterCardGroupRequest filterObject,
                                                                    @RequestParam(defaultValue = "0") int page,
                                                                    @RequestParam(defaultValue = "5") int size,
                                                                    @RequestParam(name = "ownerId") String ownerId) {
        return ResponseEntity.ok(cardService.getCardFiltered(filterObject, page, size, ownerId));
    }

    @PostMapping("/page")
    public ResponseEntity<Page<Card>> getCardListPagination(@RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "5") int size) {
        return new ResponseEntity<>(cardService.pageCard(page, size), HttpStatus.OK);
    }

    @GetMapping("/find-by-groupId")
    public ResponseEntity<Page<Card>> getByGroupId(@RequestParam("groupId") String groupId,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "5") int size){
        return new ResponseEntity<>(cardService.getByGroupId(groupId, page, size), HttpStatus.OK);
    }

    @PostMapping("/filter/card/autocomplete")
    public ResponseEntity<Set<AutoCompleteRes>> getGroupListAutocomplete(@RequestParam("filterObject") String filterObject,
                                                                         @RequestParam("ownerId") String ownerId) {
        return new ResponseEntity<>(cardService.filterSearch(filterObject, ownerId), HttpStatus.OK);
    }

    @GetMapping("/get-name")
    public ResponseEntity<Page<Card>> getGroupByName(@RequestParam("name") String name,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(cardService.getCardByName(name, page, size));
    }

    @PutMapping("/reset-scan-executed/{id}")
    public void resetScanExecuted(@PathVariable String id){
        cardService.resetScanExecuted(id);
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<Card> getCard(@PathVariable String id) {
        return ResponseEntity.ok(cardService.getCard(id));
    }

    @PostMapping("/create")
    public ResponseEntity<Card> createCard(@RequestBody Card card) {
        return ResponseEntity.ok(cardService.createCard(card));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable String id) {
        cardService.deleteCard(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Void> updateCard(@PathVariable String id, @RequestBody Card card) {
        cardService.updateCard(id, card);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<Void> updateStatus(@PathVariable String id, @RequestParam("status") Boolean status) {
        cardService.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/scanning/{id}")
    public ResponseEntity<Card> scanningCard(@PathVariable String id) {
        try{
            return ResponseEntity.ok(cardService.scannerCard(id));
        }catch (NotFoundException e) {
            System.err.println("Card non riconosciuta: " + e.getMessage());
        } catch (ExpiredCard e) {
            System.err.println("Card scaduto: " + e.getMessage());
        } catch (InvalidCard e) {
            System.err.println("Card non valida: " + e.getMessage());
        }
        return null;
    }

    @GetMapping("/generateQrCode/{id}")
    public ResponseEntity<ResponseImage> generateQrCode(@PathVariable String id) {
        try {
            String togenerate = baseUrl + "card-view/" + id;
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

    @GetMapping(value = "/export/excel/{ownerId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> downloadExcel(@PathVariable String ownerId){
        byte[] excelData = exportService.cardExport(ownerId);
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String formattedDateTime = dateTime.format(formatter);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=Lista_Card_" + formattedDateTime + ".xlsx")
                .body(excelData);
    }

    @GetMapping(value = "/export/excel-by-group/{groupId}/{ownerId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> downloadExcel(@PathVariable String groupId, @PathVariable String ownerId){
        byte[] excelData = exportService.cardExportByGroupId(groupId, ownerId);
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String formattedDateTime = dateTime.format(formatter);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=Lista_Card_Associate_" + groupId + formattedDateTime + ".xlsx")
                .body(excelData);
    }

}
