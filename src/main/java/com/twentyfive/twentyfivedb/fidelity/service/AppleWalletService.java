package com.twentyfive.twentyfivedb.fidelity.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twentyfive.twentyfivedb.fidelity.exceptions.WalletNotConfiguredException;
import com.twentyfive.twentyfivedb.fidelity.repository.CardGroupRepository;
import com.twentyfive.twentyfivedb.fidelity.repository.CardRepository;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.models.fidelityModels.Card;
import twentyfive.twentyfiveadapter.models.fidelityModels.CardGroup;

import jakarta.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.*;
import java.security.cert.*;
import java.util.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class AppleWalletService {

    @Value("${wallet.apple.pass-type-id:PLACEHOLDER}")
    private String passTypeId;

    @Value("${wallet.apple.team-id:PLACEHOLDER}")
    private String teamId;

    @Value("${wallet.apple.keystore-path:PLACEHOLDER}")
    private String keystorePath;

    @Value("${wallet.apple.keystore-password:PLACEHOLDER}")
    private String keystorePassword;

    @Value("${wallet.apple.wwdrca-path:PLACEHOLDER}")
    private String wwdrcaPath;

    private final CardRepository cardRepository;
    private final CardGroupRepository cardGroupRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AppleWalletService(CardRepository cardRepository, CardGroupRepository cardGroupRepository) {
        this.cardRepository = cardRepository;
        this.cardGroupRepository = cardGroupRepository;
    }

    @PostConstruct
    public void init() {
        Security.addProvider(new BouncyCastleProvider());
    }

    public byte[] generatePass(String cardId) throws Exception {
        if ("PLACEHOLDER".equals(passTypeId) || "PLACEHOLDER".equals(teamId)) {
            throw new WalletNotConfiguredException("Apple Wallet credentials not configured");
        }

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found: " + cardId));
        CardGroup group = cardGroupRepository.findById(card.getCardGroupId())
                .orElseThrow(() -> new RuntimeException("CardGroup not found"));

        Map<String, byte[]> files = buildPassFiles(card, group);

        Map<String, String> manifest = new LinkedHashMap<>();
        for (Map.Entry<String, byte[]> entry : files.entrySet()) {
            manifest.put(entry.getKey(), sha1Hex(entry.getValue()));
        }
        byte[] manifestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(manifest);
        byte[] signatureBytes = signManifest(manifestBytes);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (Map.Entry<String, byte[]> entry : files.entrySet()) {
                zos.putNextEntry(new ZipEntry(entry.getKey()));
                zos.write(entry.getValue());
                zos.closeEntry();
            }
            zos.putNextEntry(new ZipEntry("manifest.json"));
            zos.write(manifestBytes);
            zos.closeEntry();
            zos.putNextEntry(new ZipEntry("signature"));
            zos.write(signatureBytes);
            zos.closeEntry();
        }
        return baos.toByteArray();
    }

    private Map<String, byte[]> buildPassFiles(Card card, CardGroup group) throws Exception {
        Map<String, byte[]> files = new LinkedHashMap<>();
        files.put("pass.json", objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsBytes(buildPassJson(card, group)));
        files.put("icon.png", generatePlaceholderPng(29, 29));
        files.put("icon@2x.png", generatePlaceholderPng(58, 58));
        files.put("logo.png", generatePlaceholderPng(160, 50));
        files.put("logo@2x.png", generatePlaceholderPng(320, 100));
        return files;
    }

    private Map<String, Object> buildPassJson(Card card, CardGroup group) {
        boolean isVoucher = "voucher".equals(card.getType());

        Map<String, Object> pass = new LinkedHashMap<>();
        pass.put("formatVersion", 1);
        pass.put("passTypeIdentifier", passTypeId);
        pass.put("serialNumber", card.getId());
        pass.put("teamIdentifier", teamId);
        pass.put("organizationName", group.getName() != null ? group.getName() : "Fidelity");
        pass.put("description", isVoucher ? "Voucher" : "Carta fedeltà");
        pass.put("backgroundColor", "rgb(26,22,96)");
        pass.put("foregroundColor", "rgb(255,255,255)");
        pass.put("labelColor", "rgb(200,200,220)");
        pass.put("logoText", group.getName() != null ? group.getName() : "Fidelity");

        Map<String, Object> barcode = new LinkedHashMap<>();
        barcode.put("message", card.getCardCode() != null ? card.getCardCode() : card.getId());
        barcode.put("format", "PKBarcodeFormatQR");
        barcode.put("messageEncoding", "iso-8859-1");
        pass.put("barcode", barcode);

        List<Map<String, Object>> primaryFields = new ArrayList<>();
        List<Map<String, Object>> secondaryFields = new ArrayList<>();
        List<Map<String, Object>> backFields = new ArrayList<>();

        if (isVoucher) {
            primaryFields.add(field("balance", "Saldo",
                    (card.getVoucherAmount() != null ? card.getVoucherAmount() : 0) + " pt"));
        } else {
            primaryFields.add(field("scans", "Scansioni",
                    card.getScanNumberExecuted() + " / " + group.getScanNumber()));
        }

        secondaryFields.add(field("holder", "Titolare",
                trimToEmpty(card.getName()) + " " + trimToEmpty(card.getSurname())));

        backFields.add(field("code", "Codice",
                card.getCardCode() != null ? card.getCardCode() : card.getId()));

        Map<String, Object> storeCard = new LinkedHashMap<>();
        storeCard.put("primaryFields", primaryFields);
        storeCard.put("secondaryFields", secondaryFields);
        storeCard.put("auxiliaryFields", Collections.emptyList());
        storeCard.put("backFields", backFields);
        pass.put("storeCard", storeCard);

        return pass;
    }

    private Map<String, Object> field(String key, String label, Object value) {
        Map<String, Object> f = new LinkedHashMap<>();
        f.put("key", key);
        f.put("label", label);
        f.put("value", value);
        return f;
    }

    private String trimToEmpty(String s) {
        return s != null ? s.trim() : "";
    }

    private String sha1Hex(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] hash = md.digest(data);
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private byte[] signManifest(byte[] manifestBytes) throws Exception {
        KeyStore ks = KeyStore.getInstance("PKCS12", "BC");
        String ksPath = keystorePath.replace("classpath:", "");
        try (InputStream ksStream = new ClassPathResource(ksPath).getInputStream()) {
            ks.load(ksStream, keystorePassword.toCharArray());
        }
        String alias = ks.aliases().nextElement();
        PrivateKey privateKey = (PrivateKey) ks.getKey(alias, keystorePassword.toCharArray());
        X509Certificate signerCert = (X509Certificate) ks.getCertificate(alias);

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        String wwdrcaLocalPath = wwdrcaPath.replace("classpath:", "");
        X509Certificate wwdrcaCert;
        try (InputStream wwdrcaStream = new ClassPathResource(wwdrcaLocalPath).getInputStream()) {
            wwdrcaCert = (X509Certificate) cf.generateCertificate(wwdrcaStream);
        }

        JcaCertStore certStore = new JcaCertStore(Arrays.asList(signerCert, wwdrcaCert));
        CMSTypedData msg = new CMSProcessableByteArray(manifestBytes);
        CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
        DigestCalculatorProvider dcp = new JcaDigestCalculatorProviderBuilder().setProvider("BC").build();
        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").setProvider("BC").build(privateKey);
        gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(dcp).build(signer, signerCert));
        gen.addCertificates(certStore);
        CMSSignedData signedData = gen.generate(msg, false);
        return signedData.getEncoded();
    }

    private byte[] generatePlaceholderPng(int width, int height) throws IOException {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(26, 22, 96));
        g.fillRect(0, 0, width, height);
        g.dispose();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "PNG", baos);
        return baos.toByteArray();
    }
}
