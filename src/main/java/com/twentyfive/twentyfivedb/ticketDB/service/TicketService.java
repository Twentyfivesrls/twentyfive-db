package com.twentyfive.twentyfivedb.ticketDB.service;


import com.twentyfive.twentyfivedb.ticketDB.repository.TicketRepository;
import com.twentyfive.twentyfivedb.ticketDB.utils.MethodUtils;
import com.twentyfive.twentyfivemodel.filterTicket.AutoCompleteRes;
import com.twentyfive.twentyfivemodel.models.ticketModels.AddressBook;
import com.twentyfive.twentyfivemodel.models.ticketModels.Ticket;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    @Value("${ticketUrl}")
    private String ticketUrl;
    private final TicketRepository ticketRepository;
    private final AddressBookService addressBookService;
    private final MongoTemplate mongoTemplate;
    private static final String USER_KEY = "userId";

    public TicketDocumentDB saveTicket(Ticket ticket, AddressBook addressBook, String username) {
        AddressBookDocumentDB addressBookDB = TwentyFiveMapper.INSTANCE.addressBookToAddressBookDocumentDB(addressBook);
        boolean isPresent = false;
        List<AddressBookDocumentDB> addresses = addressBookService.findAllByUserId(username);
        for (AddressBookDocumentDB a : addresses) {
            if (MethodUtils.existedAddress(a, addressBookDB)) {
                isPresent = true;
                break;
            }
        }
        if (!isPresent) {
            addressBookDB.setId(null);
            addressBookDB.setUserId(username);
            addressBookDB = addressBookService.saveAddressBook(addressBookDB);
        } else {
            addressBookDB = addressBookService.findAddressBook(addressBookDB);
        }
        String code = UUID.randomUUID().toString();
        ticket.setCode(code);
        ticket.setAddressBookId(addressBookDB.getId());
        ticket.setEmail(addressBookDB.getEmail());
        ticket.setUrl(ticketUrl + code);
        ticket.setUserId(username);
        TicketDocumentDB ticketDocumentDB = TwentyFiveMapper.INSTANCE.ticketToTicketDocumentDB(ticket);
        return ticketRepository.save(ticketDocumentDB);

    }

    public TicketDocumentDB getTicketById(String id) {
        if (StringUtils.isBlank(id)) {
            log.error("Id cannot be null or empty");
            throw new IllegalArgumentException("Id cannot be null or empty");
        }
        return ticketRepository.findById(id).orElse(null);
    }

    public Set<AutoCompleteRes> filterSearch(String userId, String email) {
        Set<TicketDocumentDB> temp = ticketRepository.findByUserIdAndEmailContainingIgnoreCase(userId, email);
        Set<AutoCompleteRes> result = new HashSet<>();
        for (TicketDocumentDB ticket : temp) {
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
        ticketRepository.findById(id).ifPresent(ticketDocumentDB -> ticketRepository.deleteById(id));

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

    public List<TicketDocumentDB> findAllByUserId(String username) {
        return ticketRepository.findAllByUserId(username);
    }

    public TicketDocumentDB findByCode(String code) {
        return ticketRepository.findByCode(code);
    }

    public List<Ticket> getTicketsByIdEvent(String id, String username) {
        List<TicketDocumentDB> documentList = ticketRepository.findByEventId(id);

        List<Ticket> list = new ArrayList<>();
        for (TicketDocumentDB ticketDocumentDB : documentList) {
            list.add(TwentyFiveMapper.INSTANCE.ticketDocumentDBToTicket(ticketDocumentDB));
        }
        return list;
    }

    public Page<Ticket> getTicketFiltered(Ticket filterObject, String userId, int page, int size) {
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where(USER_KEY).is(userId));
        criteriaList.addAll(parseOtherFilters(filterObject));
        return this.pageEventMethod(criteriaList, page, size);
    }

    public Page<Ticket> pageTickets(String userId, int page, int size) {
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where(USER_KEY).is(userId));
        return this.pageEventMethod(criteriaList, page, size);
    }

    private List<Criteria> parseOtherFilters(Ticket filterObject) {
        List<Criteria> criteriaList = new ArrayList<>();
        if (filterObject == null) {
            return criteriaList;
        }

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
        return criteriaList;
    }

    private Page<Ticket> pageEventMethod(List<Criteria> criteriaList, int page, int size) {
        Query query = new Query();
        query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));

        List<TicketDocumentDB> ticketDocumentDBList = mongoTemplate.find(query, TicketDocumentDB.class);
        List<Ticket> tickets = new ArrayList<>();
        for (TicketDocumentDB ticketDocumentDB : ticketDocumentDBList) {
            tickets.add(TwentyFiveMapper.INSTANCE.ticketDocumentDBToTicket(ticketDocumentDB));
        }
        Pageable pageable = PageRequest.of(page, size);
        return MethodUtils.convertListToPage(tickets, pageable);
    }

}
