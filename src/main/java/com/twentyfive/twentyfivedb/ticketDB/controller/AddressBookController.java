package com.twentyfive.twentyfivedb.ticketDB.controller;

import com.twentyfive.twentyfivedb.ticketDB.service.AddressBookService;
import com.twentyfive.twentyfivemodel.filterTicket.AddressBookFilter;
import com.twentyfive.twentyfivemodel.filterTicket.AutoCompleteRes;
import com.twentyfive.twentyfivemodel.models.ticketModels.AddressBook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twentyfive.twentyfiveadapter.adapter.Document.TicketObjDocumentDB.AddressBookDocumentDB;
import twentyfive.twentyfiveadapter.adapter.Mapper.TwentyFiveMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/addressbook")
public class AddressBookController {
    private final AddressBookService addressBookService;

    public AddressBookController(AddressBookService addressBookService) {
        this.addressBookService = addressBookService;

    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<AddressBook> deleteAddressBook(@PathVariable String id) {
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

    @GetMapping("/getById/{id}")
    public ResponseEntity<AddressBook> getAddressBookById(@PathVariable String id) {
        AddressBook addressBook = addressBookService.getAddressBookById(id);
        return ResponseEntity.ok(addressBook);
    }

    @GetMapping("/getBy/firstName/{firstName}")
    public ResponseEntity<List<AddressBook>> getAddressBookByFirstName(@PathVariable String firstName) {

        List<AddressBook> addressBookList = addressBookService.getAddressBookByFirstName(firstName);
        return ResponseEntity.ok(addressBookList);
    }

    @GetMapping("/getBy/last/name/{lastName}")
    public ResponseEntity<List<AddressBook>> getAddressBookByLastName(@PathVariable String lastName) {

        List<AddressBook> addressBookList = addressBookService.getAddressBookByLastName(lastName);
        return ResponseEntity.ok(addressBookList);
    }

    @GetMapping("/get/addressBook/by/email")
    public ResponseEntity<AddressBook> getAddressBookByEmail(@RequestParam String email) {
        AddressBook addressBook = addressBookService.getAddressBookByEmail(email);
        return ResponseEntity.ok(addressBook);
    }

    @GetMapping("/findByUsername")
    public ResponseEntity<AddressBook> findByUsername(@RequestParam("username") String username) {
        return ResponseEntity.ok(addressBookService.findByUsername(username));
    }

    @PostMapping("/list")
    public ResponseEntity<Page<AddressBook>> getAddressBookList(@RequestBody AddressBookFilter filter,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "5") int size,
                                                                @RequestParam("username") String username) {
        return ResponseEntity.ok(addressBookService.getAddressBookFiltered(filter, username, page, size));
    }

    @PostMapping("/page")
    public ResponseEntity<Page<AddressBook>> pageAddressBook(@RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "5") int size,
                                                             @RequestParam("username") String username) {
        return ResponseEntity.ok(addressBookService.pageAddressBook(username, page, size));
    }

    @PostMapping("/get/autocomplete")
    public ResponseEntity<Set<AutoCompleteRes>> getEventListAutocomplete(@RequestParam("filterObject") String filterObject, @RequestParam("username") String username) {
        return new ResponseEntity<>(addressBookService.filterSearch(username, filterObject), HttpStatus.OK);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<AddressBook> updateAddressBook(@PathVariable String id, @RequestBody AddressBook addressBook) {

        AddressBook addressBook1 = addressBookService.updateAddressBook(id, addressBook);
        return ResponseEntity.ok(addressBook1);
    }

    @PostMapping("/save/addressbook")
    public AddressBookDocumentDB saveAddressbook(@RequestBody AddressBook addressBook){

        AddressBookDocumentDB addressBookDocumentDB = TwentyFiveMapper.INSTANCE.addressBookToAddressBookDocumentDB(addressBook);
        return addressBookService.saveAddressBook(addressBookDocumentDB);
    }

    @GetMapping("/get/addressbook")
    public List<AddressBook> getAllByUser(@RequestParam("userId") String userId) {
        return addressBookService.getAllAddressByUser(userId);
    }

    @GetMapping("/countRubrica")
    public ResponseEntity<Object> countRubrica(@RequestParam("username") String userId) {
        return new ResponseEntity<>(addressBookService.countByUserId(userId), HttpStatus.OK);
    }


}
