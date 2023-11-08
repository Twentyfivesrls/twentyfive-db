package com.twentyfive.twentyfivedb.ticketDB.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import twentyfive.twentyfiveadapter.adapter.Document.TicketObjDocumentDB.EventDocumentDB;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface EventRepository extends MongoRepository<EventDocumentDB, String> {

    List<EventDocumentDB> findAllByUserId(String username);

    EventDocumentDB findByNameAndDescriptionAndDateStartAndDateEndAndLocationAndEnabled(String name, String description, LocalDateTime dateStart, LocalDateTime dateEnd, String location, Boolean enabled);
}
