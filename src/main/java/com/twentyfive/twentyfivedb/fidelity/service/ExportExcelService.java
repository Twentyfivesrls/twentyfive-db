package com.twentyfive.twentyfivedb.fidelity.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.adapter.Document.FidelityDocumentDB.Card;
import twentyfive.twentyfiveadapter.adapter.Document.FidelityDocumentDB.CardGroup;
import twentyfive.twentyfiveadapter.adapter.Document.FidelityDocumentDB.Contact;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
public class ExportExcelService {

    private final CardService cardService;

    private final CardGroupService groupService;

    private final ContactService contactService;

    public ExportExcelService(CardService cardService, CardGroupService groupService, ContactService contactService) {
        this.cardService = cardService;
        this.groupService = groupService;
        this.contactService = contactService;
    }

    public byte[] cardExportByGroupId(String groupId){
        List<Card> data = cardService.getByGroupId(groupId);

        try(Workbook workbook = new XSSFWorkbook()){
            String currentDateTime = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

            String sheetName = "Lista_Card_" + currentDateTime;

            sheetName = sheetName.replace(" ", "_");
            Sheet sheet = workbook.createSheet(sheetName);
            Row row = sheet.createRow(0);

            row.createCell(0).setCellValue("Gruppo Card");
            row.createCell(1).setCellValue("Utente");
            row.createCell(2).setCellValue("Nome");
            row.createCell(3).setCellValue("Cognome");
            row.createCell(4).setCellValue("Email");
            row.createCell(5).setCellValue("N. Telefono");
            row.createCell(6).setCellValue("N. Scan Eseguiti");
            row.createCell(7).setCellValue("Data Creazione");
            row.createCell(8).setCellValue("Data Ultimo Scan");
            row.createCell(9).setCellValue("Attivo/Disattivo");

            int rowNum = 1;
            for(Card card : data){
                row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(card.getCardGroupId());
                row.createCell(1).setCellValue(card.getCustomerId());
                row.createCell(2).setCellValue(card.getName());
                row.createCell(3).setCellValue(card.getSurname());
                row.createCell(4).setCellValue(card.getEmail());
                row.createCell(5).setCellValue(card.getPhoneNumber());
                row.createCell(6).setCellValue(card.getScanNumberExecuted());
                row.createCell(7).setCellValue(String.valueOf(card.getCreationDate()));
                row.createCell(8).setCellValue(String.valueOf(card.getLastScanDate()));
                row.createCell(9).setCellValue(String.valueOf(card.getIsActive()));
            }
            return getBytes(workbook);
        }catch (IOException e){
            log.error("Error while exporting to excel", e);
            e.printStackTrace();
            return null;
        }
    }

    public byte[] cardExport(){
        List<Card> data = cardService.findAll();

        try(Workbook workbook = new XSSFWorkbook()){
            String currentDateTime = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

            String sheetName = "Lista_Card_" + currentDateTime;

            sheetName = sheetName.replace(" ", "_");
            Sheet sheet = workbook.createSheet(sheetName);
            Row row = sheet.createRow(0);

            row.createCell(0).setCellValue("Gruppo Card");
            row.createCell(1).setCellValue("Utente");
            row.createCell(2).setCellValue("Nome");
            row.createCell(3).setCellValue("Cognome");
            row.createCell(4).setCellValue("Email");
            row.createCell(5).setCellValue("N. Telefono");
            row.createCell(6).setCellValue("N. Scan Eseguiti");
            row.createCell(7).setCellValue("Data Creazione");
            row.createCell(8).setCellValue("Data Ultimo Scan");
            row.createCell(9).setCellValue("Attivo/Disattivo");

            int rowNum = 1;
            for(Card card : data){
                row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(card.getCardGroupId());
                row.createCell(1).setCellValue(card.getCustomerId());
                row.createCell(2).setCellValue(card.getName());
                row.createCell(3).setCellValue(card.getSurname());
                row.createCell(4).setCellValue(card.getEmail());
                row.createCell(5).setCellValue(card.getPhoneNumber());
                row.createCell(6).setCellValue(card.getScanNumberExecuted());
                row.createCell(7).setCellValue(String.valueOf(card.getCreationDate()));
                row.createCell(8).setCellValue(String.valueOf(card.getLastScanDate()));
                row.createCell(9).setCellValue(String.valueOf(card.getIsActive()));
            }
            return getBytes(workbook);
        }catch (IOException e){
            log.error("Error while exporting to excel", e);
            e.printStackTrace();
            return null;
        }
    }

    public byte[] groupExport(){
        List<CardGroup> data = groupService.findAll();

        try(Workbook workbook = new XSSFWorkbook()){
            String currentDateTime = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

            String sheetName = "Lista_Gruppi_Card" + currentDateTime;

            sheetName = sheetName.replace(" ", "_");
            Sheet sheet = workbook.createSheet(sheetName);
            Row row = sheet.createRow(0);

            row.createCell(0).setCellValue("Utente");
            row.createCell(1).setCellValue("Nome");
            row.createCell(2).setCellValue("Descrizione");
            row.createCell(3).setCellValue("Data creazione");
            row.createCell(4).setCellValue("Data scadenza");
            row.createCell(5).setCellValue("N. Scan");
            row.createCell(6).setCellValue("N. Giorni Premio");
            row.createCell(7).setCellValue("Attivo/Disattivo");

            int rowNum = 1;
            for(CardGroup group : data){
                row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(group.getOwnerId());
                row.createCell(1).setCellValue(group.getName());
                row.createCell(2).setCellValue(group.getDescription());
                row.createCell(3).setCellValue(String.valueOf(group.getCreationDate()));
                row.createCell(4).setCellValue(String.valueOf(group.getExpirationDate()));
                row.createCell(5).setCellValue(group.getScanNumber());
                row.createCell(6).setCellValue(group.getNumberOfDaysForPrize());
                row.createCell(7).setCellValue(String.valueOf(group.getIsActive()));
            }
            return getBytes(workbook);
        }catch (IOException e){
            log.error("Error while exporting to excel", e);
            e.printStackTrace();
            return null;
        }
    }

    public byte[] addressbookExport(){
        List<Contact> data = contactService.findAll();

        try(Workbook workbook = new XSSFWorkbook()){
            String currentDateTime = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

            String sheetName = "Lista_Contatti_" + currentDateTime;

            sheetName = sheetName.replace(" ", "_");
            Sheet sheet = workbook.createSheet(sheetName);
            Row row = sheet.createRow(0);

            row.createCell(0).setCellValue("Nome");
            row.createCell(1).setCellValue("Cognome");
            row.createCell(2).setCellValue("Email");
            row.createCell(3).setCellValue("N. Telefono");
            row.createCell(4).setCellValue("Data Creazione");

            int rowNum = 1;
            for(Contact contact : data){
                row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(contact.getName());
                row.createCell(1).setCellValue(contact.getSurname());
                row.createCell(2).setCellValue(contact.getEmail());
                row.createCell(3).setCellValue(contact.getPhoneNumber());
                row.createCell(4).setCellValue(String.valueOf(contact.getCreationDate()));
            }
            return getBytes(workbook);
        }catch (IOException e){
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
