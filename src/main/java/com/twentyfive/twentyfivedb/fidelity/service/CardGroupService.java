package com.twentyfive.twentyfivedb.fidelity.service;

import com.twentyfive.twentyfivedb.fidelity.repository.CardGroupRepository;
import com.twentyfive.twentyfivedb.fidelity.repository.CardRepository;
import com.twentyfive.twentyfivemodel.filterTicket.AutoCompleteRes;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.dto.fidelityDto.FilterCardGroupRequest;
import twentyfive.twentyfiveadapter.models.fidelityModels.Card;
import twentyfive.twentyfiveadapter.models.fidelityModels.CardGroup;

import java.util.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

@Slf4j
@Service
public class CardGroupService {

    private final CardGroupRepository cardGroupRepository;
    private final MongoTemplate mongoTemplate;
    private final CardRepository cardRepository;
    private static final String USER_KEY = "ownerId";

    public CardGroupService(CardGroupRepository cardGroupRepository, MongoTemplate mongoTemplate, CardRepository cardRepository) {
        this.cardGroupRepository = cardGroupRepository;
        this.mongoTemplate = mongoTemplate;
        this.cardRepository = cardRepository;
    }

    public CardGroup getCardGroup(String id) {
        return cardGroupRepository.findById(id).orElse(null);
    }

    public List<CardGroup> findAllByOwnerId(String ownerId){
        return cardGroupRepository.findAllListByOwnerId(ownerId);
    }

    public CardGroup createCardGroup(CardGroup cardGroup) {
        cardGroup.setExpirationDate(cardGroup.getExpirationDate().atZone(ZoneId.systemDefault())
                .withZoneSameInstant(ZoneOffset.of("+02:00")).toLocalDateTime().toLocalDate().atStartOfDay());
        this.checkExpirationDate(cardGroup);
        return this.cardGroupRepository.save(cardGroup);
    }

    public long numberCards(String groupId){
        Query query = new Query();
        query.addCriteria(Criteria.where("cardGroupId").is(groupId));
        return mongoTemplate.count(query, Card.class);
    }

    public void deleteCardGroup(String id) {
        List<Card> list = cardRepository.findAllByCardGroupId(id);
        cardRepository.deleteAll(list);
        this.cardGroupRepository.deleteById(id);
    }

    public void updateCardGroup(String id, CardGroup cardGroup) {
        if (cardGroup == null) {
            log.error("Group is null");
            throw new IllegalArgumentException("Group is null");
        }

        if (StringUtils.isBlank(id)) {
            log.error("Group is null or empty");
            throw new IllegalArgumentException("Group is null or empty");
        }

        CardGroup cardGroup1 = cardGroupRepository.findById(id).orElse(null);

        if (cardGroup1 == null) {
            this.createCardGroup(cardGroup);
        } else {
            cardGroup1.setName(cardGroup.getName());
            cardGroup1.setDescription(cardGroup.getDescription());
            cardGroup1.setCreationDate(cardGroup.getCreationDate());
            cardGroup1.setExpirationDate(cardGroup.getExpirationDate());
            cardGroup1.setScanNumber(cardGroup.getScanNumber());
            cardGroup1.setNumberOfDaysForPrize(cardGroup.getNumberOfDaysForPrize());
            cardGroup1.setIsActive(cardGroup.getIsActive());
            cardGroupRepository.save(cardGroup1);
        }
    }

    public void updateStatus(String id, Boolean status){
        if (StringUtils.isBlank(id)) {
            log.error("Id cannot be null or empty");
            throw new IllegalArgumentException("Id cannot be null or empty");
        }

        CardGroup cardGroup = cardGroupRepository.findById(id).orElse(null);
        if(cardGroup != null){
            cardGroup.setIsActive(status);
            cardGroupRepository.save(cardGroup);
        }
    }

