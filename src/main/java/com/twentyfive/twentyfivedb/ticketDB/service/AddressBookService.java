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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
@Slf4j
public class AddressBookService {

    private final AddressBookRepository addressBookRepository;

    private final MongoTemplate mongoTemplate;

    public AddressBook findByUsername(String username){
        return TwentyFiveMapper.INSTANCE.addressBookDocumentDBToAddressBook(addressBookRepository.findByUserId(username));

    }

    public AddressBookService(AddressBookRepository addressBookRepository, MongoTemplate mongoTemplate) {
        this.addressBookRepository = addressBookRepository;
        this.mongoTemplate = mongoTemplate;
    }


    public List<AddressBookDocumentDB> findAllByUsername(String username){
        return addressBookRepository.findAllByUserId(username);
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

        List<AddressBookDocumentDB> listDB = addressBookRepository.findAllByFirstName(firstName);
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

        List<AddressBookDocumentDB> listDB = addressBookRepository.findAllByLastName(lastName);
        for (AddressBookDocumentDB addressBookDocumentDB : listDB) {
            list.add(TwentyFiveMapper.INSTANCE.addressBookDocumentDBToAddressBook(addressBookDocumentDB));
        }

        return list;

    }


    public AddressBookDocumentDB saveAddressBook(AddressBookDocumentDB addressBook) {
        if (addressBook == null) {
            log.error("AddressBook is null or empty");
            throw new IllegalArgumentException("AddressBook is null or empty");
        }
        addressBookRepository.save(addressBook);
        return addressBook;
    }


    public List<AddressBookDocumentDB> findContactByCriteria(AddressBookFilter filterObject, String userId) {


        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where("userId").is(userId));


        if (StringUtils.isNotBlank(filterObject.getFirstName())) {
            Pattern pattern = Pattern.compile(filterObject.getFirstName(), Pattern.CASE_INSENSITIVE);
            criteriaList.add(Criteria.where("firstName").regex(pattern));
        }
        if (StringUtils.isNotBlank(filterObject.getLastName())) {
            Pattern pattern = Pattern.compile(filterObject.getLastName(), Pattern.CASE_INSENSITIVE);
            criteriaList.add(Criteria.where("lastName").regex(pattern));
        }
        if (StringUtils.isNotBlank(filterObject.getEmail())) {
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


    public List<AddressBookDocumentDB> filterSearch(String filterObject, String userId){
        Criteria criteriaUserId = Criteria.where("userId").is(userId);
        Criteria criteriaFilter = new Criteria();


        if (StringUtils.isNotBlank(filterObject)) {
            Pattern pattern = Pattern.compile(filterObject, Pattern.CASE_INSENSITIVE);
            criteriaFilter.orOperator(
                    Criteria.where("email").regex(pattern));

        }

        Criteria combinedCriteria = new Criteria().andOperator(criteriaUserId, criteriaFilter);

        Query query = new Query(combinedCriteria);
        return mongoTemplate.find(query, AddressBookDocumentDB.class);

    }


    public AddressBook updateAddressBook(String id, AddressBook addressBook) {
        if (StringUtils.isBlank(id)) {
            log.error("email is null or empty");
            throw new IllegalArgumentException("Id is null or empty");
        }
        if (addressBook == null) {
            log.error("AddressBook is null or empty");
            throw new IllegalArgumentException("AddressBook is null or empty");
        }

        AddressBookDocumentDB findAddressBook = addressBookRepository.findById(id).orElse(null);


        if (findAddressBook == null) {
            log.error("AddressBook not found");
            throw new IllegalArgumentException("AddressBook not found");
        }
        findAddressBook.setFirstName(addressBook.getFirstName());
        findAddressBook.setLastName(addressBook.getLastName());
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

    public List<AddressBook> getAllAddressByUser(String userId) {
        List<AddressBook> list = new ArrayList<>();

        List<AddressBookDocumentDB> listDB = addressBookRepository.findAllByUserId(userId);
        for (AddressBookDocumentDB addressBookDocumentDB : listDB) {
            list.add(TwentyFiveMapper.INSTANCE.addressBookDocumentDBToAddressBook(addressBookDocumentDB));
        }
        return list;
    }

    public long countByUserId(String userId){
        return addressBookRepository.countByUserId(userId);
    }
}


