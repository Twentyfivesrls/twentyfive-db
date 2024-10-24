package com.twentyfive.twentyfivedb.tictic.controller;

import com.twentyfive.twentyfivedb.tictic.service.ShopperService;
import com.twentyfive.twentyfivemodel.filterTicket.AutoCompleteRes;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twentyfive.twentyfiveadapter.models.tictickModels.TicTicCustomer;
import twentyfive.twentyfiveadapter.models.tictickModels.TicTicShopper;

import java.util.Set;

@RestController
@RequestMapping("/tictic/shopper")
public class ShopperController {

    private final ShopperService shopperService;

    public ShopperController(ShopperService shopperService) {
        this.shopperService = shopperService;
    }

    @GetMapping("/counters")
    public ResponseEntity<TicTicShopper> getShopperCounters(@RequestParam(name = "ownerId") String ownerId) {
        return ResponseEntity.ok(shopperService.getShopperCounters(ownerId));
    }

    @GetMapping("/customers")
    public ResponseEntity<Page<TicTicCustomer>> getShopperCustomers(@RequestParam(name = "ownerId") String ownerId,
                                                                    @RequestParam(defaultValue = "0") int page,
                                                                    @RequestParam(defaultValue = "5") int size,
                                                                    @RequestParam(defaultValue = "lastname") String sortColumn,
                                                                    @RequestParam(defaultValue = "asc") String sortDirection) {
        return ResponseEntity.ok(shopperService.getShopperCustomers(ownerId, page, size, sortColumn, sortDirection));
    }

    @GetMapping("/get-customer/{customerId}")
    public ResponseEntity<TicTicCustomer> getCustomer(@PathVariable String customerId) {
        return ResponseEntity.ok(shopperService.getCustomer(customerId));
    }

    @PostMapping("/save-shopper")
    public ResponseEntity<TicTicShopper> saveShopper(@RequestBody TicTicShopper shopper) {
        return ResponseEntity.ok(shopperService.saveShopper(shopper));
    }

    @PostMapping("/save-customer")
    public ResponseEntity<TicTicCustomer> saveCustomer(@RequestBody TicTicCustomer customer) {
        return ResponseEntity.ok(shopperService.saveCustomer(customer));
    }

    @DeleteMapping("/delete-shopper/{shopperId}")
    public ResponseEntity<Void> deleteShopper(@PathVariable String shopperId) {
        shopperService.deleteShopper(shopperId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete-customer/{customerId}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable String customerId) {
        shopperService.deleteCustomer(customerId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/filter/customer/autocomplete")
    public ResponseEntity<Set<AutoCompleteRes>> filterAutocompleteCustomer(@RequestParam("ownerId") String ownerId, @RequestParam("filterObject") String filterObject) {
        return new ResponseEntity<>(shopperService.filterAutocompleteCustomer(filterObject, ownerId), HttpStatus.OK);
    }
}
