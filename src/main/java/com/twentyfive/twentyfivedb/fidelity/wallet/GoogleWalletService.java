package com.twentyfive.twentyfivedb.fidelity.wallet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.ServiceAccountCredentials;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.models.fidelityModels.Card;
import twentyfive.twentyfiveadapter.models.fidelityModels.CardGroup;

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
import java.security.GeneralSecurityException;
import java.security.Signature;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

/**
 * Integrazione Google Wallet (tessere fedeltà):
 * una LoyaltyClass per gruppo card e un LoyaltyObject per card.
 * Gli oggetti vengono aggiornati via API: le modifiche arrivano da sole sul telefono.
 */
@Service
@Slf4j
public class GoogleWalletService {

    private static final String API = "https://walletobjects.googleapis.com/walletobjects/v1";
    private static final String SCOPE = "https://www.googleapis.com/auth/wallet_object.issuer";
    private static final String SAVE_URL = "https://pay.google.com/gp/v/save/";
    /** Colore brand Fidelity: Google sceglie da solo il colore dei testi per il contrasto. */
    private static final String BACKGROUND_COLOR = "#1A1660";

    private final String issuerId;
    private final String serviceAccountFile;
    private final List<String> origins;
    private final WalletCardContent content;
    private final WalletImageService imageService;
    private final ObjectMapper objectMapper;
    private final HttpClient http = HttpClient.newHttpClient();

    private volatile ServiceAccountCredentials credentials;

    public GoogleWalletService(@Value("${wallet.google.issuer-id:}") String issuerId,
                               @Value("${wallet.google.service-account-file:}") String serviceAccountFile,
                               @Value("${wallet.google.origins:}") String origins,
                               WalletCardContent content,
                               WalletImageService imageService,
                               ObjectMapper objectMapper) {
        this.issuerId = issuerId;
        this.serviceAccountFile = serviceAccountFile;
        // Domini da cui viene mostrato il pulsante (separati da virgola)
        this.origins = Arrays.stream(origins.split(",")).map(String::trim).filter(o -> !o.isEmpty()).toList();
        this.content = content;
        this.imageService = imageService;
        this.objectMapper = objectMapper;
    }

    public boolean isConfigured() {
        // Il logo deve essere su un URL pubblico: Google lo scarica dai propri server
        return StringUtils.isNoneBlank(issuerId, serviceAccountFile) && imageService.hasPublicUrl()
                && Files.isReadable(Path.of(serviceAccountFile));
    }

    // ══════════════════════════════════════════════════════════════════════
    // Operazioni pubbliche
    // ══════════════════════════════════════════════════════════════════════

    /** Crea/aggiorna classe e oggetto della card e restituisce il link "Aggiungi a Google Wallet". */
    public String createSaveUrl(Card card, CardGroup group) throws IOException, InterruptedException, GeneralSecurityException {
        upsert("loyaltyClass", classId(group.getId()), classBody(group));
        upsert("loyaltyObject", objectId(card.getId()), objectBody(card, group));
        return SAVE_URL + signSaveJwt(objectId(card.getId()));
    }

    /** Aggiorna l'oggetto della card (se non esiste ancora non fa nulla). */
    public void updateObject(Card card, CardGroup group) throws IOException, InterruptedException {
        patch("loyaltyObject", objectId(card.getId()), objectBody(card, group));
    }

    /** Aggiorna la classe del gruppo (se non esiste ancora non fa nulla). */
    public void updateClass(CardGroup group) throws IOException, InterruptedException {
        patch("loyaltyClass", classId(group.getId()), classBody(group));
    }

    // ══════════════════════════════════════════════════════════════════════
    // Contenuti
    // ══════════════════════════════════════════════════════════════════════

    private String classId(String groupId) {
        return issuerId + ".fidelity_group_" + groupId;
    }

    private String objectId(String cardId) {
        return issuerId + ".fidelity_card_" + cardId;
    }

