package com.twentyfive.twentyfivedb.ticketDB.controller;


import com.google.zxing.WriterException;
import com.twentyfive.twentyfivedb.ticketDB.service.ExcelExportService;
import com.twentyfive.twentyfivedb.ticketDB.service.TicketService;
import com.twentyfive.twentyfivedb.ticketDB.utils.MethodUtils;
import com.twentyfive.twentyfivemodel.filterTicket.FilterObject;
import com.twentyfive.twentyfivemodel.filterTicket.TicketFilter;
import com.twentyfive.twentyfivemodel.models.ticketModels.Ticket;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twentyfive.twentyfiveadapter.adapter.Document.TicketObjDocumentDB.TicketDocumentDB;
import twentyfive.twentyfiveadapter.adapter.Mapper.TwentyFiveMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/ticket")
public class TicketController {

    @Autowired
    private  TicketService ticketService;

    private final ExcelExportService exportService;

    public TicketController(TicketService ticketService, ExcelExportService exportService) {
        this.ticketService = ticketService;
        this.exportService = exportService;
    }

    @GetMapping("/find/all")
    public ResponseEntity<List<Ticket>> getAll(@RequestParam("username") String username){
        List<TicketDocumentDB> ticketList = ticketService.findAllByUserId(username);
        List<Ticket> mapList = new ArrayList<>();
        for (TicketDocumentDB ticketDocumentDB : ticketList) {
            mapList.add(TwentyFiveMapper.INSTANCE.ticketDocumentDBToTicket(ticketDocumentDB));
        }
        return ResponseEntity.ok(mapList);
    }

    /*
    * Generate ticket
    */
    @PostMapping("/generate")
    public ResponseEntity<Ticket> generateTicket(@RequestBody Ticket ticket, @RequestParam("name") String name, @RequestParam("lastName") String lastName, @RequestParam("email") String email, @RequestParam("username") String username) {
        ticketService.saveTicket(ticket, name, lastName, email, username);
        return ResponseEntity.ok(ticket);
    }


    /*
    * Get ticket list end filters
     */
    @PostMapping("/list")
    public ResponseEntity<Page<Ticket>> getTicketList(@RequestBody Ticket filterObject, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size, @RequestParam("username") String username) {
        FilterObject filter = new FilterObject(page, size);
        Pageable pageable = MethodUtils.makePageableFromFilter(filter);
        System.out.println("USERNAME: " + username);
        List<TicketDocumentDB> ticketList = ticketService.ticketsSearch(filterObject, username);
        System.out.println("Lista: " + ticketList.size());
        List<Ticket> mapList = new ArrayList<>();
        for (TicketDocumentDB ticketDocumentDB : ticketList) {
            mapList.add(TwentyFiveMapper.INSTANCE.ticketDocumentDBToTicket(ticketDocumentDB));
        }
        Page<Ticket> ticket = MethodUtils.convertListToPage(mapList, pageable);
        return ResponseEntity.ok(ticket);

    }

    /*
    * Get ticket by id
     */
    @GetMapping("/getTicketById/{id}")
    public ResponseEntity<Ticket> getTicketById(@PathVariable String id) {

        TicketDocumentDB ticket = ticketService.getTicketById(id);
        return ResponseEntity.ok(TwentyFiveMapper.INSTANCE.ticketDocumentDBToTicket(ticket));
    }


    /*
    * Update ticket status(ublity or not)
     */
    @PutMapping("/setStatus/{id}/{status}")
    public ResponseEntity<Ticket> setStatus(@PathVariable String id, @PathVariable Boolean status) {


        ticketService.pdateTicketValidity(id, status);
        return ResponseEntity.ok().build();
    }

    /*
    * Update ticket used or not
     */
    @PutMapping("/update/usedTicket/{id}/{used}")
    public ResponseEntity<Ticket> setUsed(@PathVariable String id, @PathVariable Boolean used) {

        ticketService.updateUsedTicket(id, used);
        return ResponseEntity.ok().build();
    }

    /*
    * Delete ticket
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Ticket> deleteTicket(@PathVariable String id) {

        ticketService.deleteTicket(id);
        return ResponseEntity.ok().build();
    }


    /*
    * Export ticket list to excel
     */
    @GetMapping(value = "/export/excel", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> downloadExcel() {
        byte[] excelData = exportService.ticketExportToExcel();
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=exported_data.xlsx")
                .body(excelData);
    }

    /*
    * Get ticket by event name for statistics
     */
    @GetMapping("/getBy/eventName/{eventName}")
    public ResponseEntity<List<Ticket>> getTicketByEventName(@PathVariable String eventName) {

        List<TicketDocumentDB> ticketList = ticketService.getTicketsByEventName(eventName);
        List<Ticket> mapList = new ArrayList<>();
        for (TicketDocumentDB ticketDocumentDB : ticketList) {
            mapList.add(TwentyFiveMapper.INSTANCE.ticketDocumentDBToTicket(ticketDocumentDB));
        }
        return ResponseEntity.ok(mapList);
    }

    /*
    * Get ticket by is used for statistics
     */
    @GetMapping("/getBy/ticket/isUsed/{isUsed}")
    public ResponseEntity<List<Ticket>> getTicketByIsUsed(@PathVariable Boolean isUsed) {

        List<TicketDocumentDB> ticketList = ticketService.getTicketsByIsUsed(isUsed);
        List<Ticket> mapList = new ArrayList<>();
        for (TicketDocumentDB ticketDocumentDB : ticketList) {
            mapList.add(TwentyFiveMapper.INSTANCE.ticketDocumentDBToTicket(ticketDocumentDB));
        }
        return ResponseEntity.ok(mapList);
    }

    /*
    qrcode generator
     */
    @GetMapping("generate/qrCode/ticket/number/{ticketNumber}")
    public ResponseEntity<byte[]> generateQrCode(@PathVariable String ticketNumber) throws IOException, WriterException {
        byte[] qrCode = MethodUtils.generateQrCodeImage(ticketNumber,350,350);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=qrCode.png")
                .body(qrCode);
    }

    @GetMapping("getTicket/byCode/{code}")
    public ResponseEntity<List<Ticket>> getTicketByCode(@PathVariable String code) {

        List<Ticket> mapList = new ArrayList<>();

        List<TicketDocumentDB> ticket = ticketService.findByCode(code);
        for (TicketDocumentDB ticketDocumentDB : ticket) {
            mapList.add(TwentyFiveMapper.INSTANCE.ticketDocumentDBToTicket(ticketDocumentDB));
        }
        return ResponseEntity.ok(mapList);
    }
}
