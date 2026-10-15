package com.twentyfive.twentyfivedb.fidelity.wallet;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.models.fidelityModels.Card;
import twentyfive.twentyfiveadapter.models.fidelityModels.CardGroup;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Integrazione Apple Wallet: genera la tessera .pkpass firmata e invia le notifiche
 * push (APNs) che fanno scaricare all'iPhone la versione aggiornata.
 */
@Service
@Slf4j
public class AppleWalletService {

    private static final String APNS_URL = "https://api.push.apple.com/3/device/";

    private final String passTypeId;
    private final String teamId;
    private final String p12File;
    private final String p12Password;
    private final String wwdrFile;
    private final String webServiceUrl;
    private final WalletCardContent content;
    private final WalletImageService imageService;
    private final ObjectMapper objectMapper;

    private volatile SigningMaterial signing;
    private volatile HttpClient apnsClient;

    private record SigningMaterial(PrivateKey key, X509Certificate certificate, X509Certificate wwdr,
                                   KeyStore keyStore, byte[] authSecret) {
    }

    public AppleWalletService(@Value("${wallet.apple.pass-type-id:}") String passTypeId,
                              @Value("${wallet.apple.team-id:}") String teamId,
                              @Value("${wallet.apple.p12-file:}") String p12File,
                              @Value("${wallet.apple.p12-password:}") String p12Password,
                              @Value("${wallet.apple.wwdr-file:}") String wwdrFile,
                              @Value("${wallet.apple.web-service-url:}") String webServiceUrl,
                              WalletCardContent content,
                              WalletImageService imageService,
                              ObjectMapper objectMapper) {
        this.passTypeId = passTypeId;
        this.teamId = teamId;
        this.p12File = p12File;
        this.p12Password = p12Password;
        this.wwdrFile = wwdrFile;
        this.webServiceUrl = webServiceUrl;
        this.content = content;
        this.imageService = imageService;
        this.objectMapper = objectMapper;
    }

    public boolean isConfigured() {
        return StringUtils.isNoneBlank(passTypeId, teamId, p12File, wwdrFile)
                && Files.isReadable(Path.of(p12File))
                && Files.isReadable(Path.of(wwdrFile));
    }

    /** Gli aggiornamenti richiedono un web service HTTPS pubblico: senza, la tessera resta statica. */
    public boolean supportsUpdates() {
        return webServiceUrl.startsWith("https://");
    }

    public String getPassTypeId() {
        return passTypeId;
    }

    // ══════════════════════════════════════════════════════════════════════
    // Tessera .pkpass
    // ══════════════════════════════════════════════════════════════════════

