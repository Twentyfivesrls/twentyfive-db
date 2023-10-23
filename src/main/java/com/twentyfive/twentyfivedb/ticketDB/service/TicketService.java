package com.twentyfive.twentyfivedb.ticketDB.service;



import com.twentyfive.twentyfivedb.ticketDB.repository.TicketRepository;
import com.twentyfive.twentyfivemodel.filterTicket.TicketFilter;
import com.twentyfive.twentyfivemodel.models.ticketModels.Ticket;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.adapter.Document.TicketObjDocumentDB.AddressBookDocumentDB;
import twentyfive.twentyfiveadapter.adapter.Document.TicketObjDocumentDB.TicketDocumentDB;
import twentyfive.twentyfiveadapter.adapter.Mapper.TwentyFiveMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final AddressBookService addressBookService;
    private final MongoTemplate mongoTemplate;

    public TicketService(TicketRepository ticketRepository, AddressBookService addressBookService, MongoTemplate mongoTemplate) {
        this.addressBookService = addressBookService;
        this.mongoTemplate = mongoTemplate;
        this.ticketRepository = ticketRepository;
    }




    public void saveTicket(Ticket ticket, String firstName, String lastName, String email, String username) {
        if (ticket == null) {
            log.error("Ticket cannot be null");
            throw new IllegalArgumentException("Ticket cannot be null");
        }
        if (StringUtils.isBlank(firstName)) {
            log.error("First name cannot be null or empty");
            throw new IllegalArgumentException("First name cannot be null or empty");
        }
        if (StringUtils.isBlank(lastName)) {
            log.error("Last name cannot be null or empty");
            throw new IllegalArgumentException("Last name cannot be null or empty");
        }
        if (email == null) {
            log.error("Date of birth cannot be null");
            throw new IllegalArgumentException("Date of birth cannot be null");
        }


        AddressBookDocumentDB addressBookControll = addressBookService.findByEmail(email);
        AddressBookDocumentDB addressBook = new AddressBookDocumentDB();

        if(addressBookControll == null) {
            addressBook.setFirstName(firstName);
            addressBook.setLastName(lastName);
            addressBook.setEmail(email);
            addressBook.setUserId(username);
            addressBookService.saveAddressBook(TwentyFiveMapper.INSTANCE.addressBookDocumentDBToAddressBook(addressBook));
        }
        else{
            addressBook = addressBookControll;
        }



        TicketDocumentDB finalTicket = new TicketDocumentDB();
        finalTicket.setEventName(ticket.getEventName());
        finalTicket.setCode(ticket.getCode());
        finalTicket.setEventDateStart(ticket.getEventDateStart());
        finalTicket.setEventDateEnd(ticket.getEventDateEnd());
        finalTicket.setUsed(ticket.getUsed());
        finalTicket.setActive(ticket.getActive());
        finalTicket.setAddressBookId(addressBook.getEmail());
        finalTicket.setUserId(ticket.getUserId());


        ticketRepository.save(finalTicket);
    }


    public TicketDocumentDB getTicketById(String id) {
        if (StringUtils.isBlank(id)) {
            log.error("Id cannot be null or empty");
            throw new IllegalArgumentException("Id cannot be null or empty");
        }
        return ticketRepository.findById(id).orElse(null);
    }


    /*
     * Search for tickets by event name, event date start, event date end
     */

    public List<TicketDocumentDB> ticketsSearch(Ticket filterObject, String userId) {

        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where("userId").is(userId));



        if (StringUtils.isNotBlank(filterObject.getEventName())) {
            Pattern namePattern = Pattern.compile(filterObject.getEventName(), Pattern.CASE_INSENSITIVE);
            criteriaList.add(Criteria.where("eventName").regex(namePattern));
        }

        //date range
        if (filterObject.getEventDateStart() != null && filterObject.getEventDateEnd() != null) {
            Criteria dateCriteria1 = Criteria.where("eventDateStart").gte(filterObject.getEventDateStart()).lte(filterObject.getEventDateEnd());
            criteriaList.add(dateCriteria1);
            Criteria dateCriteria2 = Criteria.where("eventDateEnd").gte(filterObject.getEventDateStart()).lte(filterObject.getEventDateEnd());
            criteriaList.add(dateCriteria2);
        }
        //date start
        if (filterObject.getEventDateStart() != null && filterObject.getEventDateEnd() == null) {
            criteriaList.add(Criteria.where("eventDateStart").gte(filterObject.getEventDateStart()));
        }
        //date end
        if (filterObject.getEventDateStart() != null && filterObject.getEventDateEnd() != null) {
            criteriaList.add(Criteria.where("eventDateEnd").lte(filterObject.getEventDateEnd()));
        }

        Query query = new Query();
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[criteriaList.size()])));
        }
        return mongoTemplate.find(query, TicketDocumentDB.class);

    }


    public void pdateTicketValidity(String id, Boolean status) {
        if (StringUtils.isBlank(id)) {
            log.error("Id cannot be null or empty");
            throw new IllegalArgumentException("Id cannot be null or empty");
        }
        TicketDocumentDB ticket = ticketRepository.findById(id).orElse(null);
        if (ticket != null) {
            ticket.setActive(status);
            ticketRepository.save(ticket);
        }
    }


    public void deleteTicket(String id) {
        if (StringUtils.isBlank(id)) {
            log.error("Id cannot be null or empty");
            throw new IllegalArgumentException("Id cannot be null or empty");
        }
        ticketRepository.findById(id).ifPresent(ticketRepository::delete);
    }


    public void updateUsedTicket(String id, Boolean status) {
        if (StringUtils.isBlank(id)) {
            log.error("Id cannot be null or empty");
            throw new IllegalArgumentException("Id cannot be null or empty");
        }
        TicketDocumentDB ticket = ticketRepository.findById(id).orElse(null);
        if (ticket != null) {
            ticket.setUsed(status);
            ticketRepository.save(ticket);
        }
    }


    public List<TicketDocumentDB> getTicketsByEventName(String eventName) {
        if (StringUtils.isBlank(eventName)) {
            log.error("Event name cannot be null or empty");
            throw new IllegalArgumentException("Event name cannot be null or empty");
        }
        return ticketRepository.findByEventName(eventName);
    }


    public List<TicketDocumentDB> getTicketsByIsUsed(Boolean status) {
        if (status == null) {
            log.error("Status cannot be null");
            throw new IllegalArgumentException("Status cannot be null");
        }
        return ticketRepository.findByUsed(status);
    }


    public List<TicketDocumentDB> findAll() {
        return ticketRepository.findAll();
    }

    public List<TicketDocumentDB> findAllByUserId(String username) {
        return ticketRepository.findAllByUserId(username);
    }

    }
