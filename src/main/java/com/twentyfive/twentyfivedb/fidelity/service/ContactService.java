package com.twentyfive.twentyfivedb.fidelity.service;

import com.twentyfive.twentyfivedb.fidelity.repository.ContactRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.adapter.Document.FidelityDocumentDB.Contact;

@Service
@Slf4j
public class ContactService {

    private final ContactRepository contactRepository;

    public ContactService(ContactRepository contactRepository) {this.contactRepository = contactRepository;}

    public Page<Contact> getAllContact(int page, int size, String sortColumn, String sortDirection){
        //TODO fix this
        Sort.Direction direction;
        if ("desc".equalsIgnoreCase(sortDirection)) {
            direction = Sort.Direction.DESC;
        } else {
            direction = Sort.Direction.ASC;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortColumn));
        return contactRepository.findAll(pageable);
    }

    public Contact getContact(String id){
        return contactRepository.findById(id).orElse(null);
    }

    public Contact createContact(Contact contact){
        return this.contactRepository.save(contact);
    }

    public void deleteContact(String id){
        this.contactRepository.deleteById(id);
    }

    public void updateContact(String id, Contact contact){
        Contact contact1 = contactRepository.findById(id).orElse(null);

        contact1.setName(contact.getName());
        contact1.setSurname(contact.getSurname());
        contact1.setEmail(contact.getEmail());
        contact1.setPhoneNumber(contact.getPhoneNumber());
        contact1.setCreationDate(contact.getCreationDate());

        contactRepository.save(contact1);
    }
}
