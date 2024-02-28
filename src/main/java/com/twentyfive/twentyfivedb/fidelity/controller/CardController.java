package com.twentyfive.twentyfivedb.fidelity.controller;

import com.google.zxing.WriterException;
import com.twentyfive.twentyfivedb.fidelity.service.CardService;
import com.twentyfive.twentyfivedb.ticketDB.utils.MethodUtils;
import com.twentyfive.twentyfivemodel.dto.qrGenDto.ResponseImage;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twentyfive.twentyfiveadapter.adapter.Document.FidelityDocumentDB.Card;

import java.io.IOException;
import java.util.Base64;

@RestController
@RequestMapping("/card")
public class CardController {

    private final String baseUrl = "http://localhost:8080";

    public static final int DEFAULT_QR_WIDTH = 350;
    public static final int DEFAULT_QR_HEIGHT = 350;

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping("/filter")
    public ResponseEntity<Page<Card>> getCardListPagination(@RequestBody Card filterObject,
                                                            @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "5") int size,
                                                            @RequestParam(defaultValue = "lastname") String sortColumn,
                                                            @RequestParam(defaultValue = "asc") String sortDirection) {
        return ResponseEntity.ok(cardService.getCardFiltered(filterObject, page, size, sortColumn, sortDirection));
    }

    @GetMapping("/get-name")
    public ResponseEntity<Page<Card>> getGroupByName(@RequestParam("name") String name,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(cardService.getCardByName(name, page, size));
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

    @GetMapping("/generateQrCode/{id}")
        public ResponseEntity<ResponseImage> generateQrCode(@PathVariable String id) throws IOException, WriterException {
        /*byte[] qrCode = MethodUtils.generateQrCodeImage(id, 350, 350);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=qrCode.png")
                .body(qrCode);
         */
        try {
            String togenerate = baseUrl + "crudStats/" + id;
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
}
