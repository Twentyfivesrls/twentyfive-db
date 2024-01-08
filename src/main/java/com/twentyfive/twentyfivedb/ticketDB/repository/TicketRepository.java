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
    @Query("SELECT e FROM TicketDocumentDB e " +
            "WHERE (:startDate IS NULL OR e.eventDateStart > :startDate) " +
            "AND (:endDate IS NULL OR e.eventDateEnd < :endDate) " +
            "AND (:userId IS NULL OR e.userId = :userId) " +
            "AND (:email IS NULL OR e.email = :email) " +
            "AND (:eventName IS NULL OR e.eventName = :eventName)")
    Page<TicketDocumentDB> findCustomByTwoDatesAndConditions(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("userId") String userId,
            @Param("email") String email,
            @Param("eventName") String eventName,
            Pageable pageable);

}
