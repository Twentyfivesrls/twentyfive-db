package com.twentyfive.twentyfivedb.fidelity.controller;

import com.twentyfive.twentyfivedb.fidelity.service.PrizeService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twentyfive.twentyfiveadapter.models.fidelityModels.Premio;

import java.util.List;

@RestController
@RequestMapping("/prize")
public class PrizeController {

    private final PrizeService prizeService;

    public PrizeController(PrizeService prizeService) {
        this.prizeService = prizeService;
    }

    @GetMapping("/total-number-prize-card/{id}")
    public ResponseEntity<List<Premio>> totalNumberPrizeCard(@PathVariable String id) {
        return ResponseEntity.ok(prizeService.totalNumberPrizeCard(id));
    }

    @GetMapping("/total-number-prize-customer/{id}")
    public ResponseEntity<List<Premio>> totalNumberPrizeCustomer(@PathVariable String id) {
        return ResponseEntity.ok(prizeService.totalNumberPrizeCustomer(id));
    }

    @PostMapping("/page-card/{id}")
    public ResponseEntity<Page<Premio>> pagePrizeCard(@PathVariable String id,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(prizeService.pagePrizeCard(id, page, size));
    }

    @PostMapping("/page-user/{id}")
    public ResponseEntity<Page<Premio>> pagePrizeUser(@PathVariable String id,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(prizeService.pagePrizeUser(id, page, size));
    }

    @PostMapping("/claim-prize/{id}")
    public ResponseEntity<Object> claimPrize(@PathVariable String id) {
        try {
            prizeService.claimPrize(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/claim-last-prize/{cardId}")
    public ResponseEntity<Premio> claimLastPrize(@PathVariable String cardId) {
        try {
            Premio ultimoPremio = prizeService.claimLastPrize(cardId);
            return new ResponseEntity<>(ultimoPremio, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