    private Map<String, Object> classBody(CardGroup group) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", classId(group.getId()));
        body.put("issuerName", content.getIssuerName());
        body.put("programName", group.getName());
        body.put("programLogo", Map.of(
                "sourceUri", Map.of("uri", imageService.googleLogoUrl(group.getId())),
                "contentDescription", localized(group.getName())));
        body.put("hexBackgroundColor", BACKGROUND_COLOR);
        body.put("reviewStatus", "UNDER_REVIEW");
        return body;
    }

    private Map<String, Object> objectBody(Card card, CardGroup group) {
        WalletCardContent.Data data = content.build(card, group);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", objectId(card.getId()));
        body.put("classId", classId(group.getId()));
        body.put("state", data.active() ? "ACTIVE" : "INACTIVE");
        body.put("accountId", data.code());
        body.put("accountName", data.holder());
        body.put("loyaltyPoints", Map.of(
                "label", data.balanceLabel(),
                "balance", Map.of("string", data.balanceValue())));
        body.put("barcode", Map.of(
                "type", "QR_CODE",
                "value", data.qrValue(),
                "alternateText", data.code()));
        if (data.expirationDate() != null) {
            body.put("textModulesData", List.of(Map.of(
                    "id", "expiration",
                    "header", "Scadenza",
                    "body", new SimpleDateFormat("dd/MM/yyyy").format(data.expirationDate()))));
        }
        return body;
    }

    private static Map<String, Object> localized(String value) {
        return Map.of("defaultValue", Map.of("language", "it-IT", "value", value == null ? "" : value));
    }

    // ══════════════════════════════════════════════════════════════════════
    // API REST
    // ══════════════════════════════════════════════════════════════════════

    /** Inserisce la risorsa; se esiste già (409) la aggiorna. */
    private void upsert(String resource, String id, Map<String, Object> body) throws IOException, InterruptedException {
        HttpResponse<String> insert = send("POST", API + "/" + resource, body);
        if (insert.statusCode() == 409) {
            HttpResponse<String> update = send("PATCH", API + "/" + resource + "/" + id, body);
            ensureSuccess(resource, update);
        } else {
            ensureSuccess(resource, insert);
        }
    }

    /** Aggiorna la risorsa; una risorsa inesistente (404) non è un errore. */
    private void patch(String resource, String id, Map<String, Object> body) throws IOException, InterruptedException {
        HttpResponse<String> response = send("PATCH", API + "/" + resource + "/" + id, body);
        if (response.statusCode() != 404) {
            ensureSuccess(resource, response);
        }
    }

    private HttpResponse<String> send(String method, String url, Map<String, Object> body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("Authorization", "Bearer " + accessToken())
                .header("Content-Type", "application/json")
                .method(method, HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static void ensureSuccess(String resource, HttpResponse<String> response) throws IOException {
        if (response.statusCode() / 100 != 2) {
            throw new IOException("Google Wallet " + resource + ": HTTP " + response.statusCode() + " - " + response.body());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Autenticazione
    // ══════════════════════════════════════════════════════════════════════

    private ServiceAccountCredentials credentials() throws IOException {
        if (credentials == null) {
            synchronized (this) {
                if (credentials == null) {
                    try (InputStream in = new FileInputStream(serviceAccountFile)) {
                        credentials = (ServiceAccountCredentials) ServiceAccountCredentials.fromStream(in)
                                .createScoped(SCOPE);
                    }
                }
            }
        }
        return credentials;
    }

    private String accessToken() throws IOException {
        ServiceAccountCredentials creds = credentials();
        creds.refreshIfExpired();
        return creds.getAccessToken().getTokenValue();
    }

    /** JWT firmato con la chiave dell'account di servizio, richiesto dal link di salvataggio. */
    private String signSaveJwt(String objectId) throws IOException, GeneralSecurityException {
        ServiceAccountCredentials creds = credentials();

        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("iss", creds.getClientEmail());
        claims.put("aud", "google");
        claims.put("typ", "savetowallet");
        claims.put("iat", Instant.now().getEpochSecond());
        if (!origins.isEmpty()) {
            claims.put("origins", origins);
        }
        claims.put("payload", Map.of("loyaltyObjects", List.of(Map.of("id", objectId))));

        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        String unsigned = encoder.encodeToString(objectMapper.writeValueAsBytes(Map.of("alg", "RS256", "typ", "JWT")))
                + "." + encoder.encodeToString(objectMapper.writeValueAsBytes(claims));

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(creds.getPrivateKey());
        signature.update(unsigned.getBytes(StandardCharsets.UTF_8));
        return unsigned + "." + encoder.encodeToString(signature.sign());
    }
}
