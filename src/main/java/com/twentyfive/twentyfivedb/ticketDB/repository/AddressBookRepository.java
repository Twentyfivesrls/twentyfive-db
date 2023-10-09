package com.twentyfive.twentyfivedb.ticketDB.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import twentyfive.twentyfiveadapter.adapter.Document.TicketObjDocumentDB.AddressBookDocumentDB;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AddressBookRepository extends MongoRepository<AddressBookDocumentDB, String> {

    List<AddressBookDocumentDB> findByFirstName(String firstName);

    List<AddressBookDocumentDB> findByLastName(String lastName);

    List<AddressBookDocumentDB> findByDateOfBirth(LocalDateTime dateOfBirth);
    
    AddressBookDocumentDB findByFirstNameAndLastNameAndEmail(String firstName, String lastName, String email);

    AddressBookDocumentDB findByEmail(String email);
}
