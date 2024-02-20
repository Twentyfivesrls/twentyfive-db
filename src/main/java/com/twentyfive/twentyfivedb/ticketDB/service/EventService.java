package com.twentyfive.twentyfivedb.ticketDB.service;

import com.twentyfive.twentyfivedb.ticketDB.repository.EventRepository;
import com.twentyfive.twentyfivedb.ticketDB.utils.MethodUtils;
import com.twentyfive.twentyfivemodel.filterTicket.AutoCompleteRes;
import com.twentyfive.twentyfivemodel.models.ticketModels.Event;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.adapter.Document.TicketObjDocumentDB.EventDocumentDB;
import twentyfive.twentyfiveadapter.adapter.Mapper.TwentyFiveMapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;


@Slf4j
@Service
public class EventService {
    private final EventRepository eventRepository;
    private final TicketService ticketService;
    private final MongoTemplate mongoTemplate;
    private static final String USER_KEY = "userId";

    public EventService(EventRepository eventRepository, TicketService ticketService, MongoTemplate mongoTemplate) {
        this.eventRepository = eventRepository;
        this.ticketService = ticketService;
        this.mongoTemplate = mongoTemplate;
    }

    public void saveEvent(Event event) {
        if (event == null) {
            log.error("Event is null");
            throw new IllegalArgumentException("Event is null");
        }
        eventRepository.save(TwentyFiveMapper.INSTANCE.eventToEventDocumentDB(event));
    }

    public Event getEventById(String id) {
        if (StringUtils.isBlank(id)) {
            log.error("Id is null or empty");
            throw new IllegalArgumentException("Id is null or empty");
        }

        EventDocumentDB eventDocumentDB = eventRepository.findById(id).orElse(null);
        return TwentyFiveMapper.INSTANCE.eventDocumentDBToEvent(eventDocumentDB);
    }

    public Set<AutoCompleteRes> filterSearch(String find, String username) {
        Set<EventDocumentDB> eventName = eventRepository.findByUserIdAndNameContainingIgnoreCase(username, find);
        Set<EventDocumentDB> eventDescription = eventRepository.findByUserIdAndDescriptionContainingIgnoreCase(username, find);

        Set<AutoCompleteRes> setCombinato = new HashSet<>();
        for (EventDocumentDB eventN : eventName) {
            AutoCompleteRes temp = new AutoCompleteRes(eventN.getName());
            setCombinato.add(temp);
        }
        for (EventDocumentDB eventD : eventDescription) {
            AutoCompleteRes temp = new AutoCompleteRes(eventD.getDescription());
            setCombinato.add(temp);
        }
        return setCombinato;
    }

    public void disableEvent(String id, Boolean isDisabled) {
        if (StringUtils.isBlank(id)) {
            log.error("Id is null or empty");
            throw new IllegalArgumentException("Id is null or empty");
        }

        EventDocumentDB event = eventRepository.findById(id).orElse(null);
        if (event == null) {
            log.error("Event not found");
            throw new IllegalArgumentException("Event not found");
        }
        event.setEnabled(isDisabled);
        eventRepository.save(event);
    }

    public List<EventDocumentDB> findAllByUsername(String username) {
        return eventRepository.findAllByUserId(username);
    }

    public EventDocumentDB updateEvent(String id, Event event) {
        if (StringUtils.isBlank(id)) {
            log.error("Id is null or empty");
            throw new IllegalArgumentException("Id is null or empty");
        }
        if (event == null) {
            log.error("Event is null");
            throw new IllegalArgumentException("Event is null");
        }

        EventDocumentDB eventToUpdate = eventRepository.findById(id).orElse(null);
        if (eventToUpdate == null) {
            log.error("Event not found");
            throw new IllegalArgumentException("Event not found");
        }

        eventToUpdate.setName(event.getName());
        eventToUpdate.setDescription(event.getDescription());
        eventToUpdate.setDateStart(event.getDateStart());
        eventToUpdate.setDateEnd(event.getDateEnd());
        eventToUpdate.setLocation(event.getLocation());

        return eventRepository.save(eventToUpdate);
    }

    public void delete(String id) {
        if (StringUtils.isBlank(id)) {
            log.error("Id is null or empty");
            throw new IllegalArgumentException("Id is null or empty");
        }
        ticketService.deleteTicket(id);
        eventRepository.deleteById(id);
    }

