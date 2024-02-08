package com.twentyfive.twentyfivedb.fidelity.service;

import com.twentyfive.twentyfivedb.fidelity.repository.CardGroupRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.adapter.Document.FidelityDocumentDB.CardGroup;

@Slf4j
@Service
public class CardGroupService {

    private final CardGroupRepository cardGroupRepository;

    public CardGroupService(CardGroupRepository cardGroupRepository) {this.cardGroupRepository = cardGroupRepository;}

    public Page<CardGroup> getAllCardGroup(String ownerId, int page, int size, String sortColumn, String sortDirection){
        Sort.Direction direction;
        if ("desc".equalsIgnoreCase(sortDirection)) {
            direction = Sort.Direction.DESC;
        } else {
            direction = Sort.Direction.ASC;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortColumn));
        return cardGroupRepository.getAllByOwnerId(ownerId, pageable);
    }

    public CardGroup getCardGroup(String id){
        return cardGroupRepository.findById(id).orElse(null);
    }

    public CardGroup createCardGroup(CardGroup cardGroup){
        return this.cardGroupRepository.save(cardGroup);
    }

    public void deleteCardGroup(String id){
        this.cardGroupRepository.deleteById(id);
    }

    public void updateCardGroup(String id, CardGroup cardGroup){
        CardGroup cardGroup1 = cardGroupRepository.findById(id).orElse(null);

        cardGroup1.setName(cardGroup.getName());
        cardGroup1.setDescription(cardGroup.getDescription());
        cardGroup1.setCreationDate(cardGroup.getCreationDate());
        cardGroup1.setExpirationDate(cardGroup.getExpirationDate());
        cardGroup1.setScanNumber(cardGroup.getScanNumber());
        cardGroup1.setNumberOfDaysForPrize(cardGroup.getNumberOfDaysForPrize());

        cardGroupRepository.save(cardGroup1);
    }

    public void updateActive(String id, Boolean status) {
        CardGroup cardGroup1 = cardGroupRepository.findById(id).orElse(null);
        cardGroup1.setActive(status);
        cardGroupRepository.save(cardGroup1);
    }
}
