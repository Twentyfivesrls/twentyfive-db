package com.twentyfive.twentyfivedb.ticketDB.controller;


import com.twentyfive.twentyfivedb.ticketDB.service.EventService;
import com.twentyfive.twentyfivedb.ticketDB.service.ExcelExportService;
import com.twentyfive.twentyfivemodel.filterTicket.AutoCompleteRes;
import com.twentyfive.twentyfivemodel.models.ticketModels.Event;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twentyfive.twentyfiveadapter.adapter.Document.TicketObjDocumentDB.EventDocumentDB;
import twentyfive.twentyfiveadapter.adapter.Mapper.TwentyFiveMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/event")
public class EventController {
    private final EventService eventService;
    private final ExcelExportService exportService;

    public EventController(EventService eventService, ExcelExportService exportService) {
        this.eventService = eventService;
        this.exportService = exportService;
    }

    @PostMapping("/filter")
    public ResponseEntity<Page<Event>> getEventListPagination(@RequestBody Event filterObject,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "5") int size,
                                                              @RequestParam("username") String username) {
        return new ResponseEntity<>(eventService.getEventFiltered(filterObject, page, size, username), HttpStatus.OK);
    }

    @PostMapping("/page")
    public ResponseEntity<Page<Event>> getEventListPagination(@RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "5") int size,
                                                              @RequestParam("username") String username) {
        return new ResponseEntity<>(eventService.pageEvents(page, size, username), HttpStatus.OK);
    }

    @PostMapping("/filter/event/autocomplete")
    public ResponseEntity<Set<AutoCompleteRes>> getEventListAutocomplete(@RequestParam("username") String username, @RequestParam("filterObject") String filterObject) {
        return new ResponseEntity<>(eventService.filterSearch(filterObject, username), HttpStatus.OK);
    }

    @GetMapping("/list")
    public ResponseEntity<List<Event>> getEventList(@RequestParam("username") String username) {
        List<EventDocumentDB> eventList = eventService.findAllByUsername(username);
        List<Event> mapList = new ArrayList<>();
        for (EventDocumentDB eventDocumentDB : eventList) {
            mapList.add(TwentyFiveMapper.INSTANCE.eventDocumentDBToEvent(eventDocumentDB));
        }
        return ResponseEntity.ok(mapList);
    }

    @PostMapping("/save")
    public ResponseEntity<Event> saveEvent(@RequestBody Event event) {
        eventService.saveEvent(event);
        return ResponseEntity.ok(event);

    }

    @GetMapping("/get/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable String id) {
        Event event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }

    @GetMapping(value = "/export/excel/{userId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> downloadExcel(@PathVariable String userId) {
        byte[] excelData = exportService.eventExportToExcel(userId);
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String formattedDateTime = dateTime.format(formatter);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=Lista_Eventi_" + formattedDateTime + ".xlsx")
                .body(excelData);
    }

    @PutMapping("/update/{id}/{status}")
    public ResponseEntity<Event> updateEvenByStaust(@PathVariable String id, @PathVariable Boolean status) {

        eventService.disableEvent(id, status);
        return ResponseEntity.ok(eventService.getEventById(id));

    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Event> updateEvenByIdt(@PathVariable String id, @RequestBody Event event) {

        eventService.updateEvent(id, event);
        return ResponseEntity.ok(event);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Event> deleteEvent(@PathVariable String id) {
        eventService.delete(id);
        return ResponseEntity.ok().build();
    }

}
