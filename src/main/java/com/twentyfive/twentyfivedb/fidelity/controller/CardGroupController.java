package com.twentyfive.twentyfivedb.fidelity.controller;

import com.twentyfive.twentyfivedb.fidelity.service.CardGroupService;
import com.twentyfive.twentyfivemodel.filterTicket.AutoCompleteRes;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twentyfive.twentyfiveadapter.adapter.Document.FidelityDocumentDB.CardGroup;

import java.util.Set;

@RestController
@RequestMapping("/card-group")
public class CardGroupController {

    private final CardGroupService cardGroupService;

    public CardGroupController(CardGroupService cardGroupService) {
        this.cardGroupService = cardGroupService;
    }

    @PostMapping("/filter")
    public ResponseEntity<Page<CardGroup>> getCardGroupListPagination(@RequestBody CardGroup filterObject,
                                                                      @RequestParam(name = "ownerId") String ownerId,
                                                                      @RequestParam(defaultValue = "0") int page,
                                                                      @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(cardGroupService.getCardGroupFiltered(filterObject, ownerId, page, size));
    }

    @PostMapping("/page")
    public ResponseEntity<Page<CardGroup>> getGroupsListPagination(@RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "5") int size,
                                                                   @RequestParam(name = "ownerId") String ownerId) {
        return new ResponseEntity<>(cardGroupService.pageGroups(page, size, ownerId), HttpStatus.OK);
    }

    @PostMapping("/filter/group/autocomplete")
    public ResponseEntity<Set<AutoCompleteRes>> getGroupListAutocomplete(@RequestParam("ownerId") String ownerId, @RequestParam("filterObject") String filterObject) {
        return new ResponseEntity<>(cardGroupService.filterSearch(filterObject, ownerId), HttpStatus.OK);
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
