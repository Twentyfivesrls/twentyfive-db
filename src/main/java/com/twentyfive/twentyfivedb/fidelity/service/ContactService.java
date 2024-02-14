package com.twentyfive.twentyfivedb.fidelity.service;

import com.twentyfive.twentyfivedb.Utility;
import com.twentyfive.twentyfivedb.fidelity.repository.ContactRepository;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.adapter.Document.FidelityDocumentDB.Contact;

import java.util.Optional;

@Service
@Slf4j
public class ContactService {

    private final ContactRepository contactRepository;

    public ContactService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    public Page<Contact> getAllContact(int page, int size, String sortColumn, String sortDirection) {
        Pageable pageable = Utility.makePageableObj(sortDirection, sortColumn, page, size);
        return contactRepository.findAll(pageable);
    }

    public Contact getContact(String id) {
        return contactRepository.findById(id).orElse(null);
    }

    public Contact createContact(Contact contact) {
        Optional<Contact> contact1 = contactRepository.findByEmail(contact.getEmail());
        if(contact1.isPresent()){
            throw new RuntimeException("Contact already exist");
        }
        return this.contactRepository.save(contact);
    }

    public void deleteContact(String id) {
        this.contactRepository.deleteById(id);
    }

    public void updateContact(String id, Contact contact) {
        if (contact == null) {
            return;
        }

        if (StringUtils.isBlank(id)) {
            //TODO
            return;
        }

        Contact contact1 = contactRepository.findById(id).orElse(null);

        if (contact1 == null) {
            this.createContact(contact);
        } else {
            contact1.setName(contact.getName());
            contact1.setSurname(contact.getSurname());
            contact1.setEmail(contact.getEmail());
            contact1.setPhoneNumber(contact.getPhoneNumber());
            contact1.setCreationDate(contact.getCreationDate());
            contactRepository.save(contact1);
        }
    }
}
