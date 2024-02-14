package com.twentyfive.twentyfivedb.fidelity.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import twentyfive.twentyfiveadapter.adapter.Document.FidelityDocumentDB.Contact;

import java.util.Optional;

@Repository
public interface ContactRepository extends MongoRepository<Contact, String> {
    Optional<Contact> findByEmail(String email);
}
