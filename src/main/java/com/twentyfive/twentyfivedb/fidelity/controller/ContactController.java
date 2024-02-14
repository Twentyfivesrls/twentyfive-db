package com.twentyfive.twentyfivedb.fidelity.controller;

import com.twentyfive.twentyfivedb.fidelity.service.ContactService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twentyfive.twentyfiveadapter.adapter.Document.FidelityDocumentDB.Contact;

@RestController
@RequestMapping("/contact")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping("/page")
    public ResponseEntity<Page<Contact>> getAllContact(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "lastname") String sortColumn,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        return ResponseEntity.ok(contactService.getAllContact(page, size, sortColumn, sortDirection));
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
