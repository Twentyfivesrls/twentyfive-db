package com.twentyfive.twentyfivedb.ticketDB.service;


import com.twentyfive.twentyfivedb.ticketDB.repository.AddressBookRepository;
import com.twentyfive.twentyfivemodel.filterTicket.AddressBookFilter;
import com.twentyfive.twentyfivemodel.models.ticketModels.AddressBook;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.adapter.Document.TicketObjDocumentDB.AddressBookDocumentDB;
import twentyfive.twentyfiveadapter.adapter.Mapper.TwentyFiveMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
@Slf4j
public class AddressBookService {

    private final AddressBookRepository addressBookRepository;

    private final MongoTemplate mongoTemplate;


    public AddressBookService(AddressBookRepository addressBookRepository, MongoTemplate mongoTemplate) {
        this.addressBookRepository = addressBookRepository;
        this.mongoTemplate = mongoTemplate;
    }


    public void deleteAddressBookById(String id) {
        if (StringUtils.isBlank(id)) {
            log.error("Id is null or empty");
            throw new IllegalArgumentException("Id is null or empty");
        }
        addressBookRepository.deleteById(id);
    }

    public void deleteAllAddressBook() {
        addressBookRepository.deleteAll();
    }

    public AddressBook getAddressBookById(String id) {
        if (StringUtils.isBlank(id)) {
            log.error("Id is null or empty");
            throw new IllegalArgumentException("Id is null or empty");
        }
        return TwentyFiveMapper.INSTANCE.addressBookDocumentDBToAddressBook(addressBookRepository.findById(id).orElse(null));
    }



    public List<AddressBook> getAddressBookByFirstName(String firstName) {
        if (StringUtils.isBlank(firstName)) {
            log.error("FirstName is null or empty");
            throw new IllegalArgumentException("FirstName is null or empty");
        }

        List<AddressBook> list = new ArrayList<>();

        List<AddressBookDocumentDB> listDB = addressBookRepository.findByFirstName(firstName);
        for (AddressBookDocumentDB addressBookDocumentDB : listDB) {
            list.add(TwentyFiveMapper.INSTANCE.addressBookDocumentDBToAddressBook(addressBookDocumentDB));
        }

        return list;
    }

    public List<AddressBook> getAddressBookByLastName(String lastName) {
        if (StringUtils.isBlank(lastName)) {
            log.error("LastName is null or empty");
            throw new IllegalArgumentException("LastName is null or empty");
        }

        List<AddressBook> list = new ArrayList<>();

        List<AddressBookDocumentDB> listDB = addressBookRepository.findByLastName(lastName);
        for (AddressBookDocumentDB addressBookDocumentDB : listDB) {
            list.add(TwentyFiveMapper.INSTANCE.addressBookDocumentDBToAddressBook(addressBookDocumentDB));
        }

        return list;

    }


    public AddressBook saveAddressBook(AddressBook addressBook) {
        if (addressBook == null) {
            log.error("AddressBook is null or empty");
            throw new IllegalArgumentException("AddressBook is null or empty");
        }
        addressBookRepository.save(TwentyFiveMapper.INSTANCE.addressBookToAddressBookDocumentDB(addressBook));
        return addressBook;
    }


    public List<AddressBookDocumentDB> findContactByCriteria(AddressBookFilter filterObject) {


        List<Criteria> criteriaList = new ArrayList<>();

        if(StringUtils.isNotBlank(filterObject.getFirstName())){
            Pattern pattern = Pattern.compile(filterObject.getFirstName(), Pattern.CASE_INSENSITIVE);
            criteriaList.add(Criteria.where("firstName").regex(pattern));
        }
        if(StringUtils.isNotBlank(filterObject.getLastName())){
            Pattern pattern = Pattern.compile(filterObject.getLastName(), Pattern.CASE_INSENSITIVE);
            criteriaList.add(Criteria.where("lastName").regex(pattern));
        }
        if(StringUtils.isNotBlank(filterObject.getEmail())){
            Pattern pattern = Pattern.compile(filterObject.getEmail(), Pattern.CASE_INSENSITIVE);
            criteriaList.add(Criteria.where("email").regex(pattern));
        }

        Query query = new Query();
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[criteriaList.size()])));
            return mongoTemplate.find(query, AddressBookDocumentDB.class);
        }



        return mongoTemplate.findAll(AddressBookDocumentDB.class);
    }


    public AddressBook updateAddressBook(String id, AddressBook addressBook) {
        if (StringUtils.isBlank(id)) {
            log.error("Id is null or empty");
            throw new IllegalArgumentException("Id is null or empty");
        }
        if (addressBook == null) {
            log.error("AddressBook is null or empty");
            throw new IllegalArgumentException("AddressBook is null or empty");
        }

        AddressBookDocumentDB findAddressBook = addressBookRepository.findById(id).orElse(null);


        if(findAddressBook == null){
            log.error("AddressBook not found");
            throw new IllegalArgumentException("AddressBook not found");
        }
        findAddressBook.setFirstName(addressBook.getFirstName());
        findAddressBook.setLastName(addressBook.getLastName());
        findAddressBook.setEmail(addressBook.getEmail());
        addressBookRepository.save(findAddressBook);

        return TwentyFiveMapper.INSTANCE.addressBookDocumentDBToAddressBook(findAddressBook);
    }




    public AddressBookDocumentDB findByEmail(String email) {
        if (StringUtils.isBlank(email)) {
            log.error("Email is null or empty");
            throw new IllegalArgumentException("Email is null or empty");
        }
        return addressBookRepository.findByEmail(email);
    }

    public AddressBook getAddressBookByEmail(String email) {
        if (StringUtils.isBlank(email)) {
            log.error("Email is null or empty");
            throw new IllegalArgumentException("Email is null or empty");
        }
        return TwentyFiveMapper.INSTANCE.addressBookDocumentDBToAddressBook(addressBookRepository.findByEmail(email));
    }
}
