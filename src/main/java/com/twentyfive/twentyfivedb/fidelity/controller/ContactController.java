package com.twentyfive.twentyfivedb.fidelity.controller;

import com.twentyfive.twentyfivedb.fidelity.service.ContactService;
import com.twentyfive.twentyfivedb.fidelity.service.ExportExcelService;
import com.twentyfive.twentyfivedb.fidelity.service.CardService;
import com.twentyfive.twentyfivemodel.filterTicket.AutoCompleteRes;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twentyfive.twentyfiveadapter.dto.fidelityDto.ContactDto;
import twentyfive.twentyfiveadapter.models.fidelityModels.Contact;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@RestController
@RequestMapping("/contact")
public class ContactController {

    private final ContactService contactService;

    private final ExportExcelService exportService;


    public ContactController(ContactService contactService, ExportExcelService exportService) {
        this.contactService = contactService;
        this.exportService = exportService;
    }

    @PostMapping("/filter")
    public ResponseEntity<Page<ContactDto>> getContactListFiltered(@RequestBody ContactDto filterObject,
                                                                   @RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "5") int size,
                                                                   @RequestParam(name = "ownerId") String ownerId) {

        return ResponseEntity.ok(contactService.getContactFiltered(filterObject, page, size, ownerId));
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
    public ResponseEntity<ContactDto> getContact(@PathVariable String id) {
        return ResponseEntity.ok(contactService.getContact(id));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countContacts(@RequestParam String ownerId) {
        return ResponseEntity.ok(contactService.countContacts(ownerId));
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

    @GetMapping(value = "/export/excel/{ownerId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> downloadExcel(@PathVariable String ownerId){
        byte[] excelData = exportService.addressbookExport(ownerId);
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String formattedDateTime = dateTime.format(formatter);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=Lista_Contatti_" + formattedDateTime + ".xlsx")
                .body(excelData);
    }
}
