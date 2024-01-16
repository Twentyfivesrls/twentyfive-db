package com.twentyfive.twentyfivedb.ticketDB.service;


import com.twentyfive.twentyfivedb.ticketDB.repository.EventRepository;
import com.twentyfive.twentyfivedb.ticketDB.utils.MethodUtils;
import com.twentyfive.twentyfivemodel.models.ticketModels.Event;
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
import twentyfive.twentyfiveadapter.adapter.Document.TicketObjDocumentDB.EventDocumentDB;
import twentyfive.twentyfiveadapter.adapter.Document.TicketObjDocumentDB.TicketDocumentDB;
import twentyfive.twentyfiveadapter.adapter.Mapper.TwentyFiveMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


@Slf4j
@Service
public class EventService {
    private final EventRepository eventRepository;
    private final MongoTemplate mongoTemplate;

    public EventService(EventRepository eventRepository, MongoTemplate mongoTemplate) {
        this.eventRepository = eventRepository;
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
        Event event = TwentyFiveMapper.INSTANCE.eventDocumentDBToEvent(eventDocumentDB);

        return event;
    }

    public List<EventDocumentDB> paginationEvent(Event filterObject, String userId) {
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where("userId").is(userId));


        if (StringUtils.isNotBlank(filterObject.getName())) {
            Pattern namePattern = Pattern.compile(filterObject.getName(), Pattern.CASE_INSENSITIVE);
            criteriaList.add(Criteria.where("name").regex(namePattern));
        }
        if (StringUtils.isNotBlank(filterObject.getDescription())) {
            Pattern descriptionPattern = Pattern.compile(filterObject.getDescription(), Pattern.CASE_INSENSITIVE);
            criteriaList.add(Criteria.where("description").regex(descriptionPattern));
        }
        if (StringUtils.isNotBlank(filterObject.getLocation())) {
            Pattern locationPattern = Pattern.compile(filterObject.getLocation(), Pattern.CASE_INSENSITIVE);
            criteriaList.add(Criteria.where("location").regex(locationPattern));
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
        if (filterObject.getDateStart() != null && filterObject.getDateEnd() != null) {
            criteriaList.add(Criteria.where("dateEnd").lte(filterObject.getDateEnd()));
        }

        Query query = new Query();
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[criteriaList.size()])));
            return mongoTemplate.find(query, EventDocumentDB.class);
        }
        return mongoTemplate.findAll(EventDocumentDB.class);

    }

    public Page<Event> filterSearch(String find, int page, int size,String username){
        List<EventDocumentDB> eventDocumentDBS = eventRepository.findByUserIdAndNameOrDescriptionContainingIgnoreCase(username, find, find);
        List<Event> events = new ArrayList<>();
        for (EventDocumentDB eventDocumentDB : eventDocumentDBS){
            events.add(TwentyFiveMapper.INSTANCE.eventDocumentDBToEvent(eventDocumentDB));
        }
        Pageable pageable=PageRequest.of(page,size);
        return MethodUtils.convertListToPage(events, pageable);
        /*Criteria criteriaUserId = Criteria.where("userId").is(userId);
        Criteria criteriaFilter = new Criteria();


        if (StringUtils.isNotBlank(filterObject)) {
            Pattern pattern = Pattern.compile(filterObject, Pattern.CASE_INSENSITIVE);
            criteriaFilter.orOperator(
                    Criteria.where("name").regex(pattern),
                    Criteria.where("description").regex(pattern)
            );
        }

        Criteria combinedCriteria = new Criteria().andOperator(criteriaUserId, criteriaFilter);

        Query query = new Query(combinedCriteria);
        return mongoTemplate.find(query, EventDocumentDB.class);*/

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


    public List<EventDocumentDB> findAll() {
        return eventRepository.findAll();
    }

    public List<EventDocumentDB> findAllByUsername(String username){
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

  /*  public EventDocumentDB  getEventByField(String name, String description, LocalDateTime dateStart, LocalDateTime dateEnd, String location, Boolean enabled) {
        return  eventRepository.findByNameAndDescriptionAndDateStartAndDateEndAndLocationAndEnabled(name, description, dateStart, dateEnd, location, enabled);
    }*/

    public void delete(String id) {
        eventRepository.deleteById(id);
    }

    public Page<Event> getEventFiltered(Event filterObject, int page, int dimension, String userId) {
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where("userId").is(userId));

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
        }

        Query query = new Query();
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        List<EventDocumentDB> eventsDB = mongoTemplate.find(query, EventDocumentDB.class);
        List<Event> events = new ArrayList<>();
        for (EventDocumentDB event : eventsDB) {
            events.add(TwentyFiveMapper.INSTANCE.eventDocumentDBToEvent(event));
        }

        Pageable pageable = PageRequest.of(page, dimension);
        return MethodUtils.convertListToPage(events, pageable);
    }
}
