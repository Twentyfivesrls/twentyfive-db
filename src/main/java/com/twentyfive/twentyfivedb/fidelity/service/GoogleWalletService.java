package com.twentyfive.twentyfivedb.fidelity.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.twentyfive.twentyfivedb.fidelity.exceptions.WalletNotConfiguredException;
import com.twentyfive.twentyfivedb.fidelity.repository.CardGroupRepository;
import com.twentyfive.twentyfivedb.fidelity.repository.CardRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.models.fidelityModels.Card;
import twentyfive.twentyfiveadapter.models.fidelityModels.CardGroup;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;

@Service
public class GoogleWalletService {

    @Value("${wallet.google.issuer-id:PLACEHOLDER}")
    private String issuerId;

    @Value("${wallet.google.class-id:fidelity_loyalty}")
    private String classId;

    @Value("${wallet.google.service-account-email:PLACEHOLDER}")
    private String serviceAccountEmail;

    @Value("${wallet.google.private-key:PLACEHOLDER}")
    private String privateKey;

    private final CardRepository cardRepository;
    private final CardGroupRepository cardGroupRepository;

    public GoogleWalletService(CardRepository cardRepository, CardGroupRepository cardGroupRepository) {
        this.cardRepository = cardRepository;
        this.cardGroupRepository = cardGroupRepository;
    }

    public String generateSaveUrl(String cardId) throws Exception {
        if ("PLACEHOLDER".equals(issuerId) || "PLACEHOLDER".equals(serviceAccountEmail)) {
            throw new WalletNotConfiguredException("Google Wallet credentials not configured");
        }

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found: " + cardId));
        CardGroup group = cardGroupRepository.findById(card.getCardGroupId())
                .orElseThrow(() -> new RuntimeException("CardGroup not found"));

        boolean isVoucher = "voucher".equals(card.getType());
        Map<String, Object> loyaltyObject = buildLoyaltyObject(card, group, isVoucher);

        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("iss", serviceAccountEmail);
        claims.put("aud", "google");
        claims.put("typ", "savetowallet");
        claims.put("iat", System.currentTimeMillis() / 1000L);
        claims.put("payload", Map.of("loyaltyObjects", Collections.singletonList(loyaltyObject)));

        RSAPrivateKey rsaKey = loadPrivateKey(privateKey);
        Algorithm algorithm = Algorithm.RSA256(null, rsaKey);

        String jwt = JWT.create()
                .withPayload(claims)
                .sign(algorithm);

        return "https://pay.google.com/gp/v/save/" + jwt;
    }

    private Map<String, Object> buildLoyaltyObject(Card card, CardGroup group, boolean isVoucher) {
        String objectClassId = issuerId + "." + classId + (isVoucher ? "_voucher" : "");

        Map<String, Object> obj = new LinkedHashMap<>();
        obj.put("id", issuerId + "." + card.getId());
        obj.put("classId", objectClassId);
        obj.put("state", Boolean.TRUE.equals(card.isActive) ? "ACTIVE" : "INACTIVE");
        obj.put("accountId", card.getCardCode() != null ? card.getCardCode() : card.getId());
        obj.put("accountName", trimToEmpty(card.getName()) + " " + trimToEmpty(card.getSurname()));

        Map<String, Object> loyaltyPoints = new LinkedHashMap<>();
        loyaltyPoints.put("label", isVoucher ? "Saldo punti" : "Scansioni");
        Map<String, Object> balance = new LinkedHashMap<>();
        if (isVoucher) {
            balance.put("int", card.getVoucherAmount() != null ? card.getVoucherAmount() : 0);
        } else {
            balance.put("string", card.getScanNumberExecuted() + "/" + group.getScanNumber());
        }
        loyaltyPoints.put("balance", balance);
        obj.put("loyaltyPoints", loyaltyPoints);

        Map<String, Object> barcode = new LinkedHashMap<>();
        barcode.put("type", "QR_CODE");
        barcode.put("value", card.getCardCode() != null ? card.getCardCode() : card.getId());
        obj.put("barcode", barcode);

        return obj;
    }

    private RSAPrivateKey loadPrivateKey(String pem) throws Exception {
        String cleaned = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] decoded = Base64.getDecoder().decode(cleaned);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    private String trimToEmpty(String s) {
        return s != null ? s.trim() : "";
    }
}
