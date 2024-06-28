package com.twentyfive.twentyfivedb.fidelity.service;

import com.twentyfive.twentyfivedb.fidelity.repository.CardRepository;
import com.twentyfive.twentyfivedb.fidelity.repository.ContactRepository;
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
import twentyfive.twentyfiveadapter.dto.fidelityDto.ContactDto;
import twentyfive.twentyfiveadapter.models.fidelityModels.Card;
import twentyfive.twentyfiveadapter.models.fidelityModels.Contact;
import twentyfive.twentyfiveadapter.models.fidelityModels.Premio;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ContactService {

    private final ContactRepository contactRepository;

    private final CardRepository cardRepository;

    private final MongoTemplate mongoTemplate;

    private static final String USER_KEY = "ownerId";


    public ContactService(ContactRepository contactRepository, CardRepository cardRepository, MongoTemplate mongoTemplate) {
        this.contactRepository = contactRepository;
        this.cardRepository = cardRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public Page<Contact> pageContact(int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        return contactRepository.findAll(pageable);
    }

    public List<Contact> findAll(String ownerId){
        return contactRepository.findAllByOwnerId(ownerId);
    }

    public ContactDto getContact(String id) {
        Contact contact = contactRepository.findById(id).orElse(null);
        assert contact != null;
        return this.fillContact(contact);
    }

    public Page<Contact> getContactByName(String name, int page, int size){
        Pageable pageable= PageRequest.of(page, size);
        return contactRepository.findAllByNameIgnoreCase(name, pageable);
    }

    public Contact createContact(Contact contact) { return this.contactRepository.save(contact); }

    public void deleteContact(String id) {
        this.contactRepository.deleteById(id);
    }

    public void updateContact(String id, Contact contact) {
        if (contact == null) {
            return;
        }

        if (StringUtils.isBlank(id)) {
            //TODO
            return;
        }

        Contact contact1 = contactRepository.findById(id).orElse(null);

        if (contact1 == null) {
            this.createContact(contact);
        } else {
            contact1.setName(contact.getName());
            contact1.setSurname(contact.getSurname());
            contact1.setEmail(contact.getEmail());
            contact1.setPhoneNumber(contact.getPhoneNumber());
            contact1.setCreationDate(contact.getCreationDate());
            contactRepository.save(contact1);
        }
    }

    /* TODO metodi aggiunta criteri per filtraggio*/
    public Page<ContactDto> getContactFiltered(ContactDto filterObject, int page, int size, String ownerId) {
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where(USER_KEY).is(ownerId));
        criteriaList.addAll(parseOtherFilters(filterObject));
        return this.pageMethod(criteriaList, page, size);
    }

    private List<Criteria> parseOtherFilters(ContactDto filterObject){
        List<Criteria> criteriaList = new ArrayList<>();
        if(filterObject == null){
            return criteriaList;
        }
        if(StringUtils.isNotBlank(filterObject.getName())){
            criteriaList.add(Criteria.where("name").regex(filterObject.getName(), "i"));
        }
        return criteriaList;
    }

    private Page<ContactDto> pageMethod(List<Criteria> criteriaList, int page, int size) {
        Query query = new Query();
        if(CollectionUtils.isEmpty(criteriaList)){
            log.info("criteria empty");

        } else {
            query = new Query().addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        long total = mongoTemplate.count(query, Contact.class);
        Pageable pageable = PageRequest.of(page, size);
        query.with(pageable);

        List<Contact> contacts = mongoTemplate.find(query, Contact.class);
        List<ContactDto> contactsDto = new ArrayList<>();
        for (Contact contact : contacts) {
            contactsDto.add(this.fillContact(contact));
        }
        return new PageImpl<>(contactsDto, pageable, total);
    }

    public Set<AutoCompleteRes> filterSearch(String find){
        //Set<Contact> contacts = contactRepository.findAllByNameContainingIgnoreCase(find);
        //Set<Contact> contacts = contactRepository.findAllByNameContainingIgnoreCaseOrSurnameContainingIgnoreCase(find, find);
        Set<Contact> contacts = contactRepository.findAllByNameContainingIgnoreCaseOrSurnameContainingIgnoreCaseOrEmailContainingIgnoreCase(find, find,find);
        Set<AutoCompleteRes> setCombinato = new HashSet<>();
        for (Contact contact : contacts) {
            AutoCompleteRes temp = new AutoCompleteRes(contact.getName() + " " + contact.getSurname() + " - " + contact.getEmail());
            setCombinato.add(temp);
        }
        return setCombinato;
    }

    public Long countContacts(String ownerId) {
        return (long) contactRepository.findAllByOwnerId(ownerId).size();
    }

    public List<Premio> totalNumberPrizeCustomer(List<Card> cards) {
        List<String> cardIds = cards.stream().map(card -> card.getId()).collect(Collectors.toList());
        if(!CollectionUtils.isEmpty(cardIds)){
            Query queryPremio = new Query();
            queryPremio.addCriteria(Criteria.where("cardId").in(cardIds));
            return mongoTemplate.find(queryPremio, Premio.class);
        }
        return null;
    }

    public ContactDto fillContact(Contact contact){
        ContactDto contactDto = new ContactDto();
        contactDto.setId(contact.getId());
        contactDto.setName(contact.getName());
        contactDto.setSurname(contact.getSurname());
        contactDto.setEmail(contact.getEmail());
        contactDto.setPhoneNumber(contact.getPhoneNumber());
        contactDto.setCreationDate(contact.getCreationDate());
        List<Card> cards = cardRepository.findAllByCustomerId(contact.getId());
        List<Premio> prizes = this.totalNumberPrizeCustomer(cards);
        if(!CollectionUtils.isEmpty(prizes)) {
            for(Premio premio : prizes){
                if(premio.isClaimed()){
                    contactDto.setClaimedPrizes(contactDto.getClaimedPrizes() + 1);
                }else{
                    contactDto.setUnclaimedPrizes(contactDto.getUnclaimedPrizes() + 1);
                }
            }
        }
        int cardNumber = cards.size();
        contactDto.setAssociatedCards(cardNumber);
        return contactDto;
    }
}
