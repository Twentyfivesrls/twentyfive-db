package com.twentyfive.twentyfivedb.ticketDB.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import twentyfive.twentyfiveadapter.adapter.Document.TicketObjDocumentDB.AddressBookDocumentDB;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface AddressBookRepository extends MongoRepository<AddressBookDocumentDB, String> {

    List<AddressBookDocumentDB> findAllByFirstName(String firstName);

    List<AddressBookDocumentDB> findAllByLastName(String lastName);

    AddressBookDocumentDB findByEmail(String email);

    List<AddressBookDocumentDB> findAllByUserId(String userId);

    AddressBookDocumentDB findByUserId(String userId);

    Optional<AddressBookDocumentDB> findByFirstNameAndLastNameAndUserIdAndEmail(String firstName, String lastName, String userId, String email);

    long countByUserId(String userId);

    Set<AddressBookDocumentDB> findByUserIdAndEmailContainingIgnoreCase(String userId, String email);
}
