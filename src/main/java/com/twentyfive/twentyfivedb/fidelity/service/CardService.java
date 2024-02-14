package com.twentyfive.twentyfivedb.fidelity.service;

import com.twentyfive.twentyfivedb.Utility;
import com.twentyfive.twentyfivedb.fidelity.repository.CardRepository;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.adapter.Document.FidelityDocumentDB.Card;

import java.util.Optional;

@Service
@Slf4j
public class CardService {

    private final CardRepository cardRepository;

    private final ContactService contactService;

    public CardService(CardRepository cardRepository, ContactService contactService) {
        this.cardRepository = cardRepository;
        this.contactService = contactService;
    }

    public Page<Card> getAllCard(int page, int size, String sortColumn, String sortDirection) {
        Pageable pageable = Utility.makePageableObj(sortDirection, sortColumn, page, size);
        return cardRepository.findAll(pageable);
    }

    public Card getCard(String id) {
        return cardRepository.findById(id).orElse(null);
    }

    public Card createCard(Card card) {
        Optional<Card> card1 = cardRepository.findById(card.getId());
        if(card1.isPresent()){
            throw new RuntimeException("Card already exist");
        }
        return this.cardRepository.save(card);
    }

    public void deleteCard(String id) {
        this.cardRepository.deleteById(id);
    }

    public void updateCard(String id, Card card) {
        if (card == null) {
            return;
        }

        if (StringUtils.isBlank(id)) {
            //TODO
            return;
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
            cardRepository.save(card1);
        }
    }

    public void updateActive(String id, Boolean status) {
        Card card1 = cardRepository.findById(id).orElse(null);
        card1.setActive(status);
        cardRepository.save(card1);
    }
}
