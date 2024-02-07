package com.twentyfive.twentyfivedb.fidelity.repository;

import com.twentyfive.twentyfivemodel.models.fidelityModels.Contact;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactRepository extends MongoRepository<Contact, String> {
}
