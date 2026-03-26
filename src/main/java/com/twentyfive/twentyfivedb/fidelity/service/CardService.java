package com.twentyfive.twentyfivedb.fidelity.service;

import com.twentyfive.twentyfivedb.Utility;
import com.twentyfive.twentyfivedb.fidelity.repository.CardGroupRepository;
import com.twentyfive.twentyfivedb.fidelity.repository.CardRepository;
import com.twentyfive.twentyfivedb.fidelity.repository.PrizeRepository;
import com.twentyfive.twentyfivemodel.filterTicket.AutoCompleteRes;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.dto.fidelityDto.FilterCardGroupRequest;
import twentyfive.twentyfiveadapter.models.fidelityModels.Card;
import twentyfive.twentyfiveadapter.models.fidelityModels.CardGroup;
import twentyfive.twentyfiveadapter.models.fidelityModels.Premio;

import java.util.*;

import static com.twentyfive.twentyfivedb.Utility.parseOtherFiltersForFidelityCard;

@Service
@Slf4j
public class CardService {
    private final CardRepository cardRepository;
    private final PrizeRepository prizeRepository;
    private final MongoTemplate mongoTemplate;
    private final CardGroupService cardGroupService;
    private final CardGroupRepository cardGroupRepository;
    private static final String USER_KEY = "ownerId";

    public CardService(CardRepository cardRepository, PrizeRepository prizeRepository, MongoTemplate mongoTemplate, CardGroupService cardGroupService, CardGroupRepository cardGroupRepository) {
        this.cardRepository = cardRepository;
        this.prizeRepository = prizeRepository;
        this.mongoTemplate = mongoTemplate;
        this.cardGroupService = cardGroupService;
        this.cardGroupRepository = cardGroupRepository;
    }

    public Card getCard(String id) {
        return cardRepository.findById(id).orElse(null);
    }

    public List<Card> findAllByOwnerId(String ownerId) {
        return cardRepository.findAllByOwnerId(ownerId);
    }

    public Page<Card> getByGroupId(String groupId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return cardRepository.findAllByCardGroupId(groupId, pageable);
    }

    public List<Card> findAllByGroupIdAndOwnerId(String groupId, String ownerId) {
        return cardRepository.findAllByCardGroupIdAndOwnerId(groupId, ownerId);
    }

    public Page<Card> getCardByName(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return cardRepository.findAllByNameIgnoreCase(name, pageable);
    }

    public Card scannerCard(String id) {
        if (StringUtils.isBlank(id)) {
            log.error("Card identifier is null or empty");
            throw new IllegalArgumentException("Card identifier cannot be null or empty");
        }

        String normalizedId = id.trim();
        Date currentDate = new Date();

        Card card = resolveCardForScan(normalizedId);
        CardGroup group = cardGroupRepository.findById(card.getCardGroupId())
                .orElseThrow(() -> new IllegalArgumentException("CardGroup not found with id: " + card.getCardGroupId()));

        if (!Boolean.TRUE.equals(card.getIsActive())) {
            throw new IllegalStateException("Unable to scan card: The card is not active");
        }

        cardGroupService.checkExpirationDate(group);

        if (card.getScanNumberExecuted() < group.getScanNumber()) {
            card.setScanNumberExecuted(card.getScanNumberExecuted() + 1);
            card.setLastScanDate(currentDate);
            cardRepository.save(card);

            if (card.getScanNumberExecuted() == group.getScanNumber()) {
                savePrizeForCompletedCard(card, currentDate);
            }
        }

        return card;
    }

    private Card resolveCardForScan(String identifier) {
        Optional<Card> cardOptional;

        if (identifier.length() == 6) {
            log.info("Resolving card by card code: {}", identifier);
            cardOptional = cardRepository.findByCardCode(identifier);
        } else if (identifier.length() > 6) {
            log.info("Resolving card by card id: {}", identifier);
            cardOptional = cardRepository.findById(identifier);
        } else {
            throw new IllegalArgumentException("Invalid card identifier format");
        }

        return cardOptional.orElseThrow(() -> new IllegalArgumentException("Card not found with identifier: " + identifier));
    }

