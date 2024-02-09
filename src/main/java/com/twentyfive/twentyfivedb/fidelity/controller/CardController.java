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

        public CardController(CardService cardService) {this.cardService = cardService;}

        @GetMapping("/page")
        public ResponseEntity<Page<Card>> getAllCard(
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "5") int size,
                @RequestParam(defaultValue = "lastname") String sortColumn,
                @RequestParam(defaultValue = "asc") String sortDirection
        ) {
                return ResponseEntity.ok(cardService.getAllCard(page, size, sortColumn, sortDirection));
        }

        @GetMapping("/detail/{id}")
        public ResponseEntity<Card> getCard(@PathVariable String id){
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

        @PutMapping("/update-active/{id}")
        public ResponseEntity<Void> updateActive(@PathVariable String id, @RequestParam("active") Boolean isActive){
                cardService.updateActive(id, isActive);
                return ResponseEntity.ok().build();
        }
}
