package com.twentyfive.twentyfivedb.ticketDB.service;


import com.twentyfive.twentyfivedb.ticketDB.repository.TicketRepository;
import com.twentyfive.twentyfivedb.ticketDB.utils.MethodUtils;
import com.twentyfive.twentyfivemodel.filterTicket.AutoCompleteRes;
import com.twentyfive.twentyfivemodel.models.ticketModels.Ticket;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.adapter.Document.TicketObjDocumentDB.AddressBookDocumentDB;
import twentyfive.twentyfiveadapter.adapter.Document.TicketObjDocumentDB.TicketDocumentDB;
import twentyfive.twentyfiveadapter.adapter.Mapper.TwentyFiveMapper;

import java.time.LocalDateTime;
import java.util.*;


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




    public void saveTicket(Ticket ticket, String id, String firstName, String lastName, String email, String username) {
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


        AddressBookDocumentDB addressBookControll = TwentyFiveMapper.INSTANCE.addressBookToAddressBookDocumentDB(addressBookService.getAddressBookById(id));
        AddressBookDocumentDB addressBook = new AddressBookDocumentDB();

        if(addressBookControll == null) {
            addressBook.setFirstName(firstName);
            addressBook.setLastName(lastName);
            addressBook.setEmail(email);
            addressBook.setUserId(username);
            addressBookService.saveAddressBook(addressBook);
        }
        else{
            addressBook = addressBookControll;
        }

        System.out.println("ADDRESSBOOK :" + addressBook);
        UUID uuid = UUID.randomUUID();
        TicketDocumentDB finalTicket = new TicketDocumentDB();
        finalTicket.setEventName(ticket.getEventName());
        finalTicket.setEventId(ticket.getEventId());
        finalTicket.setCode(uuid.toString());
        finalTicket.setDateStart(ticket.getDateStart());
        finalTicket.setDateEnd(ticket.getDateEnd());
        finalTicket.setUsed(ticket.getUsed());
        finalTicket.setActive(ticket.getActive());
        finalTicket.setAddressBookId(addressBook.getId());
        finalTicket.setEmail(addressBook.getEmail());
        finalTicket.setUserId(ticket.getUserId());
        finalTicket.setUrl("http://80.211.123.141:5557/dettaglio-ticket/"+uuid.toString());

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

    /*public List<TicketDocumentDB> ticketsSearch(Ticket filterObject, String userId) {

        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where("userId").is(userId));



        if (StringUtils.isNotBlank(filterObject.getEventName())) {
            Pattern namePattern = Pattern.compile(filterObject.getEventName(), Pattern.CASE_INSENSITIVE);
            criteriaList.add(Criteria.where("eventName").regex(namePattern));
        }

        if (StringUtils.isNotBlank(filterObject.getEmail())) {
            Pattern namePattern = Pattern.compile(filterObject.getEmail(), Pattern.CASE_INSENSITIVE);
            criteriaList.add(Criteria.where("email").regex(namePattern));
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

    }*/

    public Set<AutoCompleteRes> filterSearch(String userId, String email){
        Set<TicketDocumentDB> temp= ticketRepository.findByUserIdAndEmailContainingIgnoreCase(userId,email);
        Set<AutoCompleteRes> result= new HashSet<>();
        for (TicketDocumentDB ticket: temp){
            AutoCompleteRes autoComplete = new AutoCompleteRes(ticket.getEmail());
            result.add(autoComplete);
        }
        return result;
    }


    public Ticket updateTicketValidity(String id, Boolean status) {
        if (StringUtils.isBlank(id)) {
            log.error("Id cannot be null or empty");
            throw new IllegalArgumentException("Id cannot be null or empty");
        }
        TicketDocumentDB ticket = ticketRepository.findById(id).orElse(null);
        if (ticket != null) {
            ticket.setActive(status);
            ticketRepository.save(ticket);
            return TwentyFiveMapper.INSTANCE.ticketDocumentDBToTicket(ticket);
        }

        return null;
    }


    public void deleteTicket(String id) {
        if (StringUtils.isBlank(id)) {
            log.error("Id cannot be null or empty");
            throw new IllegalArgumentException("Id cannot be null or empty");
        }
        TicketDocumentDB ticketDocumentDB =  ticketRepository.findById(id).orElse(null);
        if(ticketDocumentDB != null){
            ticketRepository.deleteById(id);
        }

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

   public TicketDocumentDB findByCode(String code){

        return ticketRepository.findByCode(code);
   }

   public List<Ticket> getTicketsByIdEvent(String id, String username){



       List<TicketDocumentDB> documentList = ticketRepository.findByEventId(id);


        List<Ticket> list = new ArrayList<>();
        for (TicketDocumentDB ticketDocumentDB : documentList){
            list.add(TwentyFiveMapper.INSTANCE.ticketDocumentDBToTicket(ticketDocumentDB));
        }
        return list;
   }
    public Page<Ticket> getTicketFiltered(Ticket filterObject, String userId, int page, int dimension) {
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where("userId").is(userId));
        if (StringUtils.isNotBlank(filterObject.getEventId())) {
            Pattern namePattern = Pattern.compile(filterObject.getEventId(), Pattern.CASE_INSENSITIVE);
            criteriaList.add(Criteria.where("eventId").regex(namePattern));
        }
        if (StringUtils.isNotBlank(filterObject.getEventName())) {
            Pattern namePattern = Pattern.compile(filterObject.getEventName(), Pattern.CASE_INSENSITIVE);
            criteriaList.add(Criteria.where("eventName").regex(namePattern));
        }

        if (StringUtils.isNotBlank(filterObject.getEmail())) {
            Pattern namePattern = Pattern.compile(filterObject.getEmail(), Pattern.CASE_INSENSITIVE);
            criteriaList.add(Criteria.where("email").regex(namePattern));
        }

        //date range
        if (filterObject.getDateStart() != null && filterObject.getDateEnd() != null) {
            Criteria dateCriteria1 = Criteria.where("dateStart").gte(filterObject.getDateStart()).lte(filterObject.getDateEnd());
            criteriaList.add(dateCriteria1);
            Criteria dateCriteria2 = Criteria.where("dateEnd").gte(filterObject.getDateStart()).lte(filterObject.getDateEnd());
            criteriaList.add(dateCriteria2);
        }
        //date start
        if (filterObject.getDateStart() != null && filterObject.getDateEnd() == null) {
            criteriaList.add(Criteria.where("dateStart").gte(filterObject.getDateStart()));
        }
        //date end
        if (filterObject.getDateStart() == null && filterObject.getDateEnd() != null) {
            criteriaList.add(Criteria.where("dateEnd").lte(filterObject.getDateEnd()));
        }

        Query query = new Query();
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[criteriaList.size()])));
        }
        List<TicketDocumentDB> ticketDocumentDBList =mongoTemplate.find(query, TicketDocumentDB.class);
        List<Ticket> tickets = new ArrayList<>();
        for (TicketDocumentDB ticketDocumentDB : ticketDocumentDBList){
            tickets.add(TwentyFiveMapper.INSTANCE.ticketDocumentDBToTicket(ticketDocumentDB));
        }
        Pageable pageable=PageRequest.of(page,dimension);
        return MethodUtils.convertListToPage(tickets, pageable);
    }
}
