package com.twentyfive.twentyfivedb.ticketDB.utils;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.twentyfive.twentyfivemodel.filterTicket.AddressBookFilter;
import com.twentyfive.twentyfivemodel.filterTicket.FilterObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j

public class MethodUtils {

    private static final int PAGE_SIZE = 5;
    private static final int PAGE_NUMBER = 0;

    private MethodUtils(){}

    public static void generateQRCodeImage(String text, int width, int height, String filePath)
            throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        Path path = FileSystems.getDefault().getPath(filePath);

        System.out.println("path: " + path);

        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);

    }

    public static byte[] getQRCodeImage(String text, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return pngOutputStream.toByteArray();
    }


    public static byte[] generateQrCodeImage(String text, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageConfig con = new MatrixToImageConfig(
                0xFFFFFFFF
                , 0xFF000000);

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
        log.info("makePageableFromFilter: " + filterObject);
        System.out.println("makePageableFromFilter: " + filterObject);
        if (filterObject.getPage() == null && filterObject.getSize() == null) {
            log.info("ENTRO IN NULLLLLLLLLL");
            System.out.println("ENTRO IN NULLLLLLLLLL");
            return PageRequest.of(PAGE_NUMBER, PAGE_SIZE);
        } else {
            log.info("ENTRO IN ELSE");
            System.out.println("ENTRO IN ELSE");
            if (filterObject.getPage() != null && filterObject.getSize() != null) {
                log.info("ENTRO IN IF");
                System.out.println("ENTRO IN IF");
                return PageRequest.of(filterObject.getPage(), filterObject.getSize());
            } else if (filterObject.getPage() != null) {
                log.info("ENTRO IN IF 2");
                System.out.println("ENTRO IN IF 2");
                return PageRequest.of(filterObject.getPage(), PAGE_SIZE);
            } else {
                log.info("ENTRO IN ELSE 2");
                System.out.println("ENTRO IN ELSE 2");
                return PageRequest.of(PAGE_NUMBER, filterObject.getSize());
            }
        }
    }


}
