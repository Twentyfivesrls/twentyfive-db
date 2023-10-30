package com.twentyfive.twentyfivedb.ticketDB.service;


import com.twentyfive.twentyfivedb.ticketDB.repository.EventRepository;
import com.twentyfive.twentyfivemodel.filterTicket.EventFilter;
import com.twentyfive.twentyfivemodel.models.ticketModels.Event;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.adapter.Document.TicketObjDocumentDB.EventDocumentDB;
import twentyfive.twentyfiveadapter.adapter.Mapper.TwentyFiveMapper;

import java.time.LocalDateTime;
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

    public List<EventDocumentDB> paginationEvent(EventFilter filterObject, String userId) {
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
        if (filterObject.getStartDate() != null) {
            criteriaList.add(Criteria.where("date").gte(filterObject.getStartDate()));

        }
        if (filterObject.getEndDate() != null) {
            criteriaList.add(Criteria.where("date").lte(filterObject.getEndDate()));
        }
        Query query = new Query();
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[criteriaList.size()])));
            return mongoTemplate.find(query, EventDocumentDB.class);
        }
        return mongoTemplate.findAll(EventDocumentDB.class);

    }

    public List<EventDocumentDB> filterSearch(String filterObject, String userId){
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where("userId").is(userId));
        Query query = new Query();
        List<EventDocumentDB> mapList = new ArrayList<>();

        if (StringUtils.isNotBlank(filterObject)) {
            Pattern namePattern = Pattern.compile(filterObject, Pattern.CASE_INSENSITIVE);
            criteriaList.add(Criteria.where("name").regex(namePattern));
            Pattern descriptionPattern = Pattern.compile(filterObject, Pattern.CASE_INSENSITIVE);
            criteriaList.add(Criteria.where("description").regex(descriptionPattern));
        }

        Query res = query.addCriteria(new Criteria().orOperator(criteriaList.toArray(new Criteria[criteriaList.size()])));
        return mongoTemplate.find(res, EventDocumentDB.class);

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
        eventToUpdate.setDate(event.getDate());
        eventToUpdate.setLocation(event.getLocation());

        return eventRepository.save(eventToUpdate);
    }

    public EventDocumentDB  getEventByField(String name, String description, LocalDateTime date, String location, Boolean enabled) {
        return  eventRepository.findByNameAndDescriptionAndDateAndLocationAndEnabled(name, description, date, location, enabled);
    }

    public void delete(String id) {
        eventRepository.deleteById(id);
    }

}
