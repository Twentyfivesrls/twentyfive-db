package com.twentyfive.twentyfivedb.qrGenDB.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import twentyfive.twentyfiveadapter.models.qrGenModels.QrCodeGroup;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface QrCodeGroupRepository extends MongoRepository<QrCodeGroup, String> {
    @Query(value = "{ 'ownerId' : ?0 }", fields = "{ 'groupName' : 1 }")
    List<QrCodeGroup> findAllGroupNamesByOwnerId(String ownerId);
    Optional<QrCodeGroup> findByIdQrCode(String idQrCode);
    Optional<QrCodeGroup> findByAnimalId(String animalId);
    List<QrCodeGroup> findAllByUsernameAndAndGroupName(String username, String groupName);
    List<QrCodeGroup> findAllByUsername(String username);
    List<QrCodeGroup> findAllByCustomerId(String customerId);
    boolean existsByOwnerId(String ownerId);
    Page<QrCodeGroup> findByOwnerId(String ownerId, Pageable pageable);
    List<QrCodeGroup> findAllByOwnerIdAndNameQrCodeContainsIgnoreCaseAndCustomerIdNull(String ownerId, String name);

    //Page<QrCodeGroup> findByOwnerIdAndCustomerId(String ownerId, String customerId, Pageable pageable);

  @Query("{ 'ownerId': ?0, '$or': [ { 'customerId': ?1 }, { 'idQrCode': ?1 } ] }")
  Page<QrCodeGroup> findByOwnerIdAndSearchString(String ownerId, String searchString, Pageable pageable);


  @Query(value = "{}", count = true)
    Long countAllDocuments();

    Optional<QrCodeGroup> findByOwnerIdAndNameQrCode(String ownerId, String nameQrCode);

    @Query("{ 'ownerId': ?0, '$or': [ { 'nameQrCode': { $regex: ?1, $options: 'i' }}]}")
    Set<QrCodeGroup> findByOwnerIdAndAnyMatchingFields(String ownerId, String find);
}
