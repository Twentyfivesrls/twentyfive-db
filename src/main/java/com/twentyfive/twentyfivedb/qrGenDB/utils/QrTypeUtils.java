package com.twentyfive.twentyfivedb.qrGenDB.utils;

import lombok.extern.slf4j.Slf4j;
import twentyfive.twentyfiveadapter.models.qrGenModels.QrCodeObject;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
public class QrTypeUtils {

    public static String handleLinkType(QrCodeObject qrCodeObject) {
        String destinationUrl = qrCodeObject.getLink();
        try {
            URI uri = new URI(destinationUrl);
            if (uri.getScheme() == null) {
                destinationUrl = "http://" + destinationUrl;
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL format", e);
        }
        return destinationUrl;
    }

    public static String handlePhoneType(QrCodeObject qrCodeObject) {
        if (qrCodeObject.getPhoneNumber() == null || qrCodeObject.getPhoneNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be null or empty for 'telefono' type QR code");
        }
        return "tel:" + qrCodeObject.getPhoneNumber();
    }

    public static String handleSmsType(QrCodeObject qrCodeObject) {
        try {
            if (qrCodeObject.getSmsNumber() == null || qrCodeObject.getSmsNumber().trim().isEmpty()) {
                throw new IllegalArgumentException("SMS number cannot be null or empty for 'sms' type QR code");
            }

            String destinationUrl = "sms:" + qrCodeObject.getSmsNumber();

            if (qrCodeObject.getSmsMessage() != null && !qrCodeObject.getSmsMessage().trim().isEmpty()) {

                String encodedMessage = URLEncoder.encode(qrCodeObject.getSmsMessage(), StandardCharsets.UTF_8.name())
                        .replace("+", "%20");
                destinationUrl += "?body=" + encodedMessage;
            }

            return destinationUrl;

        } catch (UnsupportedEncodingException e) {
            System.err.println("Error encoding SMS message: " + e.getMessage());
            return "sms:" + qrCodeObject.getSmsNumber();
        }
    }


    public static String handleEmailType(QrCodeObject qrCodeObject) {
        try {
            if (qrCodeObject.getEmailTo() == null || qrCodeObject.getEmailTo().trim().isEmpty()) {
                throw new IllegalArgumentException("Email 'to' address cannot be null or empty for 'email' type QR code");
            }

            String destinationUrl = "mailto:" + qrCodeObject.getEmailTo();
            boolean hasSubject = qrCodeObject.getEmailSubject() != null && !qrCodeObject.getEmailSubject().isEmpty();
            boolean hasBody = qrCodeObject.getEmailBody() != null && !qrCodeObject.getEmailBody().isEmpty();

            if (hasSubject || hasBody) {
                destinationUrl += "?";

                if (hasSubject) {
                    destinationUrl += "subject=" + URLEncoder.encode(qrCodeObject.getEmailSubject(), StandardCharsets.UTF_8.name()).replace("+", "%20");
                }

                if (hasBody) {
                    if (hasSubject) {
                        destinationUrl += "&";
                    }
                    destinationUrl += "body=" + URLEncoder.encode(qrCodeObject.getEmailBody(), StandardCharsets.UTF_8.name())
                            .replace("+", "%20")
                            .replace("%0A", "%0D%0A");
                }
            }

            return destinationUrl;

        } catch (UnsupportedEncodingException e) {
            System.err.println("Error encoding email parameters: " + e.getMessage());
            return "mailto:" + qrCodeObject.getEmailTo();
        }
    }



    public static String handleWhatsappType(QrCodeObject qrCodeObject) {
        if (qrCodeObject.getPhoneNumber() == null || qrCodeObject.getPhoneNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be null or empty for 'whatsapp' type QR code");
        }
        return "https://wa.me/" + qrCodeObject.getPhoneNumber();
    }

    public static String handleWifiType(QrCodeObject qrCodeObject) {
        if (qrCodeObject.getWifiSSID() == null || qrCodeObject.getWifiSSID().trim().isEmpty()) {
            throw new IllegalArgumentException("Wi-Fi SSID cannot be null or empty for 'wifi' type QR code");
        }
        if (qrCodeObject.getWifiEncryption() == null || qrCodeObject.getWifiEncryption().trim().isEmpty()) {
            throw new IllegalArgumentException("Wi-Fi encryption type cannot be null or empty");
        }
        String destinationUrl = "WIFI:S:" + qrCodeObject.getWifiSSID() + ";T:" + qrCodeObject.getWifiEncryption();
        if (qrCodeObject.getWifiPassword() != null && !qrCodeObject.getWifiPassword().trim().isEmpty()) {
            destinationUrl += ";P:" + qrCodeObject.getWifiPassword();
        }
        if (qrCodeObject.isWifiHidden()) {
            destinationUrl += ";H:true";
        }
        return destinationUrl + ";;";
    }

    public static String handleTextType(QrCodeObject qrCodeObject) {
        if (qrCodeObject.getSimpleText() == null || qrCodeObject.getSimpleText().trim().isEmpty()) {
            throw new IllegalArgumentException("Text cannot be null or empty for 'testo' type QR code");
        }

        String searchQuery = qrCodeObject.getSimpleText().trim();
        return "https://www.google.com/search?q=" + URLEncoder.encode(searchQuery, StandardCharsets.UTF_8);
    }
}
