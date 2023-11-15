package com.twentyfive.twentyfivedb.ticketDB.repository;

import com.twentyfive.twentyfivemodel.models.ticketModels.Ticket;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import twentyfive.twentyfiveadapter.adapter.Document.TicketObjDocumentDB.TicketDocumentDB;

import java.util.List;

@Repository
public interface TicketRepository  extends MongoRepository<TicketDocumentDB, String> {


    List<TicketDocumentDB> findByEventName(String eventName);

    List<TicketDocumentDB> findByUsed(Boolean status);

    List<TicketDocumentDB> findAllByUserId(String username);
    
    TicketDocumentDB findByCode(String code);

    void deleteByCode(String code);

    //List<TicketDocumentDB> finAllByEventId(String id);
}
