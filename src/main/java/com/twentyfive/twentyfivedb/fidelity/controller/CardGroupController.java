package com.twentyfive.twentyfivedb.fidelity.controller;

import com.twentyfive.twentyfivedb.fidelity.service.CardGroupService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twentyfive.twentyfiveadapter.adapter.Document.FidelityDocumentDB.CardGroup;

import java.time.LocalDateTime;

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

    @GetMapping("/page-status")
    public ResponseEntity<Page<CardGroup>> getAllCardGroupByStatus(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "lastname") String sortColumn,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam("status") Boolean status

    ) {
        return ResponseEntity.ok(cardGroupService.getAllCardGroupByStatus(page, size, sortColumn, sortDirection, status));
    }

    @GetMapping("/page-date")
    public ResponseEntity<Page<CardGroup>> getGroupByDate(@RequestParam("date") String date,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(cardGroupService.getGroupByDate(date, page, size));
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<CardGroup> getCardGroup(@PathVariable String id) {
        return ResponseEntity.ok(cardGroupService.getCardGroup(id));
    }

    @GetMapping("/get-name")
    public ResponseEntity<Page<CardGroup>> getGroupByName(@RequestParam("name") String name,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(cardGroupService.getGroupByName(name, page, size));
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

    @PutMapping("/status/{id}")
    public ResponseEntity<Void> updateStatus(@PathVariable String id, @RequestParam("status") Boolean status) {
        cardGroupService.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }
}
