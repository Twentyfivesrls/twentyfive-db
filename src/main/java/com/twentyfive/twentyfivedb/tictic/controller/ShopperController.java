package com.twentyfive.twentyfivedb.tictic.controller;

import com.twentyfive.twentyfivedb.tictic.service.ShopperService;
import com.twentyfive.twentyfivemodel.filterTicket.AutoCompleteRes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twentyfive.twentyfiveadapter.models.qrGenModels.QrCodeGroup;
import twentyfive.twentyfiveadapter.models.tictickModels.TTAnimal;
import twentyfive.twentyfiveadapter.models.tictickModels.TicTicCustomer;
import twentyfive.twentyfiveadapter.models.tictickModels.TicTicShopper;

import java.util.List;
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

    @GetMapping("/customers/search")
    public ResponseEntity<Page<TicTicCustomer>> getShopperCustomersWithEmail(@RequestParam(name = "email") String email,
                                                                             @RequestParam(defaultValue = "0") int page,
                                                                             @RequestParam(defaultValue = "5") int size,
                                                                             @RequestParam(defaultValue = "lastname") String sortColumn,
                                                                             @RequestParam(defaultValue = "asc") String sortDirection) {
      return ResponseEntity.ok(shopperService.getShopperCustomersWithEmail(email, page, size, sortColumn, sortDirection));
    }

  @GetMapping("/customers")
  public ResponseEntity<Page<TicTicCustomer>> getAllCustomers(@RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "5") int size,
                                                              @RequestParam(defaultValue = "lastname") String sortColumn,
                                                              @RequestParam(defaultValue = "asc") String sortDirection) {
    return ResponseEntity.ok(shopperService.getAllCustomers(page, size, sortColumn, sortDirection));
  }


    @GetMapping("/getAnimalByQrCode/{idQrCode}")
    public ResponseEntity<TTAnimal> getAnimalByIdQrCode(@PathVariable("idQrCode") String idQrCode) {
        return ResponseEntity.ok(shopperService.getAnimalByIdQrCode(idQrCode));
    }

    @GetMapping("/get-customer/{customerId}")
    public ResponseEntity<TicTicCustomer> getCustomer(@PathVariable String customerId) {
        return ResponseEntity.ok(shopperService.getCustomer(customerId));
    }

    @PostMapping("/save-customer")
    public ResponseEntity<TicTicCustomer> saveCustomer(@RequestBody TicTicCustomer customer) {
        return ResponseEntity.ok(shopperService.saveCustomer(customer));
    }

    @PostMapping("/save-shopper")
    public ResponseEntity<TicTicShopper> saveShopper(@RequestBody TicTicShopper shopper) {
        return ResponseEntity.ok(shopperService.saveShopper(shopper));
    }

    @DeleteMapping("/delete-shopper/{shopperId}")
    public ResponseEntity<Void> deleteShopper(@PathVariable String shopperId) {
        shopperService.deleteShopper(shopperId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/customer/{customerId}")
    public ResponseEntity<Void> deleteCustomer(@RequestParam("ownerId") String ownerId, @PathVariable String customerId) {
        shopperService.deleteCustomer(customerId, ownerId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/filter/customer/autocomplete")
    public ResponseEntity<Set<AutoCompleteRes>> filterAutocompleteCustomer(@RequestParam("ownerId") String ownerId, @RequestParam("filterObject") String filterObject) {
        return new ResponseEntity<>(shopperService.filterAutocompleteCustomer(filterObject, ownerId), HttpStatus.OK);
    }

    /*@PostMapping("/associate-qrcode")
    public ResponseEntity<TicTicQrCodeCustomerAssociations> associateQRCodeWithCustomer(
            @RequestParam String ownerId,
            @RequestParam String qrCodeId,
            @RequestParam String customerId) {

        TicTicQrCodeCustomerAssociations association = shopperService.associateQRCodeWithCustomer(ownerId, qrCodeId, customerId);
        return ResponseEntity.ok(association);
    }*/

  @GetMapping("/check-customer-qrcode")
  public ResponseEntity<Boolean> checkCustomerAndQRCodeExists(@RequestParam String ownerId) {
    boolean exists = shopperService.checkCustomerAndQRCodeExists(ownerId);
    return ResponseEntity.ok(exists);
  }

  @GetMapping("/getQrcodeList")
  public Page<QrCodeGroup> getQrCodes(
    @RequestParam String ownerId,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size,
    @RequestParam(defaultValue = "customerId") String sortColumn, // Default column for sorting
    @RequestParam(defaultValue = "asc") String sortDirection // Default direction for sorting
  ) {
    return shopperService.getQrCodes(ownerId, page, size, sortColumn, sortDirection);
  }

  @GetMapping("/getQrcodeListByCustomer")
  public Page<QrCodeGroup> getQrCodesByCustomer(
    @RequestParam String ownerId,
    @RequestParam String customerId,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size,
    @RequestParam(defaultValue = "customerId") String sortColumn, // Colonna di ordinamento predefinita
    @RequestParam(defaultValue = "asc") String sortDirection // Direzione di ordinamento predefinita
  ) {
    return shopperService.getQrCodesByCustomer(ownerId, customerId, page, size, sortColumn, sortDirection);
  }

    @GetMapping("/getQrCodesCustomer/{username}")
    public List<QrCodeGroup> getQrCodesForShopper(@PathVariable String username) {
        return shopperService.getQrCodesForShopper(username);
    }

    @PostMapping("/associateQrCodeWhitCustomer")
    public ResponseEntity<Boolean> associateQRCodeWithCustomer(
            @RequestParam("ownerId") String ownerId,
            @RequestParam("qrCodeId") String qrCodeId,
            @RequestParam("customerId") String customerId,
            @RequestBody TTAnimal animal) {
        shopperService.associateQRCodeWithCustomer(ownerId, qrCodeId, customerId, animal);
        return ResponseEntity.ok(true);
    }

    @GetMapping("/unassigned-qrcodes")
    public ResponseEntity<List<QrCodeGroup>> getUnassignedQrCodes(@RequestParam("ownerId") String ownerId, @RequestParam("name") String name) {
      List<QrCodeGroup> unassignedQrCodes = shopperService.getUnassignedQrCodes(ownerId, name);
      return ResponseEntity.ok(unassignedQrCodes);
    }
}
