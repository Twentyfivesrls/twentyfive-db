package com.twentyfive.twentyfivedb.bustepaga.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import twentyfive.twentyfiveadapter.models.bustepagaModels.Dipendente;

import java.util.List;
import java.util.Optional;

@Repository
public interface BustePagaRepository extends MongoRepository<Dipendente, String> {
    Page<Dipendente> getAllByUserId(String userId, Pageable pageable);
    List<Dipendente> getAllByUserId(String userId);
    Dipendente getDipendenteByUserIdAndId(String userId, String id);
    Optional<Dipendente> findDipendenteByEmail(String email);
}
