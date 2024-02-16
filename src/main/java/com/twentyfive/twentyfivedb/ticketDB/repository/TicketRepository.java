package com.twentyfive.twentyfivedb.ticketDB.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import twentyfive.twentyfiveadapter.adapter.Document.TicketObjDocumentDB.TicketDocumentDB;

import java.util.List;
import java.util.Set;

@Repository
public interface TicketRepository  extends MongoRepository<TicketDocumentDB, String> {

    List<TicketDocumentDB> findByEventName(String eventName);
    List<TicketDocumentDB> findByUsed(Boolean status);
    List<TicketDocumentDB> findAllByUserId(String username);
    TicketDocumentDB findByCode(String code);
    List<TicketDocumentDB> findByEventId(String id);
    Set<TicketDocumentDB> findByUserIdAndEmailContainingIgnoreCase(String userId, String email);
}