    private void savePrizeForCompletedCard(Card card, Date completionDate) {
        Premio premio = new Premio();
        premio.setCardId(card.getId());
        premio.setCardComplete(completionDate);
        prizeRepository.save(premio);
    }

    public Card createCard(Card card) {
        if (card == null) {
            log.error("Card is null");
            throw new IllegalArgumentException("Card cannot be null");
        }

        CardGroup group = cardGroupRepository.findById(card.getCardGroupId())
                .orElseThrow(() -> new IllegalArgumentException("CardGroup not found with id: " + card.getCardGroupId()));

        cardGroupService.checkExpirationDate(group);

        this.addCodeToCard(card);
        return cardRepository.save(card);
    }

    private void addCodeToCard(Card card) {
        String code;
        do {
            code = Utility.generateCode();
        } while (cardRepository.findByCardCode(code).isPresent());
        card.setCardCode(code);
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
            card1.setType(card.getType());
            card1.setTournamentName(card.getTournamentName());
            card1.setVoucherAmount(card.getVoucherAmount());
            card1.setTournamentPosition(card.getTournamentPosition());
            cardRepository.save(card1);
        }
    }

    public void updateStatus(String id, Boolean status) {
        if (StringUtils.isBlank(id)) {
            log.error("Id cannot be null or empty");
            throw new IllegalArgumentException("Id cannot be null or empty");
        }

        Card card = cardRepository.findById(id).orElse(null);
        if (card != null) {
            card.setIsActive(status);
            cardRepository.save(card);
        }
    }

    public void resetScanExecuted(String id) {
        Optional<Card> optionalCard = cardRepository.findById(id);

        if (optionalCard.isPresent()) {
            Card card = optionalCard.get();
            card.setScanNumberExecuted(0);
            cardRepository.save(card);
        } else {
            throw new IllegalArgumentException("La carta con ID " + id + " non esiste");
        }
    }


    public Page<Card> pageCard(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return cardRepository.findAll(pageable);
    }

    public Set<AutoCompleteRes> filterSearch(String find, String ownerId) {
        //Set<Card> cards = cardRepository.findAllByNameContainingIgnoreCase(find);
        //Search by name or surname
        Set<Card> cards = cardRepository.findAllByOwnerIdAndNameContainingIgnoreCaseOrOwnerIdAndSurnameContainingIgnoreCase(ownerId, find,ownerId, find);
        Set<AutoCompleteRes> setCombinato = new HashSet<>();
        for (Card card : cards) {
            AutoCompleteRes temp = new AutoCompleteRes(card.getName() + " " + card.getSurname() + " - " + card.getEmail());
            setCombinato.add(temp);
        }
        return setCombinato;
    }

    public Page<Card> getCardFiltered(FilterCardGroupRequest filterObject, int page, int size, String ownerId) {
       if (filterObject.getName().equals(null) || filterObject.getName() != "") {
           String[] parts = filterObject.getName().split("-");
           String email = parts[1].trim();
           filterObject.setName(email);
       }

        List<AggregationOperation> totalPipeline = parseOtherFiltersForFidelityCard(filterObject, ownerId, false, 0, 0);
        long total = getTotalCount(totalPipeline);

        List<AggregationOperation> pagedPipeline = parseOtherFiltersForFidelityCard(filterObject, ownerId, true, page, size);
        Aggregation aggregation = Aggregation.newAggregation(pagedPipeline);
        List<Card> cards = mongoTemplate.aggregate(aggregation, "fidelity_card", Card.class).getMappedResults();

        Pageable pageable = PageRequest.of(page, size);
        return new PageImpl<>(cards, pageable, total);
    }


    private long getTotalCount(List<AggregationOperation> operations) {
        operations.add(Aggregation.group().count().as("total"));

        Aggregation countAggregation = Aggregation.newAggregation(operations);

        // Execute the count aggregation
        AggregationResults<Utility.CountResult> countResult = mongoTemplate.aggregate(countAggregation, "fidelity_card", Utility.CountResult.class);

        return countResult.getUniqueMappedResult() != null ? countResult.getUniqueMappedResult().getTotal() : 0;
    }


    public Optional<Card> findByEmailAndType(String email, String type) {
        return cardRepository.findByEmailAndType(email, type);
    }
}
