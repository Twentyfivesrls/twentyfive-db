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
    @Query("SELECT t FROM TicketDocumentDB t " +
            "WHERE (:userId IS NULL OR t.userId = :userId) " +
            "AND (:email IS NULL OR t.email = :email) " +
            "AND (:eventName IS NULL OR t.eventName = :eventName) " +
            "AND (:startDate IS NULL OR t.eventDateStart >= :startDate) " +
            "AND (:endDate IS NULL OR t.eventDateEnd <= :endDate)")
    Page<TicketDocumentDB> findCustomByFilters(
            @Param("userId") String userId,
            @Param("email") String email,
            @Param("eventName") String eventName,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

}
