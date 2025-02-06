package com.twentyfive.twentyfivedb.tictic.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import twentyfive.twentyfiveadapter.models.tictickModels.TicTicCustomer;

import java.util.Optional;
import java.util.Set;


@Repository
public interface CustomerRepository extends MongoRepository<TicTicCustomer, String> {
    Page<TicTicCustomer> findAllByOwnerId(String ownerId, Pageable pageable);
  @Query("{ '$or': [ { 'name': { $regex: ?0, $options: 'i' } }, { 'lastName': { $regex: ?0, $options: 'i' } }, { 'email': { $regex: ?0, $options: 'i' } } ] }")
  Set<TicTicCustomer> findByAnyMatchingFields(String find);
    Optional<TicTicCustomer> findByEmail(String email);

    boolean existsByOwnerId(String ownerId);

    void deleteByIdAndOwnerId(String customerId, String ownerId);

    boolean existsByEmail(String email, String ownerId);

    Page<TicTicCustomer> findAllByEmailContainingIgnoreCase(String email, Pageable pageable);

    Page<TicTicCustomer> findByOwnerId(String ownerId, Pageable pageable);
}

