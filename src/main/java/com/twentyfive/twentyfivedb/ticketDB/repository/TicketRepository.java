package com.twentyfive.twentyfivedb.ticketDB.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import twentyfive.twentyfiveadapter.adapter.Document.TicketObjDocumentDB.TicketDocumentDB;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TicketRepository  extends MongoRepository<TicketDocumentDB, String> {


    List<TicketDocumentDB> findByEventName(String eventName);

    List<TicketDocumentDB> findByUsed(Boolean status);

    List<TicketDocumentDB> findAllByUserId(String username);
    
    TicketDocumentDB findByCode(String code);

    void deleteByCode(String code);

    List<TicketDocumentDB> findByEventId(String id);

    List<TicketDocumentDB> findByUserIdAndEmailAndEventNameAndEventDateStartGreaterThanEqualAndEventDateEndLessThanEqual(
            String userId, String email, String eventName, LocalDateTime startDate, LocalDateTime endDate);

}
