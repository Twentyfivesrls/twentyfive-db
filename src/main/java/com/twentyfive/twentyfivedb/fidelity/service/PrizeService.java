package com.twentyfive.twentyfivedb.fidelity.service;

import com.twentyfive.twentyfivedb.fidelity.repository.CardGroupRepository;
import com.twentyfive.twentyfivedb.fidelity.repository.CardRepository;
import com.twentyfive.twentyfivedb.fidelity.repository.PrizeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import twentyfive.twentyfiveadapter.adapter.Document.FidelityDocumentDB.Card;
import twentyfive.twentyfiveadapter.adapter.Document.FidelityDocumentDB.CardGroup;
import twentyfive.twentyfiveadapter.adapter.Document.FidelityDocumentDB.Premio;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PrizeService {

    private final PrizeRepository prizeRepository;
    private final CardGroupRepository groupRepository;
    private final CardRepository cardRepository;
    private final MongoTemplate mongoTemplate;
    private final CardService cardService;

    public PrizeService(PrizeRepository prizeRepository, CardGroupRepository groupRepository, CardRepository cardRepository, MongoTemplate mongoTemplate, CardService cardService) {
        this.prizeRepository = prizeRepository;
        this.groupRepository = groupRepository;
        this.cardRepository = cardRepository;
        this.mongoTemplate = mongoTemplate;
        this.cardService = cardService;
    }

    public List<Premio> totalNumberPrizeCard(String id){
        Query query = new Query();
        query.addCriteria(Criteria.where("cardId").is(id));
        return mongoTemplate.find(query, Premio.class);
    }

    public List<Premio> totalNumberPrizeCustomer(String id){
        Query query = new Query();
        query.addCriteria(Criteria.where("customerId").is(id));
        List<Card> list = mongoTemplate.find(query, Card.class);
        List<String> cardIds = list.stream().map(card -> card.getId()).collect(Collectors.toList());
        if(!CollectionUtils.isEmpty(cardIds)){
            Query queryPremio = new Query();
            queryPremio.addCriteria(Criteria.where("cardId").in(cardIds));
            return mongoTemplate.find(queryPremio, Premio.class);
        }
        return null;
    }

    public Page<Premio> pagePrizeCard(String id, int page, int size){
        Pageable pageable= PageRequest.of(page, size);
        return prizeRepository.findAllByCardIdIgnoreCase(id, pageable);
    }

    public Page<Premio> pagePrizeUser(String id, int page, int size){
        Query query = new Query();
        query.addCriteria(Criteria.where("customerId").is(id));
        List<Card> list = mongoTemplate.find(query, Card.class);
        List<String> cardIds = list.stream().map(card -> card.getId()).collect(Collectors.toList());
        if(!CollectionUtils.isEmpty(cardIds)){
            Pageable pageable= PageRequest.of(page, size);
            Query queryPremio = new Query().with(pageable);
            queryPremio.addCriteria(Criteria.where("cardId").in(cardIds));
            List<Premio> premioList =  mongoTemplate.find(queryPremio, Premio.class);
            return PageableExecutionUtils.getPage(
                    premioList,
                    pageable,
                    () -> mongoTemplate.count(Query.of(queryPremio).limit(-1).skip(-1), Premio.class));
        }
        return null;
    }

    public void claimPrize(String id){
        LocalDate currentDate = LocalDate.now();
        Premio premio = prizeRepository.findById(id).orElse(null);
        assert premio != null;
        Card card = cardService.getCard(premio.getCardId());
        CardGroup group = groupRepository.findById(card.getCardGroupId()).orElse(null);
        assert group != null;
        if(premio.getClaimDate() == null){
            premio.setClaimed(true);
            premio.setClaimDate(currentDate.atStartOfDay());
            prizeRepository.save(premio);
        }
        if(premio.getCardComplete().equals(currentDate.atStartOfDay()) && card.getScanNumberExecuted() == group.getScanNumber()){
            card.setScanNumberExecuted(0);
            cardRepository.save(card);
        }
    }

    public Premio claimLastPrize(String cardId) {
        LocalDate currentDate = LocalDate.now();
        Optional<Premio> ultimoPremioOptional = Optional.ofNullable(prizeRepository.findTopByCardIdOrderByCardCompleteDesc(cardId));

        if (ultimoPremioOptional.isPresent()) {
            Premio ultimoPremio = ultimoPremioOptional.get();
            ultimoPremio.setClaimed(true);
            ultimoPremio.setClaimDate(currentDate.atStartOfDay());
            return prizeRepository.save(ultimoPremio);
        } else {
            throw new RuntimeException("Nessun premio trovato per la carta con ID: " + cardId);
        }
    }
}
