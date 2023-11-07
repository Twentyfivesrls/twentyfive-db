package com.twentyfive.twentyfivedb.ticketDB.controller;


import com.twentyfive.twentyfivedb.ticketDB.service.AddressBookService;
import com.twentyfive.twentyfivedb.ticketDB.utils.MethodUtils;
import com.twentyfive.twentyfivemodel.filterTicket.AddressBookFilter;
import com.twentyfive.twentyfivemodel.filterTicket.FilterObject;
import com.twentyfive.twentyfivemodel.models.ticketModels.AddressBook;
import com.twentyfive.twentyfivemodel.models.ticketModels.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twentyfive.twentyfiveadapter.adapter.Document.TicketObjDocumentDB.AddressBookDocumentDB;
import twentyfive.twentyfiveadapter.adapter.Document.TicketObjDocumentDB.EventDocumentDB;
import twentyfive.twentyfiveadapter.adapter.Document.UserLinkDocumentDB;
import twentyfive.twentyfiveadapter.adapter.Mapper.TwentyFiveMapper;

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

   @GetMapping("/allElement")
    public ResponseEntity<List<AddressBook>> getEventList(@RequestParam("username") String username) {
        List<AddressBookDocumentDB> list = addressBookService.findAllByUsername(username);
        List<AddressBook> mapList = new ArrayList<>();
        for (AddressBookDocumentDB addressBookDocumentDB : list) {
            mapList.add(TwentyFiveMapper.INSTANCE.INSTANCE.addressBookDocumentDBToAddressBook(addressBookDocumentDB));
        }
        return ResponseEntity.ok(mapList);
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
        get address book by email
     */
    @GetMapping("/get/addressBook/by/email")
    public ResponseEntity<AddressBook> getAddressBookByEmail(@RequestParam String email){
        AddressBook addressBook = addressBookService.getAddressBookByEmail(email);
        return ResponseEntity.ok(addressBook);
    }

    /*
        get address book by username
     */
    @GetMapping("/findByUsername")
    public ResponseEntity<AddressBookDocumentDB> findByUsername(@RequestParam("username") String username){
        return new ResponseEntity(addressBookService.findByUsername(username), HttpStatus.OK);
    }

    /*
     * Get address book list end filters
     */
    @PostMapping("/list")
    public ResponseEntity<Page<AddressBook>> getAddressBookList(@RequestBody AddressBookFilter filter, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size, @RequestParam("username") String username){

        FilterObject filterObject = new FilterObject(page,size);

        Pageable pageable = MethodUtils.makePageableFromFilter(filterObject);
        List<AddressBookDocumentDB> addressBookList = addressBookService.findContactByCriteria(filter, username);

        List<AddressBook> mapList = new ArrayList<>();
        for (AddressBookDocumentDB addressBookDocumentDB : addressBookList) {
            mapList.add(TwentyFiveMapper.INSTANCE.addressBookDocumentDBToAddressBook(addressBookDocumentDB));
        }
        Page<AddressBook> addressBookPage = MethodUtils.convertListToPage(mapList, pageable);
        return ResponseEntity.ok(addressBookPage);
    }

    @PostMapping("/get/autocomplete")
    public ResponseEntity<Page<AddressBook>> getEventListAutocomplete(@RequestParam("filterObject") String filterObject, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size, @RequestParam("username") String username) {

        FilterObject filter = new FilterObject(page, size);
        Pageable pageable = MethodUtils.makePageableFromFilter(filter);
        List<AddressBookDocumentDB> eventPage = addressBookService.filterSearch(filterObject,username);
        List<AddressBook> aList = new ArrayList<>();
        for (AddressBookDocumentDB addressBookDocumentDB : eventPage) {
            aList.add(TwentyFiveMapper.INSTANCE.addressBookDocumentDBToAddressBook(addressBookDocumentDB));
        }
        Page<AddressBook> aRes = MethodUtils.convertListToPage(aList, pageable);
        return ResponseEntity.ok(aRes);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<AddressBook> updateAddressBook(@PathVariable String id, @RequestBody AddressBook addressBook){

        AddressBook addressBook1 = addressBookService.updateAddressBook(id, addressBook);
        return ResponseEntity.ok(addressBook1);
    }

    @PostMapping("/save/addressbook")
    public AddressBookDocumentDB saveAddressbook(@RequestBody AddressBook addressBook){

        AddressBookDocumentDB addressBookDocumentDB = TwentyFiveMapper.INSTANCE.addressBookToAddressBookDocumentDB(addressBook);
        return addressBookService.saveAddressBook(addressBookDocumentDB);
    }

    @GetMapping("/get/addressbook")
    public List<AddressBook> getAllByUser(@RequestParam("userId") String userId){
        return addressBookService.getAllAddressByUser(userId);
    }


}
