package com.twentyfive.twentyfivedb.fidelity.service;

import com.twentyfive.twentyfivedb.Utility;
import com.twentyfive.twentyfivedb.fidelity.repository.CardGroupRepository;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.adapter.Document.FidelityDocumentDB.CardGroup;

@Slf4j
@Service
public class CardGroupService {

    private final CardGroupRepository cardGroupRepository;

    public CardGroupService(CardGroupRepository cardGroupRepository) {
        this.cardGroupRepository = cardGroupRepository;
    }

    public Page<CardGroup> getAllCardGroup(String ownerId, int page, int size, String sortColumn, String sortDirection) {
        Pageable pageable = Utility.makePageableObj(sortDirection, sortColumn, page, size);
        return cardGroupRepository.getAllByOwnerId(ownerId, pageable);
    }

    public CardGroup getCardGroup(String id) {
        return cardGroupRepository.findById(id).orElse(null);
    }

    public CardGroup createCardGroup(CardGroup cardGroup) { return this.cardGroupRepository.save(cardGroup); }

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
}
