package com.twentyfive.twentyfivedb.fidelity.service;

import com.twentyfive.twentyfivedb.Utility;
import com.twentyfive.twentyfivedb.fidelity.repository.CardRepository;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.adapter.Document.FidelityDocumentDB.Card;
import twentyfive.twentyfiveadapter.adapter.Document.FidelityDocumentDB.CardGroup;

import java.time.LocalDate;

@Service
@Slf4j
public class CardService {

    private final CardRepository cardRepository;

    private final CardGroupService cardGroupService;

    public CardService(CardRepository cardRepository, CardGroupService cardGroupService) {
        this.cardRepository = cardRepository;
        this.cardGroupService = cardGroupService;
    }

    public Page<Card> getAllCard(int page, int size, String sortColumn, String sortDirection) {
        Pageable pageable = Utility.makePageableObj(sortDirection, sortColumn, page, size);

        Page<Card> cards = cardRepository.findAll(pageable);
        for(Card card: cards){
            String groupId = card.getCardGroupId();
            CardGroup group = cardGroupService.getCardGroup(groupId);
            if(!group.getIsActive()){
                card.setIsActive(false);
                cardRepository.save(card);
            }
        }
        return cards;
    }

    public Card getCard(String id) {
        return cardRepository.findById(id).orElse(null);
    }

    public Page<Card> getCardByName(String name, int page, int size){
        Pageable pageable= PageRequest.of(page, size);
        return cardRepository.findAllByNameIgnoreCase(name, pageable);
    }

    public Page<Card> getAllCardByStatus(int page, int size, String sortColumn, String sortDirection, Boolean status) {
        Pageable pageable = Utility.makePageableObj(sortDirection, sortColumn, page, size);
        return cardRepository.findAllByIsActive(status, pageable);
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

}