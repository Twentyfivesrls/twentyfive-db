package com.twentyfive.twentyfivedb.bustepaga.repository;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import twentyfive.twentyfiveadapter.adapter.Document.BustePagaDocumentDB.Dipendente;

import java.util.List;

@Repository
public interface BustePagaRepository extends MongoRepository<Dipendente, String> {
    List<Dipendente> getAllByUserId(String userId);
}
