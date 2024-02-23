package com.twentyfive.twentyfivedb.fidelity.service;

import com.twentyfive.twentyfivedb.Utility;
import com.twentyfive.twentyfivedb.fidelity.repository.ContactRepository;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import twentyfive.twentyfiveadapter.adapter.Document.FidelityDocumentDB.Contact;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ContactService {

    private final ContactRepository contactRepository;

    private final MongoTemplate mongoTemplate;

    public ContactService(ContactRepository contactRepository, MongoTemplate mongoTemplate) {
        this.contactRepository = contactRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public Contact getContact(String id) {
        return contactRepository.findById(id).orElse(null);
    }

    public Page<Contact> getContactByName(String name, int page, int size){
        Pageable pageable= PageRequest.of(page, size);
        return contactRepository.findAllByNameIgnoreCase(name, pageable);
    }

    public Contact createContact(Contact contact) { return this.contactRepository.save(contact); }

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

    /* TODO metodi aggiunta criteri per filtraggio*/
    public Page<Contact> getContactFiltered(Contact filterObject, int page, int size, String sortColumn, String sortDirection) {
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.addAll(parseOtherFilters(filterObject));
        return this.pageMethod(criteriaList, page, size, sortColumn, sortDirection);
    }

    private List<Criteria> parseOtherFilters(Contact filterObject){
        List<Criteria> criteriaList = new ArrayList<>();
        if(filterObject == null){
            return criteriaList;
        }
        if(StringUtils.isNotBlank(filterObject.getName())){
            criteriaList.add(Criteria.where("name").regex(filterObject.getName(), "i"));
        }
        return criteriaList;
    }

    private Page<Contact> pageMethod(List<Criteria> criteriaList, int page, int size, String sortColumn, String sortDirection) {
        Pageable pageable = Utility.makePageableObj(sortDirection, sortColumn, page, size);
        Query query = new Query();
        if(CollectionUtils.isEmpty(criteriaList)){
            log.info("criteria empty");

        } else {
            query = new Query().addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }
        query.with(pageable);

        long total = mongoTemplate.count(query, Contact.class);

        List<Contact> contacts = mongoTemplate.find(query, Contact.class);
        return new PageImpl<>(contacts, pageable, total);
    }
}
