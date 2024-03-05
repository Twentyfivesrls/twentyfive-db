package com.twentyfive.twentyfivedb.fidelity.controller;

import com.twentyfive.twentyfivedb.fidelity.service.ContactService;
import com.twentyfive.twentyfivemodel.filterTicket.AutoCompleteRes;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twentyfive.twentyfiveadapter.adapter.Document.FidelityDocumentDB.Contact;

import java.util.Set;

@RestController
@RequestMapping("/contact")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping("/filter")
    public ResponseEntity<Page<Contact>> getContactListFiltered(@RequestBody Contact filterObject,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "5") int size) {

        return ResponseEntity.ok(contactService.getContactFiltered(filterObject, page, size));
    }

    @PostMapping("/page")
    public ResponseEntity<Page<Contact>> getContactListPaginated(@RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "5") int size) {

        return ResponseEntity.ok(contactService.pageContact(page, size));
    }

    @PostMapping("/filter/contact/autocomplete")
    public ResponseEntity<Set<AutoCompleteRes>> getGroupListAutocomplete(@RequestParam("filterObject") String filterObject) {
        return new ResponseEntity<>(contactService.filterSearch(filterObject), HttpStatus.OK);
    }

    @GetMapping("/get-name")
    public ResponseEntity<Page<Contact>> getContactByName(@RequestParam("name") String name,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(contactService.getContactByName(name, page, size));
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<Contact> getContact(@PathVariable String id) {
        return ResponseEntity.ok(contactService.getContact(id));
    }

    @PostMapping("/create")
    public ResponseEntity<Contact> createContact(@RequestBody Contact contact) {
        return ResponseEntity.ok(contactService.createContact(contact));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable String id) {
        contactService.deleteContact(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Void> updateContact(@PathVariable String id, @RequestBody Contact contact) {
        contactService.updateContact(id, contact);
        return ResponseEntity.ok().build();
    }

}
