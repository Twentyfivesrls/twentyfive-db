package com.twentyfive.twentyfivedb.tictic.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import twentyfive.twentyfiveadapter.models.tictickModels.TicTicQrCodeCustomerAssociations;

@Repository
public interface TicTicCodeCustomerAssociationRepository extends MongoRepository<TicTicQrCodeCustomerAssociations, String> {

    Page<TicTicQrCodeCustomerAssociations> findByOwnerId(String ownerId, Pageable pageable);
}