    public Page<Event> getEventFiltered(Event filterObject, int page, int size, String userId) {
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where(USER_KEY).is(userId));
        criteriaList.addAll(parseOtherFilters(filterObject));
        return this.pageEventMethod(criteriaList, page, size);
    }

    public Page<Event> pageEvents(int page, int size, String userId) {
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where(USER_KEY).is(userId));
        return this.pageEventMethod(criteriaList, page, size);
    }

    private List<Criteria> parseOtherFilters(Event filterObject) {
        List<Criteria> criteriaList = new ArrayList<>();
        if (filterObject == null) {
            return criteriaList;
        }
        if (StringUtils.isNotBlank(filterObject.getName()) || StringUtils.isNotBlank(filterObject.getDescription())) {
            List<Criteria> nameAndDescriptionCriteria = new ArrayList<>();

            if (StringUtils.isNotBlank(filterObject.getName())) {
                Pattern namePattern = Pattern.compile(filterObject.getName(), Pattern.CASE_INSENSITIVE);
                if (MethodUtils.isValidPattern(namePattern)) {
                    nameAndDescriptionCriteria.add(Criteria.where("name").regex(namePattern));
                }
            }

            if (StringUtils.isNotBlank(filterObject.getDescription())) {
                Pattern descriptionPattern = Pattern.compile(filterObject.getDescription(), Pattern.CASE_INSENSITIVE);
                if (MethodUtils.isValidPattern(descriptionPattern)) {
                    nameAndDescriptionCriteria.add(Criteria.where("description").regex(descriptionPattern));
                }
            }

            criteriaList.add(new Criteria().orOperator(nameAndDescriptionCriteria.toArray(new Criteria[0])));
        }

        if (StringUtils.isNotBlank(filterObject.getLocation())) {
            Pattern locationPattern = Pattern.compile(filterObject.getLocation(), Pattern.CASE_INSENSITIVE);
            if (MethodUtils.isValidPattern(locationPattern)) {
                criteriaList.add(Criteria.where("location").regex(locationPattern));
            }
        }

        if (filterObject.getDateStart() != null && filterObject.getDateEnd() != null) {
            Criteria criteriaWithinRange = new Criteria().orOperator(
                    Criteria.where("dateStart").gte(filterObject.getDateStart()).lte(filterObject.getDateEnd()),
                    Criteria.where("dateEnd").gte(filterObject.getDateStart()).lte(filterObject.getDateEnd()),
                    Criteria.where("dateStart").lte(filterObject.getDateStart()).and("dateEnd").gte(filterObject.getDateEnd())
            );
            criteriaList.add(criteriaWithinRange);
        } else {
            if (filterObject.getDateStart() != null) {
                criteriaList.add(Criteria.where("dateStart").gte(filterObject.getDateStart()));
            }
            if (filterObject.getDateEnd() != null) {
                criteriaList.add(Criteria.where("dateEnd").lte(filterObject.getDateEnd()));
            }
        }


        /*if (filterObject.getDateStart() != null && filterObject.getDateEnd() != null) {
            Criteria dateCriteria1 = Criteria.where("dateStart").gte(filterObject.getDateStart()).lte(filterObject.getDateEnd());
            Criteria dateCriteria2 = Criteria.where("dateEnd").gte(filterObject.getDateStart()).lte(filterObject.getDateEnd());
            criteriaList.add(new Criteria().andOperator(dateCriteria1, dateCriteria2));
        } else {
            if (filterObject.getDateStart() != null) {
                criteriaList.add(Criteria.where("dateStart").gte(filterObject.getDateStart()));
            }
            if (filterObject.getDateEnd() != null) {
                criteriaList.add(Criteria.where("dateEnd").lte(filterObject.getDateEnd()));
            }
        }*/
        return criteriaList;
    }

    private Page<Event> pageEventMethod(List<Criteria> criteriaList, int page, int size) {
        Query query = new Query();
        query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));

        List<EventDocumentDB> eventsDB = mongoTemplate.find(query, EventDocumentDB.class);
        List<Event> events = new ArrayList<>();
        for (EventDocumentDB event : eventsDB) {
            events.add(TwentyFiveMapper.INSTANCE.eventDocumentDBToEvent(event));
        }

        Pageable pageable = PageRequest.of(page, size);
        return MethodUtils.convertListToPage(events, pageable);
    }


}
