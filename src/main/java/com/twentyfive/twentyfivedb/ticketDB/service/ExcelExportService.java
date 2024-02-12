package com.twentyfive.twentyfivedb.ticketDB.service;


import com.twentyfive.twentyfivedb.ticketDB.utils.MethodUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.adapter.Document.TicketObjDocumentDB.EventDocumentDB;
import twentyfive.twentyfiveadapter.adapter.Document.TicketObjDocumentDB.TicketDocumentDB;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Slf4j
@Service
public class ExcelExportService {

    private final EventService eventService;

    private final TicketService ticketService ;

    public ExcelExportService(EventService eventService, TicketService ticketService) {
        this.eventService = eventService;
        this.ticketService = ticketService;
    }


    public byte[] eventExportToExcel(String userId) {
        List<EventDocumentDB> data = eventService.findAllByUsername(userId);

        try (Workbook workbook = new XSSFWorkbook()) {
            String currentDateTime = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

            String sheetName = "Lista_Eventi_" + currentDateTime;

            sheetName = sheetName.replace(" ", "_");

            Sheet sheet = workbook.createSheet(sheetName);
            Row row = sheet.createRow(0);
            row.createCell(0).setCellValue("Titolo");
            row.createCell(1).setCellValue("Descrizione");
            row.createCell(2).setCellValue("Data Inizio");
            row.createCell(3).setCellValue("Data Fine");
            row.createCell(4).setCellValue("Luogo");

            int rowNum = 1;
            for (EventDocumentDB item : data) {
                 row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(item.getName());
                row.createCell(1).setCellValue(item.getDescription());
                if (item.getDateStart() != null) {
                    row.createCell(2).setCellValue(MethodUtils.formatDate(item.getDateStart()));
                }
                if (item.getDateEnd() != null) {
                    row.createCell(3).setCellValue(MethodUtils.formatDate(item.getDateEnd()));
                }
                row.createCell(4).setCellValue(item.getLocation());
            }

            return getBytes(workbook);
        } catch (IOException e) {
            log.error("Error while exporting to excel", e);
            e.printStackTrace();
            return null;
        }
    }


    public byte[] ticketExportToExcel(String userId) {
        List<TicketDocumentDB> data = ticketService.findAllByUserId(userId);

        try (Workbook workbook = new XSSFWorkbook()) {
            String currentDateTime = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

            String sheetName = "Lista_Ticket_" + currentDateTime;

            sheetName = sheetName.replace(" ", "_");

            Sheet sheet = workbook.createSheet(sheetName);

            Row row = sheet.createRow(0);
            row.createCell(0).setCellValue("Evento Associato");
            row.createCell(1).setCellValue("Codice");
            row.createCell(2).setCellValue("Data Inizio");
            row.createCell(3).setCellValue("Data Fine");
            row.createCell(4).setCellValue("Stato abilitazione");
            row.createCell(5).setCellValue("Stato utilizzo");
            int rowNum = 1;
            for (TicketDocumentDB item : data) {
                row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(item.getEventName());
                row.createCell(1).setCellValue(item.getCode());
                if (item.getDateStart() != null) {
                    row.createCell(2).setCellValue(MethodUtils.formatDate(item.getDateStart()));
                }
                if (item.getDateEnd() != null) {
                    row.createCell(3).setCellValue(MethodUtils.formatDate(item.getDateEnd()));
                }
                if (item.getActive() != null) {
                    if (item.getActive()){
                        row.createCell(4).setCellValue("Abilitato");
                    }
                    else {
                        row.createCell(4).setCellValue("Non abilitato");

                    }
                }
                if (item.getUsed() != null) {
                    if (item.getUsed()){
                        row.createCell(5).setCellValue("Utilizzato");
                    }
                    else{
                        row.createCell(5).setCellValue("Non utilizzato");

                    }
                }

                // Aggiungi colonne in base alle tue esigenze
            }
            return getBytes(workbook);
        } catch (IOException e) {
            log.error("Error while exporting to excel", e);
            e.printStackTrace();
            return null;
        }
    }

    private static byte[] getBytes(Workbook workbook) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        return outputStream.toByteArray();
    }
}