    public byte[] buildPass(Card card, CardGroup group) throws IOException, GeneralSecurityException {
        SigningMaterial material = signing();

        Map<String, byte[]> files = new LinkedHashMap<>();
        files.put("pass.json", objectMapper.writeValueAsBytes(passJson(card, group)));
        // Logo del gruppo (o logo Fidelity) e icona alle tre densità
        files.putAll(imageService.appleImages(group.getId()));

        Map<String, String> manifest = new LinkedHashMap<>();
        for (Map.Entry<String, byte[]> file : files.entrySet()) {
            manifest.put(file.getKey(), sha1Hex(file.getValue()));
        }
        byte[] manifestBytes = objectMapper.writeValueAsBytes(manifest);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            for (Map.Entry<String, byte[]> file : files.entrySet()) {
                addZipEntry(zip, file.getKey(), file.getValue());
            }
            addZipEntry(zip, "manifest.json", manifestBytes);
            addZipEntry(zip, "signature", sign(manifestBytes, material));
        }
        return out.toByteArray();
    }

    private Map<String, Object> passJson(Card card, CardGroup group) throws GeneralSecurityException, IOException {
        WalletCardContent.Data data = content.build(card, group);

        Map<String, Object> pass = new LinkedHashMap<>();
        pass.put("formatVersion", 1);
        pass.put("passTypeIdentifier", passTypeId);
        pass.put("serialNumber", data.cardId());
        pass.put("teamIdentifier", teamId);
        pass.put("organizationName", content.getIssuerName());
        pass.put("description", (data.voucher() ? "Voucher " : "Carta fedeltà ") + data.groupName());
        pass.put("logoText", data.groupName());
        // Colori Fidelity: sfondo brand #1A1660, testi bianchi, etichette lilla
        pass.put("foregroundColor", "rgb(255, 255, 255)");
        pass.put("backgroundColor", "rgb(26, 22, 96)");
        pass.put("labelColor", "rgb(190, 184, 255)");
        if (supportsUpdates()) {
            pass.put("webServiceURL", webServiceUrl);
            pass.put("authenticationToken", authenticationToken(data.cardId()));
        }
        if (!data.active()) {
            pass.put("voided", true);
        }

        Map<String, Object> balance = field("balance", data.balanceLabel(), data.balanceValue());
        // Testo della notifica sulla schermata di blocco quando il valore cambia
        balance.put("changeMessage", data.balanceLabel() + ": %@");

        // Riga sotto la strip: scadenza per le card fedeltà, programma per i voucher
        Map<String, Object> auxiliary;
        if (data.expirationDate() != null) {
            auxiliary = field("expiration", "Scadenza",
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(
                            data.expirationDate().toInstant().atZone(ZoneId.systemDefault())));
            auxiliary.put("dateStyle", "PKDateStyleShort");
        } else {
            auxiliary = field("program", "Programma", data.groupName());
        }

        Map<String, Object> storeCard = new LinkedHashMap<>();
        // Intestazione: visibile anche quando le tessere sono impilate nel Wallet
        storeCard.put("headerFields", List.of(field("headerBalance", data.balanceLabel(), data.balanceValue())));
        // Valore principale: Apple lo scrive sopra la strip, a sinistra del logo del gruppo
        storeCard.put("primaryFields", List.of(balance));
        storeCard.put("secondaryFields", List.of(
                field("holder", "Titolare", data.holder()),
                field("code", "Codice", data.code())));
        storeCard.put("auxiliaryFields", List.of(auxiliary));
        storeCard.put("backFields", List.of(
                field("program", "Programma", data.groupName()),
                field("circuit", "Circuito", content.getIssuerName())));
        pass.put("storeCard", storeCard);

        pass.put("barcodes", List.of(Map.of(
                "format", "PKBarcodeFormatQR",
                "message", data.qrValue(),
                "messageEncoding", "iso-8859-1",
                "altText", data.code())));
        return pass;
    }

    private static Map<String, Object> field(String key, String label, String value) {
        Map<String, Object> field = new LinkedHashMap<>();
        field.put("key", key);
        field.put("label", label);
        field.put("value", value == null ? "" : value);
        return field;
    }

    private static void addZipEntry(ZipOutputStream zip, String name, byte[] bytes) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(bytes);
        zip.closeEntry();
    }

    private static String sha1Hex(byte[] bytes) throws NoSuchAlgorithmException {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest(bytes));
    }

    /** Firma PKCS#7 "detached" del manifest, con il certificato della tessera e l'intermedio WWDR. */
    private static byte[] sign(byte[] manifest, SigningMaterial material) throws IOException, GeneralSecurityException {
        try {
            CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
            generator.addSignerInfoGenerator(
                    new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().build())
                            .build(new JcaContentSignerBuilder("SHA256withRSA").build(material.key()), material.certificate()));
            generator.addCertificates(new JcaCertStore(List.of(material.certificate(), material.wwdr())));
            return generator.generate(new CMSProcessableByteArray(manifest), false).getEncoded();
        } catch (org.bouncycastle.operator.OperatorCreationException | org.bouncycastle.cms.CMSException e) {
            throw new GeneralSecurityException("Firma della tessera Apple non riuscita", e);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Autenticazione web service
    // ══════════════════════════════════════════════════════════════════════

    /** Token della tessera, derivato dal serialNumber: non serve salvarlo. */
    public String authenticationToken(String serialNumber) throws GeneralSecurityException, IOException {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(signing().authSecret(), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(serialNumber.getBytes(StandardCharsets.UTF_8)));
    }

    /** Verifica l'header "Authorization: ApplePass <token>" inviato dall'iPhone. */
    public boolean isAuthorized(String authorizationHeader, String serialNumber) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("ApplePass ")) {
            return false;
        }
        try {
            byte[] expected = authenticationToken(serialNumber).getBytes(StandardCharsets.UTF_8);
            byte[] received = authorizationHeader.substring("ApplePass ".length()).trim().getBytes(StandardCharsets.UTF_8);
            return MessageDigest.isEqual(expected, received);
        } catch (GeneralSecurityException | IOException e) {
            log.error("Verifica token Apple Wallet non riuscita", e);
            return false;
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Notifiche push
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Notifica ai dispositivi che la tessera è cambiata (payload vuoto, come da specifica Wallet).
     * Restituisce i token non più validi, da rimuovere.
     */
    public List<String> push(Collection<String> pushTokens) throws IOException, GeneralSecurityException {
        List<String> invalid = new ArrayList<>();
        for (String token : pushTokens) {
            HttpRequest request = HttpRequest.newBuilder(URI.create(APNS_URL + token))
                    .header("apns-topic", passTypeId)
                    .POST(HttpRequest.BodyPublishers.ofString("{}"))
                    .build();
            try {
                HttpResponse<String> response = apnsClient().send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 410 || (response.statusCode() == 400 && response.body().contains("BadDeviceToken"))) {
                    invalid.add(token);
                } else if (response.statusCode() != 200) {
                    log.warn("APNs: HTTP {} - {}", response.statusCode(), response.body());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return invalid;
            }
        }
        return invalid;
    }

    private HttpClient apnsClient() throws IOException, GeneralSecurityException {
        if (apnsClient == null) {
            synchronized (this) {
                if (apnsClient == null) {
                    // APNs accetta lo stesso certificato della tessera come certificato client
                    KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                    kmf.init(signing().keyStore(), p12Password.toCharArray());
                    SSLContext sslContext = SSLContext.getInstance("TLS");
                    sslContext.init(kmf.getKeyManagers(), null, null);
                    apnsClient = HttpClient.newBuilder()
                            .version(HttpClient.Version.HTTP_2)
                            .sslContext(sslContext)
                            .build();
                }
            }
        }
        return apnsClient;
    }

    // ══════════════════════════════════════════════════════════════════════
    // Certificati
    // ══════════════════════════════════════════════════════════════════════

    private SigningMaterial signing() throws IOException, GeneralSecurityException {
        if (signing == null) {
            synchronized (this) {
                if (signing == null) {
                    KeyStore keyStore = KeyStore.getInstance("PKCS12");
                    try (InputStream in = new FileInputStream(p12File)) {
                        keyStore.load(in, p12Password.toCharArray());
                    }
                    String alias = Collections.list(keyStore.aliases()).stream()
                            .filter(a -> {
                                try {
                                    return keyStore.isKeyEntry(a);
                                } catch (KeyStoreException e) {
                                    return false;
                                }
                            })
                            .findFirst()
                            .orElseThrow(() -> new KeyStoreException("Nessuna chiave privata nel file .p12"));
                    PrivateKey key = (PrivateKey) keyStore.getKey(alias, p12Password.toCharArray());
                    X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);

                    X509Certificate wwdr;
                    try (InputStream in = new FileInputStream(wwdrFile)) {
                        wwdr = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(in);
                    }

                    // Segreto per i token delle tessere, derivato dalla chiave privata (stabile e non pubblico)
                    byte[] authSecret = MessageDigest.getInstance("SHA-256").digest(key.getEncoded());

                    signing = new SigningMaterial(key, certificate, wwdr, keyStore, authSecret);
                }
            }
        }
        return signing;
    }
}
