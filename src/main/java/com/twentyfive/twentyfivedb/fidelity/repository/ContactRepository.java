package com.twentyfive.twentyfivedb.fidelity.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import twentyfive.twentyfiveadapter.models.fidelityModels.Contact;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ContactRepository extends MongoRepository<Contact, String> {
    Optional<Contact> findByEmail(String email);

    Page<Contact> findAllByNameIgnoreCase(String name, Pageable pageable);

    List<Contact> findAllByOwnerId(String ownerId);
    
    Set<Contact> findAllByNameContainingIgnoreCase(String name);
}
