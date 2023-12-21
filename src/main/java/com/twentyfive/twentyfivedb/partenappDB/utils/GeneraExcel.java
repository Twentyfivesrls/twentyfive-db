package com.twentyfive.twentyfivedb.partenappDB.utils;

import com.twentyfive.twentyfivemodel.dto.partenupDto.RiepilogoPerFrontEnd;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.text.SimpleDateFormat;
import java.util.List;

public class GeneraExcel {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<RiepilogoPerFrontEnd> listariepilogo;


    public GeneraExcel(List<RiepilogoPerFrontEnd> listariepilogo) {
        this.listariepilogo = listariepilogo;
        workbook = new XSSFWorkbook();
    }


    private void writeHeaderLine() {
        sheet = workbook.createSheet("Riepiloghi");

        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(12);
        style.setFont(font);

        createCell(row, 0, "Data", style);
        createCell(row, 1, "Punto Vendita", style);
        createCell(row, 2, "Base di carico", style);
        createCell(row, 3, "Fornitore", style);
        createCell(row, 4, "Gasolio", style);
        createCell(row, 5, "Benzina", style);
        createCell(row, 6, "Supreme", style);
        createCell(row, 7, "Gpl", style);
        createCell(row, 8, "Totale volumi", style);
        createCell(row, 9, "Cali gasolio", style);
        createCell(row, 10, "Cali benzina", style);
        createCell(row, 11, "Cali supreme", style);
        createCell(row, 12, "Cali gpl", style);
        createCell(row, 13, "Ultimo scarico", style);
        createCell(row, 14, "Vettore", style);
        createCell(row, 15, "DAS", style);
        createCell(row, 16, "Prezzo gasolio fornitore", style);
        createCell(row, 17, "Prezzo benzina fornitore", style);
        createCell(row, 18, "Prezzo supreme fornitore", style);
        createCell(row, 19, "Prezzo gpl fornitore", style);
        createCell(row, 20, "Numero fattura fornitore", style);
        createCell(row, 21, "Importo fattura fornitore", style);
        createCell(row, 22, "Numero fattura partenopea", style);
        createCell(row, 23, "Importo fattura partenopea", style);
        createCell(row, 24, "Data bonifico", style);
        createCell(row, 25, "Importo bonifico", style);
        createCell(row, 26, "Importo preventivo", style);
        createCell(row, 27, "Residuo da versare", style);

    }


    private void createCell(Row row, int columnCount, Object value, CellStyle style) {
        sheet.autoSizeColumn(columnCount);
        Cell cell = row.createCell(columnCount);
        if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else {
            cell.setCellValue((String) value);
        }
        cell.setCellStyle(style);
    }


    private void writeDataLines() {
        int rowCount = 1;

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);

        CellStyle currencyStyle;
        XSSFDataFormat cf = workbook.createDataFormat();
        currencyStyle = workbook.createCellStyle();
        currencyStyle.setDataFormat(cf.getFormat("€#,##0.00_);[Red]€#,##0.00)"));

        for (RiepilogoPerFrontEnd riep : listariepilogo) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            if (riep.getFabbisogno() != null) {
                createCell(row, columnCount++, sdf.format(riep.getFabbisogno().getData()), style);
                if (riep.getFabbisogno().getPuntoVendita() != null) {
                    createCell(row, columnCount++, riep.getFabbisogno().getPuntoVendita().getNome(), style);
                } else {
                    createCell(row, columnCount++, " ", style);
                }
                if (riep.getFabbisogno().getBasedicarico() != null) {
                    createCell(row, columnCount++, riep.getFabbisogno().getBasedicarico().getNomebasedicarico(), style);
                } else {
                    createCell(row, columnCount++, " ", style);
                }
                if (riep.getFabbisogno().getFornitore() != null) {
                    createCell(row, columnCount++, riep.getFabbisogno().getFornitore().getNomefornitore(), style);
                } else {
                    createCell(row, columnCount++, " ", style);
                }
                createCell(row, columnCount++, riep.getFabbisogno().getGasolio(), style);
                createCell(row, columnCount++, riep.getFabbisogno().getBenzina(), style);
                createCell(row, columnCount++, riep.getFabbisogno().getSupreme(), style);
                createCell(row, columnCount++, riep.getFabbisogno().getGpl(), style);
            } else {
                // Devo riempire 8 celle
                for (int i = 0; i < 8; i++) {
                    createCell(row, columnCount++, " ", style);
                }
            }


            createCell(row, columnCount++, riep.getTotalevolumicarburantitradizionali(), style);
            createCell(row, columnCount++, riep.getCaligasolio(), style);
            createCell(row, columnCount++, riep.getCalibenzina(), style);
            createCell(row, columnCount++, riep.getCalisupreme(), style);
            createCell(row, columnCount++, riep.getCaligpl(), style);
            createCell(row, columnCount++, riep.getUltimoscarico(), style);


            if (riep.getTrasporto() != null) {
                createCell(row, columnCount++, riep.getTrasporto().getNometrasportatore(), style);
            } else {
                createCell(row, columnCount++, " ", style);
            }


            createCell(row, columnCount++, riep.getDas(), style);
            createCell(row, columnCount++, riep.getPrezzogasoliofornitore(), currencyStyle);
            createCell(row, columnCount++, riep.getPrezzobenzinafornitore(), currencyStyle);
            createCell(row, columnCount++, riep.getPrezzosupremefornitore(), currencyStyle);
            createCell(row, columnCount++, riep.getPrezzogplfornitore(), currencyStyle);
            createCell(row, columnCount++, riep.getNumerofatturafornitore(), style);
            createCell(row, columnCount++, riep.getImportofatturafornitore(), currencyStyle);
            createCell(row, columnCount++, riep.getNumerofatturapartenopea(), style);
            createCell(row, columnCount++, riep.getImportofattura(), currencyStyle);

            if (riep.getDatabonifico() != null) {
                createCell(row, columnCount++, sdf.format(riep.getDatabonifico()), style);
            } else {
                createCell(row, columnCount++, " ", style);
            }

            createCell(row, columnCount++, riep.getImportopreventivo(), currencyStyle);
            createCell(row, columnCount++, riep.getResiduodaversare(), currencyStyle);

        }
    }


    public XSSFWorkbook export() {
        writeHeaderLine();
        writeDataLines();
        return workbook;

    }
}