package com.twentyfive.twentyfivedb.fidelity.wallet;

import com.twentyfive.twentyfivedb.fidelity.repository.FidelityProfileRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.models.fidelityModels.ProfileFidelity;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Immagini delle tessere wallet: logo del gruppo (l'immagine caricata dalla dashboard)
 * su un badge bianco, così resta leggibile sullo sfondo scuro Fidelity anche se scura
 * o trasparente. Senza immagine si usa il marchio Fidelity, nello stesso badge.
 *
 * Nessuna immagine viene letta all'avvio: un problema di ImageIO non deve impedire
 * l'avvio dell'applicazione. Se l'elaborazione fallisce si usano immagini pre-generate.
 */
@Service
@Slf4j
public class WalletImageService {

    /** Colore brand Fidelity (#1A1660). */
    public static final Color BRAND = new Color(26, 22, 96);

    private static final int GOOGLE_LOGO_SIZE = 660;
    private static final int CACHE_SIZE = 64;

    private final String groupImageBaseUrl;
    private final String publicBaseUrl;
    private final FidelityProfileRepository profileRepository;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    private volatile BufferedImage fidelityMark;

    /** Immagini dei gruppi già scaricate, per URL (l'URL cambia quando l'immagine viene sostituita). */
    private final Map<String, BufferedImage> cache = Collections.synchronizedMap(new LinkedHashMap<>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, BufferedImage> eldest) {
            return size() > CACHE_SIZE;
        }
    });

    public WalletImageService(@Value("${wallet.group-image-base-url:}") String groupImageBaseUrl,
                              @Value("${wallet.public-base-url:}") String publicBaseUrl,
                              FidelityProfileRepository profileRepository) {
        this.groupImageBaseUrl = groupImageBaseUrl;
        this.publicBaseUrl = publicBaseUrl;
        this.profileRepository = profileRepository;
    }

    // ══════════════════════════════════════════════════════════════════════
    // Google
    // ══════════════════════════════════════════════════════════════════════

    public boolean hasPublicUrl() {
        return StringUtils.isNotBlank(publicBaseUrl);
    }

    /**
     * URL pubblico del logo del gruppo, servito da {@link WalletController}.
     * Il parametro v cambia con l'immagine, così Google scarica quella nuova.
     */
    public String googleLogoUrl(String groupId) {
        String version = groupImageName(groupId).orElse("default");
        return publicBaseUrl + "/card/wallet/image/group/" + groupId + "/logo.png?v="
                + URLEncoder.encode(version, StandardCharsets.UTF_8);
    }

    /** Logo quadrato per Google: Google lo ritaglia a cerchio, quindi il contenuto sta al centro. */
    public byte[] googleLogo(String groupId) throws IOException {
        return withFallback("logo Google del gruppo " + groupId,
                () -> renderGoogleLogo(groupId),
                () -> fallbackResource("google-logo.png"));
    }

    private byte[] renderGoogleLogo(String groupId) throws IOException {
        BufferedImage source = groupImage(groupId).orElse(fidelityMark());
        int size = GOOGLE_LOGO_SIZE;
        BufferedImage canvas = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = canvas.createGraphics();
        quality(g);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, size, size);
        BufferedImage fitted = fit(source, (int) (size * 0.68), (int) (size * 0.68));
        g.drawImage(fitted, (size - fitted.getWidth()) / 2, (size - fitted.getHeight()) / 2, null);
        g.dispose();
        return png(canvas);
    }

    // ══════════════════════════════════════════════════════════════════════
    // Apple
    // ══════════════════════════════════════════════════════════════════════

    /** Icone e logo della tessera Apple alle tre densità (@1x, @2x, @3x). */
    public Map<String, byte[]> appleImages(String groupId) throws IOException {
        return withFallback("immagini Apple del gruppo " + groupId,
                () -> renderAppleImages(groupId),
                this::fallbackAppleImages);
    }

    private Map<String, byte[]> renderAppleImages(String groupId) throws IOException {
        BufferedImage source = groupImage(groupId).orElse(fidelityMark());
        Map<String, byte[]> images = new LinkedHashMap<>();
        for (int scale = 1; scale <= 3; scale++) {
            String suffix = scale == 1 ? "" : "@" + scale + "x";
            images.put("icon" + suffix + ".png", png(appleIcon(29 * scale)));
            images.put("logo" + suffix + ".png", png(badge(source, 160 * scale, 50 * scale, scale)));
        }
        return images;
    }

    private Map<String, byte[]> fallbackAppleImages() throws IOException {
        Map<String, byte[]> images = new LinkedHashMap<>();
        for (String suffix : new String[]{"", "@2x", "@3x"}) {
            images.put("icon" + suffix + ".png", fallbackResource("icon" + suffix + ".png"));
            images.put("logo" + suffix + ".png", fallbackResource("logo" + suffix + ".png"));
        }
        return images;
    }

    /** Icona delle notifiche: marchio Fidelity su fondo bianco. */
    private BufferedImage appleIcon(int size) throws IOException {
        BufferedImage canvas = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = canvas.createGraphics();
        quality(g);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, size, size);
        BufferedImage fitted = fit(fidelityMark(), (int) (size * 0.8), (int) (size * 0.8));
        g.drawImage(fitted, (size - fitted.getWidth()) / 2, (size - fitted.getHeight()) / 2, null);
        g.dispose();
        return canvas;
    }

    /** Immagine dentro un badge bianco arrotondato, entro maxW × maxH. */
    private static BufferedImage badge(BufferedImage source, int maxW, int maxH, int scale) {
        int padding = 4 * scale;
        BufferedImage fitted = fit(source, maxW - 2 * padding, maxH - 2 * padding);
        int w = fitted.getWidth() + 2 * padding;
        int h = fitted.getHeight() + 2 * padding;
        BufferedImage canvas = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = canvas.createGraphics();
        quality(g);
        g.setColor(Color.WHITE);
        g.fill(new RoundRectangle2D.Float(0, 0, w, h, 10f * scale, 10f * scale));
        g.drawImage(fitted, padding, padding, null);
        g.dispose();
        return canvas;
    }

    // ══════════════════════════════════════════════════════════════════════
    // Immagine del gruppo
    // ══════════════════════════════════════════════════════════════════════

    public Optional<String> groupImageName(String groupId) {
        ProfileFidelity profile = profileRepository.findByOwnerId(groupId);
        return Optional.ofNullable(profile).map(ProfileFidelity::getImageName).filter(StringUtils::isNotBlank);
    }

    /** Immagine caricata per il gruppo; vuota se assente o non leggibile (PNG/JPEG). */
    private Optional<BufferedImage> groupImage(String groupId) {
        Optional<String> name = groupImageName(groupId);
        if (name.isEmpty() || StringUtils.isBlank(groupImageBaseUrl)) {
            return Optional.empty();
        }
        String url = groupImageBaseUrl + groupId + "/" + URLEncoder.encode(name.get(), StandardCharsets.UTF_8).replace("+", "%20");
        BufferedImage cached = cache.get(url);
        if (cached != null) {
            return Optional.of(cached);
        }
        try {
            HttpResponse<byte[]> response = http.send(
                    HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(15)).build(),
                    HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200) {
                log.warn("Immagine gruppo {} non disponibile: HTTP {}", groupId, response.statusCode());
                return Optional.empty();
            }
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(response.body()));
            if (image == null) {
                log.warn("Immagine gruppo {} in un formato non supportato", groupId);
                return Optional.empty();
            }
            cache.put(url, image);
            return Optional.of(image);
        } catch (IOException e) {
            log.warn("Download immagine gruppo {} non riuscito: {}", groupId, e.getMessage());
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Resilienza ImageIO
    // ══════════════════════════════════════════════════════════════════════

    private interface ImageTask<T> {
        T run() throws IOException;
    }

    /**
     * Esegue l'elaborazione; se ImageIO fallisce (es. plugin di un'applicazione già fermata
     * nello stesso Tomcat) ripulisce il registro e riprova, poi ripiega sulle immagini pre-generate.
     */
    private static <T> T withFallback(String description, ImageTask<T> task, ImageTask<T> fallback) throws IOException {
        try {
            return task.run();
        } catch (Exception | LinkageError first) {
            log.warn("Elaborazione {} non riuscita, nuovo tentativo dopo la pulizia di ImageIO: {}", description, first.toString());
            ImageIoProviderCleanup.removeStaleProviders();
            try {
                return task.run();
            } catch (Exception | LinkageError second) {
                log.error("Elaborazione {} non riuscita, uso le immagini di riserva", description, second);
                return fallback.run();
            }
        }
    }

    private BufferedImage fidelityMark() throws IOException {
        if (fidelityMark == null) {
            fidelityMark = readResource("wallet/fidelity-mark.png");
        }
        return fidelityMark;
    }

    /** Immagini pre-generate: lette come byte, senza passare da ImageIO. */
    private static byte[] fallbackResource(String name) throws IOException {
        try (InputStream in = new ClassPathResource("wallet/fallback/" + name).getInputStream()) {
            return in.readAllBytes();
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Utilità
    // ══════════════════════════════════════════════════════════════════════

    /** Ridimensiona mantenendo le proporzioni; riduzioni forti a passi per evitare scalettature. */
    private static BufferedImage fit(BufferedImage source, int maxW, int maxH) {
        double ratio = Math.min((double) maxW / source.getWidth(), (double) maxH / source.getHeight());
        int targetW = Math.max(1, (int) Math.round(source.getWidth() * ratio));
        int targetH = Math.max(1, (int) Math.round(source.getHeight() * ratio));

        BufferedImage current = source;
        while (current.getWidth() / 2 >= targetW && current.getHeight() / 2 >= targetH) {
            current = resize(current, current.getWidth() / 2, current.getHeight() / 2);
        }
        return resize(current, targetW, targetH);
    }

    private static BufferedImage resize(BufferedImage source, int w, int h) {
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        quality(g);
        g.drawImage(source, 0, 0, w, h, null);
        g.dispose();
        return out;
    }

    private static void quality(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    private static byte[] png(BufferedImage image) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return out.toByteArray();
    }

    private static BufferedImage readResource(String path) throws IOException {
        try (InputStream in = new ClassPathResource(path).getInputStream()) {
            return ImageIO.read(in);
        }
    }
}
