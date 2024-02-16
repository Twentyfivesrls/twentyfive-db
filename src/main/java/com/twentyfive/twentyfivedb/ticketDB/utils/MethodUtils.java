package com.twentyfive.twentyfivedb.ticketDB.utils;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.twentyfive.twentyfivemodel.filterTicket.FilterObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import twentyfive.twentyfiveadapter.adapter.Document.TicketObjDocumentDB.AddressBookDocumentDB;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j

public class MethodUtils {

    private static final int PAGE_SIZE = 5;
    private static final int PAGE_NUMBER = 0;

    private MethodUtils(){}

    public static byte[] generateQrCodeImage(String text, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return pngOutputStream.toByteArray();
    }

    public static String formatDate(LocalDateTime date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return date.format(formatter);
    }


    public static <T> Page<T> convertListToPage(List<T> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());

        return new PageImpl<>(list.subList(start, end), pageable, list.size());
    }

    public static Pageable makePageableFromFilter(FilterObject filterObject) {

        if (filterObject.getPage() == null && filterObject.getSize() == null) {

            return PageRequest.of(PAGE_NUMBER, PAGE_SIZE);
        } else {

            if (filterObject.getPage() != null && filterObject.getSize() != null) {

                return PageRequest.of(filterObject.getPage(), filterObject.getSize());
            } else if (filterObject.getPage() != null) {

                return PageRequest.of(filterObject.getPage(), PAGE_SIZE);
            } else {

                return PageRequest.of(PAGE_NUMBER, filterObject.getSize());
            }
        }
    }

    // Metodo ausiliario per verificare se il Pattern è valido
    public static boolean isValidPattern(Pattern pattern) {
        return pattern != null && pattern.pattern() != null && !pattern.pattern().isEmpty();
    }

    public static boolean existedAddress(AddressBookDocumentDB address1, AddressBookDocumentDB address2){
        return address1.getEmail().equals(address2.getEmail()) && address1.getFirstName().equals(address2.getFirstName()) && address1.getLastName().equals(address2.getLastName());
    }
}
