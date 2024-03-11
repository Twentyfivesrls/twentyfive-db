package com.twentyfive.twentyfivedb.fidelity.service;

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
import org.springframework.util.CollectionUtils;
import twentyfive.twentyfiveadapter.adapter.Document.FidelityDocumentDB.Card;
import twentyfive.twentyfiveadapter.adapter.Document.FidelityDocumentDB.CardGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class CardService {

    private final CardRepository cardRepository;

    private final MongoTemplate mongoTemplate;

    private final CardGroupService cardGroupService;

    public CardService(CardRepository cardRepository, MongoTemplate mongoTemplate, CardGroupService cardGroupService) {
        this.cardRepository = cardRepository;
        this.mongoTemplate = mongoTemplate;
        this.cardGroupService = cardGroupService;
    }

    public Card getCard(String id) {
        return cardRepository.findById(id).orElse(null);
    }

    public List<Card> findAll(){
        return cardRepository.findAll();
    }

    public List<Card> getByGroupId(String groupId){
        return cardRepository.findAllByCardGroupId(groupId);
    }

    public Page<Card> getCardByName(String name, int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        return cardRepository.findAllByNameIgnoreCase(name, pageable);
    }

    public Card createCard(Card card) {
        String temp = card.getCardGroupId();
        CardGroup group = cardGroupService.getCardGroup(temp);
        try{
            cardGroupService.checkExpirationDate(group);
        } catch (Exception e){
            throw new RuntimeException("Unable to create card");
        }
        return this.cardRepository.save(card);
    }

    public void deleteCard(String id) {
        this.cardRepository.deleteById(id);
    }

    public void updateCard(String id, Card card) {
        if (card == null) {
            log.error("Card is null");
            throw new IllegalArgumentException("Card is null");
        }

        if (StringUtils.isBlank(id)) {
            log.error("Card is null or empty");
            throw new IllegalArgumentException("Card is null or empty");
        }

        Card card1 = cardRepository.findById(id).orElse(null);

        if (card1 == null) {
            //se la card non esiste allora la creo
            this.createCard(card);
        } else {
            //se la card esiste aggiorno
            card1.setName(card.getName());
            card1.setSurname(card.getSurname());
            card1.setEmail(card.getEmail());
            card1.setPhoneNumber(card.getPhoneNumber());
            card1.setScanNumberExecuted(card.getScanNumberExecuted());
            card1.setCreationDate(card.getCreationDate());
            card1.setLastScanDate(card.getLastScanDate());
            card1.setIsActive(card.getIsActive());
            cardRepository.save(card1);
        }
    }

    public void updateStatus(String id, Boolean status){
        if (StringUtils.isBlank(id)) {
            log.error("Id cannot be null or empty");
            throw new IllegalArgumentException("Id cannot be null or empty");
        }

        Card card = cardRepository.findById(id).orElse(null);
        if(card != null){
            card.setIsActive(status);
            cardRepository.save(card);
        }
    }


    /* TODO metodi aggiunta criteri per filtraggio*/
    public Page<Card> getCardFiltered(Card filterObject, int page, int size) {
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.addAll(parseOtherFilters(filterObject));
        return this.pageMethod(criteriaList, page, size);
    }

    public Page<Card> pageCard(int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        return cardRepository.findAll(pageable);
    }

    private List<Criteria> parseOtherFilters(Card filterObject){
        List<Criteria> criteriaList = new ArrayList<>();
        if(filterObject == null){
            return criteriaList;
        }
        if(StringUtils.isNotBlank(filterObject.getName())){
            criteriaList.add(Criteria.where("name").regex(filterObject.getName(), "i"));
        }
        if(StringUtils.isNotBlank(filterObject.getEmail())){
            criteriaList.add(Criteria.where("email").regex(filterObject.getEmail(), "i"));
        }
        if(filterObject.getIsActive() != null){
            criteriaList.add(Criteria.where("isActive").is(filterObject.getIsActive()));
        }
        /* TODO aggiungere la ricerca per data di scadenza */
        /*if(filterObject.getExpirationDate() != null){
            criteriaList.add(Criteria.where("expirationDate").is(filterObject.getExpirationDate()));
        }*/
        return criteriaList;
    }

    private Page<Card> pageMethod(List<Criteria> criteriaList, int page, int size) {
        Query query = new Query();
        if(CollectionUtils.isEmpty(criteriaList)){
           log.info("criteria empty");

        } else {
            query = new Query().addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        long total = mongoTemplate.count(query, Card.class);
        Pageable pageable = PageRequest.of(page, size);
        query.with(pageable);

        List<Card> cards = mongoTemplate.find(query, Card.class);
        this.disableStatusCard(cards);
        return new PageImpl<>(cards, pageable, total);
    }

    public Set<AutoCompleteRes> filterSearch(String find){
        Set<Card> cards = cardRepository.findAllByNameContainingIgnoreCase(find);
        Set<AutoCompleteRes> setCombinato = new HashSet<>();
        for (Card card : cards) {
            AutoCompleteRes temp = new AutoCompleteRes(card.getName());
            setCombinato.add(temp);
        }
        return setCombinato;
    }

    private void disableStatusCard(List<Card> cards){
        for(Card card: cards){
            String groupId = card.getCardGroupId();
            CardGroup group = cardGroupService.getCardGroup(groupId);
            if(!group.getIsActive()){
                card.setIsActive(false);
                cardRepository.save(card);
            }
        }
    }
}
