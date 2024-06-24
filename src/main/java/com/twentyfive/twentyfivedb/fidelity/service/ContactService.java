package com.twentyfive.twentyfivedb.fidelity.service;

import com.twentyfive.twentyfivedb.fidelity.repository.ContactRepository;
import com.twentyfive.twentyfivemodel.filterTicket.AutoCompleteRes;
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
import twentyfive.twentyfiveadapter.models.fidelityModels.Contact;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class ContactService {

    private final ContactRepository contactRepository;

    private final MongoTemplate mongoTemplate;

    private static final String USER_KEY = "ownerId";


    public ContactService(ContactRepository contactRepository, MongoTemplate mongoTemplate) {
        this.contactRepository = contactRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public Page<Contact> pageContact(int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        return contactRepository.findAll(pageable);
    }

    public List<Contact> findAll(String ownerId){
        return contactRepository.findAllByOwnerId(ownerId);
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
    public Page<Contact> getContactFiltered(Contact filterObject, int page, int size, String ownerId) {
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where(USER_KEY).is(ownerId));
        criteriaList.addAll(parseOtherFilters(filterObject));
        return this.pageMethod(criteriaList, page, size);
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

    private Page<Contact> pageMethod(List<Criteria> criteriaList, int page, int size) {
        Query query = new Query();
        if(CollectionUtils.isEmpty(criteriaList)){
            log.info("criteria empty");

        } else {
            query = new Query().addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        long total = mongoTemplate.count(query, Contact.class);
        Pageable pageable = PageRequest.of(page, size);
        query.with(pageable);

        List<Contact> contacts = mongoTemplate.find(query, Contact.class);
        return new PageImpl<>(contacts, pageable, total);
    }

    public Set<AutoCompleteRes> filterSearch(String find){
        //Set<Contact> contacts = contactRepository.findAllByNameContainingIgnoreCase(find);
        //Set<Contact> contacts = contactRepository.findAllByNameContainingIgnoreCaseOrSurnameContainingIgnoreCase(find, find);
        Set<Contact> contacts = contactRepository.findAllByNameContainingIgnoreCaseOrSurnameContainingIgnoreCaseOrEmailContainingIgnoreCase(find, find,find);
        Set<AutoCompleteRes> setCombinato = new HashSet<>();
        for (Contact contact : contacts) {
            AutoCompleteRes temp = new AutoCompleteRes(contact.getName() + " " + contact.getSurname() + " - " + contact.getEmail());
            setCombinato.add(temp);
        }
        return setCombinato;
    }

    public Long countContacts(String ownerId) {
        return (long) contactRepository.findAllByOwnerId(ownerId).size();
    }
}
