package com.twentyfive.twentyfivedb.fidelity.controller;

import com.twentyfive.twentyfivedb.fidelity.service.CardService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twentyfive.twentyfiveadapter.adapter.Document.FidelityDocumentDB.Card;

@RestController
@RequestMapping("/card")
public class CardController {

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
}
