package com.twentyfive.twentyfivedb.tictic.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import twentyfive.twentyfiveadapter.models.tictickModels.TicTicCustomer;

import java.util.Set;


@Repository
public interface CustomerRepository extends MongoRepository<TicTicCustomer, String> {
    Page<TicTicCustomer> findAllByOwnerId(String ownerId, Pageable pageable);
    @Query("{ 'ownerId': ?0, '$or': [ { 'name': { $regex: ?1, $options: 'i' } }, { 'lastName': { $regex: ?1, $options: 'i' } }, { 'email': { $regex: ?1, $options: 'i' } } ] }")
    Set<TicTicCustomer> findByOwnerIdAndAnyMatchingFields(String ownerId, String find);
}

