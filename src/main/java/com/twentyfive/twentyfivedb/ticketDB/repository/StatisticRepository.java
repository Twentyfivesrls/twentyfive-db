package com.twentyfive.twentyfivedb.ticketDB.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import twentyfive.twentyfiveadapter.adapter.Document.TicketObjDocumentDB.StatisticDocumentDB;

@Repository
public interface StatisticRepository extends MongoRepository<StatisticDocumentDB, String> {
}
