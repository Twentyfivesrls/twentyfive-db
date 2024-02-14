package com.twentyfive.twentyfivedb.fidelity.controller;

import com.twentyfive.twentyfivedb.fidelity.service.CardGroupService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twentyfive.twentyfiveadapter.adapter.Document.FidelityDocumentDB.CardGroup;


@RestController
@RequestMapping("/card-group")
public class CardGroupController {

    private final CardGroupService cardGroupService;

    public CardGroupController(CardGroupService cardGroupService) {
        this.cardGroupService = cardGroupService;
    }

    @GetMapping("/page")
    public ResponseEntity<Page<CardGroup>> getAllCardGroup(
            @RequestParam(name = "ownerId") String ownerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "lastname") String sortColumn,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        return ResponseEntity.ok(cardGroupService.getAllCardGroup(ownerId, page, size, sortColumn, sortDirection));
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<CardGroup> getCardGroup(@PathVariable String id) {
        return ResponseEntity.ok(cardGroupService.getCardGroup(id));
    }

    @PostMapping("/create")
    public ResponseEntity<CardGroup> createCardGroup(@RequestBody CardGroup cardGroup) {
        return ResponseEntity.ok(cardGroupService.createCardGroup(cardGroup));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteCardGroup(@PathVariable String id) {
        cardGroupService.deleteCardGroup(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Void> updateCardGroup(@PathVariable String id, @RequestBody CardGroup cardGroup) {
        cardGroupService.updateCardGroup(id, cardGroup);
        return ResponseEntity.ok().build();
    }

}
