package com.twentyfive.twentyfivedb.qrGenDB.service;

import com.twentyfive.twentyfivedb.qrGenDB.repository.QrCodeGroupRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.models.qrGenModels.QrCodeGroup;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class QrCodeSvgService {

    private static final int QR_CODE_SIZE = 130; // size of each QR code in pixels
    private static final int GAP = 10; // gap between QR codes in pixels
    private static final int MARGIN = 30; // page margin in pixels
    private static final int COLUMNS = 4; // number of columns in the grid

    private final QrCodeGroupRepository qrCodeGroupRepository;

    public QrCodeSvgService(QrCodeGroupRepository qrCodeGroupRepository) {
        this.qrCodeGroupRepository = qrCodeGroupRepository;
    }

    public byte[] generateQrCodeSvg(String usernameShopper, String groupNumber) throws IOException, WriterException {
        if (!groupNumber.contains("Gruppo ")) {
            throw new IllegalArgumentException("Invalid group number");
        }

        // Retrieve QR codes from the database
        List<QrCodeGroup> qrCodes = qrCodeGroupRepository.findAllByUsernameAndAndGroupName(usernameShopper, groupNumber);
        if (qrCodes.isEmpty()) {
            throw new IllegalArgumentException("No QR codes found for the given group number");
        }

        // Create SVG document
        StringBuilder svgBuilder = new StringBuilder();
        svgBuilder.append("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\">\n");

        int xPosition = MARGIN;
        int yPosition = MARGIN;

        int count = 0;
        for (QrCodeGroup qrCode : qrCodes) {
            if (count > 0 && count % COLUMNS == 0) { // Add new row
                xPosition = MARGIN;
                yPosition += QR_CODE_SIZE + GAP;
            }

            // Generate the QR code as an SVG string
            String qrCodeSvg = generateQrCodeSvgForLink(qrCode.getLink());

            // Add QR code SVG to document
            svgBuilder.append(String.format("<g transform=\"translate(%d, %d)\">%s</g>\n", xPosition, yPosition, qrCodeSvg));

            // Position for the next QR code
            xPosition += QR_CODE_SIZE + GAP;
            count++;
        }

        svgBuilder.append("</svg>");

        // Return the SVG document as a byte array
        return svgBuilder.toString().getBytes();
    }

    // Method to generate the QR code as an SVG string
    private String generateQrCodeSvgForLink(String link) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(link, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE);

        // Create SVG representation of the QR code
        StringBuilder svg = new StringBuilder();
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"" + QR_CODE_SIZE + "\" height=\"" + QR_CODE_SIZE + "\">\n");
        svg.append("<rect width=\"100%\" height=\"100%\" fill=\"#ffffff\"/>\n");

        for (int y = 0; y < QR_CODE_SIZE; y++) {
            for (int x = 0; x < QR_CODE_SIZE; x++) {
                if (bitMatrix.get(x, y)) {
                    svg.append(String.format("<rect x=\"%d\" y=\"%d\" width=\"1\" height=\"1\" fill=\"#000000\"/>\n", x, y));
                }
            }
        }

        svg.append("</svg>");
        return svg.toString();
    }
}