    public void disableExpiredGroups(List<CardGroup> groups){
        LocalDate currentDate = LocalDate.now();

        for(CardGroup cardGroup: groups){
            if(cardGroup.getExpirationDate().isBefore(currentDate.atStartOfDay())){
                cardGroup.setIsActive(false);
                cardGroupRepository.save(cardGroup);
            }
        }
    }

    public void checkExpirationDate(CardGroup group){
        LocalDate currentDate = LocalDate.now();
        if(group.getExpirationDate().isBefore(currentDate.atStartOfDay())){
            log.error("Impossible create group with this date");
            throw new RuntimeException("Impossible create group with this date");
        }
    }

    public Page<CardGroup> getGroupByName(String name, int page, int size){
        Pageable pageable= PageRequest.of(page, size);
        return cardGroupRepository.findAllByNameIgnoreCase(name, pageable);
    }

    /* TODO metodi aggiunta criteri per filtraggio*/
    public Page<CardGroup> getCardGroupFiltered(FilterCardGroupRequest filterObject, String ownerId, int page, int size) {
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where(USER_KEY).is(ownerId));
        criteriaList.addAll(parseOtherFilters(filterObject));
        return this.pageMethod(criteriaList, page, size);
    }

    public Page<CardGroup> pageGroups(String ownerId, int page, int size) {
        Pageable pageable= PageRequest.of(page, size);
        return cardGroupRepository.findAllByOwnerId(ownerId, pageable);
    }

    private List<Criteria> parseOtherFilters(FilterCardGroupRequest filterObject){
        List<Criteria> criteriaList = new ArrayList<>();
        if(filterObject == null){
            return criteriaList;
        }
        if(filterObject.isActive()){
            criteriaList.add(Criteria.where("isActive").is(filterObject.isActive()));
        }
        if(filterObject.getFromDate() != null && filterObject.getToDate() != null){
            criteriaList.add(Criteria.where("expirationDate").gte(filterObject.getFromDate()).lte(filterObject.getToDate()));
        }
        if(filterObject.getFromDate() != null && filterObject.getToDate() == null){
            criteriaList.add(Criteria.where("expirationDate").gte(filterObject.getFromDate()));
        }
        if(filterObject.getToDate() != null && filterObject.getFromDate() == null){
            criteriaList.add(Criteria.where("expirationDate").lte(filterObject.getToDate()));
        }
        return criteriaList;
    }

    private Page<CardGroup> pageMethod(List<Criteria> criteriaList, int page, int size) {

        Query query = new Query().addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));

        long total = mongoTemplate.count(query, CardGroup.class);

        Pageable pageable= PageRequest.of(page, size);
        query.with(pageable);

        List<CardGroup> cardGroups = mongoTemplate.find(query, CardGroup.class);
        this.disableExpiredGroups(cardGroups);
        return new PageImpl<>(cardGroups, pageable, total);
    }

    public Set<AutoCompleteRes> filterSearch(String find, String ownerId) {
        Set<CardGroup> groups = cardGroupRepository.findAllByOwnerIdAndNameContainingIgnoreCase(ownerId, find);
        //Set<CardGroup> groupsDescription = cardGroupRepository.findByOwnerIdAndDescriptionContainingIgnoreCase(ownerId, find);

        Set<AutoCompleteRes> setCombinato = new HashSet<>();
        for (CardGroup group : groups) {
            AutoCompleteRes temp = new AutoCompleteRes(group.getName());
            setCombinato.add(temp);
        }
        /*for (CardGroup groupD : groupsDescription) {
            AutoCompleteRes temp = new AutoCompleteRes(groupD.getDescription());
            setCombinato.add(temp);
        }

         */
        return setCombinato;
    }

    public boolean checkSameName(String name) {
        return cardGroupRepository.findAllByNameIgnoreCase(name, PageRequest.of(0, 1)).hasContent();
    }

    public Long countGroups(String ownerId) {
        return (long) cardGroupRepository.findAllListByOwnerId(ownerId).size();
    }
}
