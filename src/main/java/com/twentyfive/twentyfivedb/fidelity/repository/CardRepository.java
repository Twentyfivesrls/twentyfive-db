package com.twentyfive.twentyfivedb.fidelity.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import twentyfive.twentyfiveadapter.models.fidelityModels.Card;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CardRepository extends MongoRepository<Card, String> {

    Page<Card> findAllByNameIgnoreCase(String name, Pageable pageable);
    Set<Card> findAllByOwnerIdAndNameContainingIgnoreCase(
            String ownerId, String name);
    Set<Card> findAllByOwnerIdAndNameContainingIgnoreCaseOrOwnerIdAndSurnameContainingIgnoreCase(
            String ownerId, String name, String ownerId2, String surname);

    Page<Card> findAllByCardGroupId(String groupId, Pageable pageable);

    List<Card> findAllByCardGroupId(String groupId);

    List<Card> findAllByCardGroupIdAndOwnerId(String groupId, String ownerId);

    List<Card> findAllByOwnerId(String ownerId);

    List<Card> findAllByCustomerId(String customerId);

    Optional<Card> findByCardCode(String cardCode);
    Card findByCardCodeContainingIgnoreCase(String cardCode);
    // Java
    Optional<Card> findByEmailAndTypeAndCardGroupId(String email, String type, String cardGroupId);

}
