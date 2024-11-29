package com.twentyfive.twentyfivedb.qrGenDB.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import twentyfive.twentyfiveadapter.models.qrGenModels.QrCodeGroup;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface QrCodeGroupRepository extends MongoRepository<QrCodeGroup, String> {
    @Query(value = "{ 'ownerId' : ?0 }", fields = "{ 'groupName' : 1 }")
    List<QrCodeGroup> findAllGroupNamesByOwnerId(String ownerId);
    List<QrCodeGroup> findAllByUsernameAndAndGroupName(String username, String groupName);
    List<QrCodeGroup> findAllByUsername(String username);
    boolean existsByOwnerId(String ownerId);
    Page<QrCodeGroup> findByOwnerId(String ownerId, Pageable pageable);
    List<QrCodeGroup> findAllByOwnerIdAndNameQrCodeContainsIgnoreCaseAndCustomerIdNull(String ownerId, String name);


  @Query(value = "{}", count = true)
    Long countAllDocuments();

    Optional<QrCodeGroup> findByOwnerIdAndNameQrCode(String ownerId, String nameQrCode);

    @Query("{ 'ownerId': ?0, '$or': [ { 'nameQrCode': { $regex: ?1, $options: 'i' }}]}")
    Set<QrCodeGroup> findByOwnerIdAndAnyMatchingFields(String ownerId, String find);
}
