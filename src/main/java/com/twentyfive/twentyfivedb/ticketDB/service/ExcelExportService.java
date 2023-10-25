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
            Sheet sheet = workbook.createSheet("Data");

            int rowNum = 0;
            for (EventDocumentDB item : data) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(item.getId());
                row.createCell(1).setCellValue(item.getName());
                row.createCell(2).setCellValue(item.getDescription());
                if (item.getDate() != null) {
                    row.createCell(3).setCellValue(MethodUtils.formatDate(item.getDate()));
                }
                row.createCell(4).setCellValue(item.getLocation());
                if (item.getEnabled() != null) {
                    row.createCell(5).setCellValue(item.getEnabled().toString());
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


    public byte[] ticketExportToExcel(String userId) {
        List<TicketDocumentDB> data = ticketService.findAllByUserId(userId);
        System.out.println("TICKET LIST :"+ data);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Data");


            int rowNum = 0;
            for (TicketDocumentDB item : data) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(item.getId());
                row.createCell(1).setCellValue(item.getEventName());
                row.createCell(2).setCellValue(item.getCode());
                if (item.getEventDateStart() != null) {
                    row.createCell(3).setCellValue(MethodUtils.formatDate(item.getEventDateStart()));
                }
                if (item.getEventDateEnd() != null) {
                    row.createCell(4).setCellValue(MethodUtils.formatDate(item.getEventDateEnd()));
                }
                if (item.getActive() != null) {
                    row.createCell(5).setCellValue(item.getActive().toString());
                }
                if (item.getUsed() != null) {
                    row.createCell(6).setCellValue(item.getUsed().toString());
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
