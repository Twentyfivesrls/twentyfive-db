package com.twentyfive.twentyfivedb.fidelity.service;

import com.twentyfive.twentyfivedb.fidelity.repository.CardGroupRepository;
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
import twentyfive.twentyfiveadapter.adapter.Document.FidelityDocumentDB.CardGroup;

import java.util.ArrayList;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class CardGroupService {

    private final CardGroupRepository cardGroupRepository;
    private final MongoTemplate mongoTemplate;
    private static final String USER_KEY = "ownerId";

    public CardGroupService(CardGroupRepository cardGroupRepository, MongoTemplate mongoTemplate) {
        this.cardGroupRepository = cardGroupRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public CardGroup getCardGroup(String id) {
        return cardGroupRepository.findById(id).orElse(null);
    }

    public CardGroup createCardGroup(CardGroup cardGroup) {
        cardGroup.setExpirationDate(cardGroup.getExpirationDate().atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.of("+02:00")).toLocalDateTime());
        this.checkExpirationDate(cardGroup);
        return this.cardGroupRepository.save(cardGroup);
    }

    public void deleteCardGroup(String id) {

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
    public Page<CardGroup> getCardGroupFiltered(CardGroup filterObject, String ownerId, int page, int size) {
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where(USER_KEY).is(ownerId));
        criteriaList.addAll(parseOtherFilters(filterObject));
        return this.pageMethod(criteriaList, page, size);
    }

    public Page<CardGroup> pageGroups(int page, int size, String userId) {
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where(USER_KEY).is(userId));
        return this.pageMethod(criteriaList, page, size);
    }

    private List<Criteria> parseOtherFilters(CardGroup filterObject){
        List<Criteria> criteriaList = new ArrayList<>();
        if(filterObject == null){
            return criteriaList;
        }
        if(StringUtils.isNotBlank(filterObject.getName())){
            criteriaList.add(Criteria.where("name").regex(filterObject.getName(), "i"));
        }
        if(StringUtils.isNotBlank(filterObject.getDescription())){
            criteriaList.add(Criteria.where("description").regex(filterObject.getDescription(), "i"));
        }
        if(filterObject.getIsActive() != null){
            criteriaList.add(Criteria.where("isActive").is(filterObject.getIsActive()));
        }
        if(filterObject.getExpirationDate() != null){
            criteriaList.add(Criteria.where("expirationDate").is(filterObject.getExpirationDate()));
        }
        return criteriaList;
    }

    private Page<CardGroup> pageMethod(List<Criteria> criteriaList, int page, int size) {
        Pageable pageable= PageRequest.of(page, size);

        Query query = new Query().addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        query.with(pageable);

        long total = mongoTemplate.count(query, CardGroup.class);

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
}
