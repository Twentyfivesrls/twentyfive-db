package com.twentyfive.twentyfivedb.fidelity.controller;

import com.twentyfive.twentyfivedb.fidelity.service.CardGroupService;
import com.twentyfive.twentyfivedb.fidelity.service.ExportExcelService;
import com.twentyfive.twentyfivemodel.filterTicket.AutoCompleteRes;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twentyfive.twentyfiveadapter.dto.fidelityDto.FilterCardGroupRequest;
import twentyfive.twentyfiveadapter.models.fidelityModels.CardGroup;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@RestController
@RequestMapping("/card-group")
public class CardGroupController {

    private final CardGroupService cardGroupService;

    private final ExportExcelService exportService;


    public CardGroupController(CardGroupService cardGroupService, ExportExcelService exportService) {
        this.cardGroupService = cardGroupService;
        this.exportService = exportService;
    }

    @PostMapping("/filter")
    public ResponseEntity<Page<CardGroup>> getGroupsPaginationFiltered(@RequestBody FilterCardGroupRequest filterObject,
                                                                       @RequestParam(name = "ownerId") String ownerId,
                                                                       @RequestParam(defaultValue = "0") int page,
                                                                       @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(cardGroupService.getCardGroupFiltered(filterObject, ownerId, page, size));
    }

    @PostMapping("/page")
    public ResponseEntity<Page<CardGroup>> getGroupsListPagination(@RequestParam(name = "ownerId") String ownerId,
                                                                   @RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "5") int size) {
        return new ResponseEntity<>(cardGroupService.pageGroups(ownerId, page, size), HttpStatus.OK);
    }

    @PostMapping("/filter/group/autocomplete")
    public ResponseEntity<Set<AutoCompleteRes>> getGroupListAutocomplete(@RequestParam("ownerId") String ownerId, @RequestParam("filterObject") String filterObject) {
        return new ResponseEntity<>(cardGroupService.filterSearch(filterObject, ownerId), HttpStatus.OK);
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<CardGroup> getCardGroup(@PathVariable String id) {
        return ResponseEntity.ok(cardGroupService.getCardGroup(id));
    }

    @GetMapping("/cards-number/{id}")
    public ResponseEntity<Long> getCardNumber(@PathVariable String id) {
        return ResponseEntity.ok(cardGroupService.numberCards(id));
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
        try {
            cardGroupService.deleteCardGroup(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
        }
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

    @GetMapping(value = "/export/excel/{ownerId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> downloadExcel(@PathVariable String ownerId) {
        byte[] excelData = exportService.groupExport(ownerId);
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String formattedDateTime = dateTime.format(formatter);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=Lista_Gruppi_Card_" + formattedDateTime + ".xlsx")
                .body(excelData);
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkSameName(@RequestParam("name") String name) {
        return ResponseEntity.ok(cardGroupService.checkSameName(name));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countGroups(@RequestParam String ownerId) {
        return ResponseEntity.ok(cardGroupService.countGroups(ownerId));
    }
}
