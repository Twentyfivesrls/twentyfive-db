package com.twentyfive.twentyfivedb.fidelity.service;

import com.twentyfive.twentyfivedb.fidelity.repository.CardRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.adapter.Document.FidelityDocumentDB.Card;

@Service
@Slf4j
public class CardService {

    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public Page<Card> getAllCard(String id, int page, int size, String sortColumn, String sortDirection){
        //TODO fix this
        Sort.Direction direction;
        if ("desc".equalsIgnoreCase(sortDirection)) {
            direction = Sort.Direction.DESC;
        } else {
            direction = Sort.Direction.ASC;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortColumn));
        return cardRepository.getAllById(id, pageable);
    }

    public Card getCard(String id){
        return cardRepository.findById(id).orElse(null);
    }

    public Card createCard(Card card){
        return this.cardRepository.save(card);
    }

    public void deleteCard(String id){
        this.cardRepository.deleteById(id);
    }

    public void updateCard(String id, Card card){
        Card card1 = cardRepository.findById(id).orElse(null);

        card1.setName(card.getName());
        card1.setSurname(card.getSurname());
        card1.setEmail(card.getEmail());
        card1.setPhoneNumber(card.getPhoneNumber());
        card1.setScanNumberExecuted(card.getScanNumberExecuted());
        card1.setCreationDate(card.getCreationDate());
        card1.setLastScanDate(card.getLastScanDate());

        cardRepository.save(card1);
    }

    public void updateActive(String id, Boolean status) {
        Card card1 = cardRepository.findById(id).orElse(null);
        card1.setActive(status);
        cardRepository.save(card1);
    }
}
