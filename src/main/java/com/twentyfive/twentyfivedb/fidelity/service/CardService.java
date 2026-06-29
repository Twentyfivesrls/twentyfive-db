package com.twentyfive.twentyfivedb.fidelity.service;

import com.twentyfive.twentyfivedb.Utility;
import com.twentyfive.twentyfivedb.fidelity.exceptions.InactiveCardGroup;
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

        if (!Boolean.TRUE.equals(group.getIsActive())) {
            throw new InactiveCardGroup("Unable to scan card: The card group is not active");
        }

        cardGroupService.checkExpirationDate(group);

        int target = group.getScanNumber();
        int before = card.getScanNumberExecuted();
        boolean isVoucher = "voucher".equals(card.getType());

        // Le card fidelity possono superare scanNumber; le altre restano limitate al traguardo
        if (!isVoucher || before < target) {
            int after = before + 1;
            card.setScanNumberExecuted(after);
            card.setLastScanDate(currentDate);
            cardRepository.save(card);

            // Un premio per ogni ciclo completato (a scanNumber, 2*scanNumber, ...)
            generatePrizesForCompletedCycles(card, before, after, target, currentDate);
        }

        return card;
    }

    /**
     * Genera un premio per ogni multiplo di {@code target} (la soglia del gruppo)
     * attraversato passando da {@code before} a {@code after}.
     * Es. target=100: a 100 un premio, a 200 un altro, ecc.
     */
    private void generatePrizesForCompletedCycles(Card card, int before, int after, int target, Date date) {
        if (target <= 0) {
            return;
        }
        int completedCycles = (after / target) - (before / target);
        for (int i = 0; i < completedCycles; i++) {
            savePrizeForCompletedCard(card, date);
        }
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
            if (card1.getType().equals("voucher")) {
                card1.setVoucherAmount(card.getVoucherAmount());
            } else {
                card1.setVoucherAmount(null);
            }
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
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("La carta con ID " + id + " non esiste"));
        CardGroup group = cardGroupRepository.findById(card.getCardGroupId())
                .orElseThrow(() -> new IllegalArgumentException("CardGroup not found with id: " + card.getCardGroupId()));

        int target = group.getScanNumber();
        int current = card.getScanNumberExecuted();
        // Se la raccolta è oltre il traguardo, scala un ciclo mantenendo il surplus; altrimenti azzera
        card.setScanNumberExecuted(current > target ? current - target : 0);
        cardRepository.save(card);
    }

    /**
     * Aggiunge più scansioni in una sola operazione: incrementa scanNumberExecuted
     * di {@code times} (può superare scanNumber) e genera un premio per ogni ciclo
     * (multiplo di scanNumber) completato — anche più di uno se l'aggiunta supera
     * più traguardi in una volta.
     */
    public Card addScans(String id, int times) {
        if (times <= 0) {
            throw new IllegalArgumentException("Il numero di scansioni da aggiungere deve essere positivo");
        }

        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("La carta con ID " + id + " non esiste"));
        CardGroup group = cardGroupRepository.findById(card.getCardGroupId())
                .orElseThrow(() -> new IllegalArgumentException("CardGroup not found with id: " + card.getCardGroupId()));

        int target = group.getScanNumber();
        int before = card.getScanNumberExecuted();
        int newExecuted = before + times;   // può superare scanNumber

        Date now = new Date();
        card.setScanNumberExecuted(newExecuted);
        card.setLastScanDate(now);
        cardRepository.save(card);

        // Un premio per ogni ciclo completato attraversando il/i traguardo/i
        generatePrizesForCompletedCycles(card, before, newExecuted, target, now);

        return card;
    }


    public Page<Card> pageCard(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return cardRepository.findAll(pageable);
    }

    public Set<AutoCompleteRes> filterSearch(String find, String ownerId) {
        // Ricerca per nome o cognome (parziale) + per codice card (parziale)
        Set<Card> cards = new HashSet<>();
        cards.addAll(cardRepository.findAllByOwnerIdAndNameContainingIgnoreCaseOrOwnerIdAndSurnameContainingIgnoreCase(ownerId, find, ownerId, find));
        cards.addAll(cardRepository.findAllByOwnerIdAndCardCodeContainingIgnoreCase(ownerId, find));
        Set<AutoCompleteRes> setCombinato = new HashSet<>();
        for (Card card : cards) {
            setCombinato.add(new AutoCompleteRes(buildAutocompleteLabel(card)));
        }
        return setCombinato;
    }

    /**
     * Costruisce l'etichetta del suggerimento autocomplete.
     * Il token finale (dopo " - ") è l'identificativo usato per filtrare:
     * l'email se presente, altrimenti il codice card (sempre presente e univoco).
     */
    private String buildAutocompleteLabel(Card card) {
        String name = card.getName() != null ? card.getName() : "";
        String surname = card.getSurname() != null ? card.getSurname() : "";
        String fullName = (name + " " + surname).trim();
        String token = (card.getEmail() != null && !card.getEmail().isBlank())
                ? card.getEmail()
                : card.getCardCode();
        return fullName.isEmpty() ? token : fullName + " - " + token;
    }

    public Page<Card> getCardFiltered(FilterCardGroupRequest filterObject, int page, int size, String ownerId) {
       // Dal suggerimento "Nome Cognome - <token>" estraggo il token finale (email o codice card)
       // su cui effettuare la ricerca parziale (vedi parseOtherFiltersForFidelityCard).
       if (filterObject.getSearchText() != null && !filterObject.getSearchText().isBlank()) {
           String term = filterObject.getSearchText().trim();
           int sep = term.lastIndexOf(" - ");
           if (sep >= 0) {
               term = term.substring(sep + 3).trim();
           }
           filterObject.setSearchText(term);
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


    public Optional<Card> findByEmailAndType(String email, String type, String cardGroupId) {
        return cardRepository.findByEmailAndTypeAndCardGroupId(email, type, cardGroupId);
    }

    public List<Card> findAllByCustomer(String customerId) {
        return cardRepository.findAllByCustomerId(customerId);
    }

    public Card findByCardCodeLike(String cardCode) {
        return cardRepository.findByCardCodeContainingIgnoreCase(cardCode);
    }
}
