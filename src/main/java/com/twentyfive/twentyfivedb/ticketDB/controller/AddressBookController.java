package com.twentyfive.twentyfivedb.ticketDB.controller;


import com.twentyfive.twentyfivedb.ticketDB.service.AddressBookService;
import com.twentyfive.twentyfivedb.ticketDB.utils.MethodUtils;
import com.twentyfive.twentyfivemodel.filterTicket.AddressBookFilter;
import com.twentyfive.twentyfivemodel.models.ticketModels.AddressBook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twentyfive.twentyfiveadapter.adapter.Document.TicketObjDocumentDB.AddressBookDocumentDB;
import twentyfive.twentyfiveadapter.adapter.Mapper.TwentyFiveMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/addressbook")
public class AddressBookController {
    private final AddressBookService addressBookService;


    public AddressBookController(AddressBookService addressBookService) {
        this.addressBookService = addressBookService;

    }

    /*
        delete address book
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<AddressBook> deleteAddressBook(@PathVariable String id){
        addressBookService.deleteAddressBookById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /*
        get address book by id
     */
    @GetMapping("/getById/{id}")
    public ResponseEntity<AddressBook> getAddressBookById(@PathVariable String id){
        AddressBook addressBook = addressBookService.getAddressBookById(id);
        return ResponseEntity.ok(addressBook);
    }


    /*
        get address book by first name
     */
    @GetMapping("/getBy/firstName/{firstName}")
    public ResponseEntity<List<AddressBook>> getAddressBookByFirstName(@PathVariable String firstName){

        List<AddressBook> addressBookList = addressBookService.getAddressBookByFirstName(firstName);
        return ResponseEntity.ok(addressBookList);
    }

    /*
        get address book by last name
     */
    @GetMapping("/getBy/last/name/{lastName}")
    public ResponseEntity<List<AddressBook>> getAddressBookByLastName(@PathVariable String lastName){

        List<AddressBook> addressBookList = addressBookService.getAddressBookByLastName(lastName);
        return ResponseEntity.ok(addressBookList);
    }

    /*
        get address book by date of birth
     */
    @GetMapping("/get/addressBook/by/email")
    public ResponseEntity<AddressBook> getAddressBookByEmail(@RequestParam String email){

        AddressBook addressBookList = addressBookService.getAddressBookByEmail(email);
        return ResponseEntity.ok(addressBookList);
    }


    /*
     * Get address book list end filters
     */
    @PostMapping("/list")
    public ResponseEntity<Page<AddressBook>> getAddressBookList(@RequestBody AddressBookFilter filter){

        log.info("filter: {}", filter);
        System.out.println("filter: " + filter);

        Pageable pageable = MethodUtils.makePageableFromFilter(filter);
        List<AddressBook> addressBookList = addressBookService.findContactByCriteria(filter);
        log.info("addressBookList: {}", addressBookList);
        System.out.println("addressBookList: " + addressBookList);
        List<AddressBook> mapList = new ArrayList<>();
        /*for (AddressBookDocumentDB addressBookDocumentDB : addressBookList) {
            mapList.add(TwentyFiveMapper.INSTANCE.addressBookDocumentDBToAddressBook(addressBookDocumentDB));
        }*/
        Page<AddressBook> addressBookPage = MethodUtils.convertListToPage(addressBookList, pageable);
        return ResponseEntity.ok(addressBookPage);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<AddressBook> updateAddressBook(@PathVariable String id, @RequestBody AddressBook addressBook){

        AddressBook addressBook1 = addressBookService.updateAddressBook(id, addressBook);
        return ResponseEntity.ok(addressBook1);
    }


}
