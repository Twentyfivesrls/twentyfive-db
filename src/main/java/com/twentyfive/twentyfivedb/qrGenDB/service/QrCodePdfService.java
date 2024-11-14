package com.twentyfive.twentyfivedb.qrGenDB.service;

import com.twentyfive.twentyfivedb.qrGenDB.repository.QrCodeGroupRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.models.qrGenModels.QrCodeGroup;

import java.io.IOException;
import java.util.List;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.ByteArrayOutputStream;

@Service
@Slf4j
public class QrCodePdfService {

    private static final int QR_CODE_SIZE = 130; // size of each QR code in pixels
    private static final int GAP = 10; // gap between QR codes in pixels
    private static final int MARGIN = 30; // page margin in pixels
    private static final int COLUMNS = 4; // number of columns in the grid

    private final QrCodeGroupRepository qrCodeGroupRepository;

    public QrCodePdfService(QrCodeGroupRepository qrCodeGroupRepository) {
        this.qrCodeGroupRepository = qrCodeGroupRepository;
    }

    public byte[] generateQrCodePdf(String usernameShopper, String groupNumber) throws IOException, WriterException {
        if (!groupNumber.contains("Gruppo ")) {
            throw new IllegalArgumentException("Invalid group number");
        }
        List<QrCodeGroup> qrCodes = qrCodeGroupRepository.findAllByUsernameAndAndGroupName(usernameShopper, groupNumber); // fetch first n QR codes
        if (qrCodes.isEmpty()) {
            throw new IllegalArgumentException("No QR codes found for the given group number");
        }
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        int xPosition = MARGIN;
        int yPosition = (int) page.getMediaBox().getHeight() - MARGIN - QR_CODE_SIZE;

        int count = 0;
        for (QrCodeGroup qrCode : qrCodes) {
            if (count > 0 && count % COLUMNS == 0) { // Move to the next row
                xPosition = MARGIN;
                yPosition -= (QR_CODE_SIZE + GAP);
            }

            if (yPosition < MARGIN) { // Add a new page if out of space
                contentStream.close();
                page = new PDPage();
                document.addPage(page);
                contentStream = new PDPageContentStream(document, page);
                yPosition = (int) page.getMediaBox().getHeight() - MARGIN - QR_CODE_SIZE;
            }

            // Generate QR code image from link
            PDImageXObject pdImage = createQrCodeImage(document, qrCode.getLink());

            // Draw the QR code image
            contentStream.drawImage(pdImage, xPosition, yPosition, QR_CODE_SIZE, QR_CODE_SIZE);

            // Update x position for the next QR code
            xPosition += QR_CODE_SIZE + GAP;
            count++;
        }

        contentStream.close(); // Close the last content stream

        // Save the document to a byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        document.save(outputStream);
        document.close();
        return outputStream.toByteArray();
    }

    private PDImageXObject createQrCodeImage(PDDocument document, String link) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(link, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE);
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);

        byte[] qrCodeBytes = pngOutputStream.toByteArray();
        return PDImageXObject.createFromByteArray(document, qrCodeBytes, "QR Code");
    }
}
