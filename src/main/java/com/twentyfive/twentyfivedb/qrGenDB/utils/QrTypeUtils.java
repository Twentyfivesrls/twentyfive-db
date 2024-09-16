package com.twentyfive.twentyfivedb.qrGenDB.utils;

import twentyfive.twentyfiveadapter.models.qrGenModels.QrCodeObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
        if (qrCodeObject.getSmsNumber() == null || qrCodeObject.getSmsNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("SMS number cannot be null or empty for 'sms' type QR code");
        }
        String destinationUrl = "sms:" + qrCodeObject.getSmsNumber();
        if (qrCodeObject.getSmsMessage() != null && !qrCodeObject.getSmsMessage().trim().isEmpty()) {
            destinationUrl += "?body=" + qrCodeObject.getSmsMessage();
        }
        return destinationUrl;
    }

    public static String handleEmailType(QrCodeObject qrCodeObject) {
        if (qrCodeObject.getEmailTo() == null || qrCodeObject.getEmailTo().trim().isEmpty()) {
            throw new IllegalArgumentException("Email 'to' address cannot be null or empty for 'email' type QR code");
        }
        String destinationUrl = "mailto:" + qrCodeObject.getEmailTo();
        if (qrCodeObject.getEmailSubject() != null || qrCodeObject.getEmailBody() != null) {
            destinationUrl += "?subject=" + (qrCodeObject.getEmailSubject() != null ? qrCodeObject.getEmailSubject() : "");
            destinationUrl += "&body=" + (qrCodeObject.getEmailBody() != null ? qrCodeObject.getEmailBody() : "");
        }
        return destinationUrl;
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
