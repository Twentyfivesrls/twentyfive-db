package com.twentyfive.twentyfivedb.ticketDB.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import twentyfive.twentyfiveadapter.adapter.Document.TicketObjDocumentDB.EventDocumentDB;

import java.util.List;
import java.util.Set;


@Repository
public interface EventRepository extends MongoRepository<EventDocumentDB, String> {

    List<EventDocumentDB> findAllByUserId(String username);

    Set<EventDocumentDB> findByUserIdAndNameContainingIgnoreCase(String userId, String name);
    Set<EventDocumentDB> findByUserIdAndDescriptionContainingIgnoreCase(String userId,String description);
}
