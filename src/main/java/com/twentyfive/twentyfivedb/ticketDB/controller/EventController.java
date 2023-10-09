package com.twentyfive.twentyfivedb.ticketDB.controller;


import com.twentyfive.twentyfivedb.ticketDB.service.EventService;
import com.twentyfive.twentyfivedb.ticketDB.service.ExcelExportService;
import com.twentyfive.twentyfivedb.ticketDB.utils.MethodUtils;


import com.twentyfive.twentyfivemodel.filterTicket.EventFilter;
import com.twentyfive.twentyfivemodel.models.ticketModels.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twentyfive.twentyfiveadapter.adapter.Document.TicketObjDocumentDB.EventDocumentDB;
import twentyfive.twentyfiveadapter.adapter.Mapper.TwentyFiveMapper;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/event")
public class EventController {
    private final EventService eventService;

    private final ExcelExportService exportService;


    public EventController(EventService eventService, ExcelExportService exportService) {
        this.eventService = eventService;
        this.exportService = exportService;
    }

    /*
     * Get event list end filters
     */
    @PostMapping("/filter")
    public ResponseEntity<Page<Event>> getEventList(@RequestBody EventFilter filterObject) {
        Pageable pageable = MethodUtils.makePageableFromFilter(filterObject);
        List<EventDocumentDB> eventPage = eventService.eventSearch(filterObject);
        List<Event> eventList = new ArrayList<>();
        for (EventDocumentDB eventDocumentDB : eventPage) {
              eventList.add(TwentyFiveMapper.INSTANCE.eventDocumentDBToEvent(eventDocumentDB));
        }
        Page<Event> eventpageRes = MethodUtils.convertListToPage(eventList, pageable);
        return ResponseEntity.ok(eventpageRes);
    }


    /*
        * Get event list

     */
    @GetMapping("/list")
    public ResponseEntity<List<Event>> getEventList() {
        List<EventDocumentDB> eventList = eventService.findAll();
        List<Event> mapList = new ArrayList<>();
        for (EventDocumentDB eventDocumentDB : eventList) {
            mapList.add(TwentyFiveMapper.INSTANCE.eventDocumentDBToEvent(eventDocumentDB));
        }
        return ResponseEntity.ok(mapList);
    }

    /*
     * Generate event
     */
    @PostMapping("/save")
    public ResponseEntity<Event> saveEvent(@RequestBody Event event) {
        eventService.saveEvent(event);
        return ResponseEntity.ok(event);

    }

    /*
     * Get event by id
     */
    @GetMapping("/get/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable String id) {
        Event event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }

    /*
     * Export event to excel
     */
    @GetMapping(value = "/export/excel", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> downloadExcel() {
        byte[] excelData = exportService.eventExportToExcel();
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=exported_data.xlsx")
                .body(excelData);
    }

    /*
     * Update event enable/disable
     */
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

}
