package com.twentyfive.twentyfivedb.fidelity.wallet;

import com.twentyfive.twentyfivedb.fidelity.repository.CardGroupRepository;
import com.twentyfive.twentyfivedb.fidelity.repository.CardRepository;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.models.fidelityModels.Card;
import twentyfive.twentyfiveadapter.models.fidelityModels.CardGroup;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Orchestrazione delle tessere wallet: generazione su richiesta del titolare e
 * sincronizzazione quando la card (o il suo gruppo) cambia nel database.
 * La sincronizzazione avviene in background, su un unico thread per mantenere
 * l'ordine degli aggiornamenti: un errore di Google/Apple non blocca mai la scansione.
 */
@Service
@Slf4j
public class WalletService {

    private final CardRepository cardRepository;
    private final CardGroupRepository cardGroupRepository;
    private final WalletPassRepository walletPassRepository;
    private final AppleWalletRegistrationRepository registrationRepository;
    private final GoogleWalletService googleWalletService;
    private final AppleWalletService appleWalletService;

    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "wallet-sync");
        thread.setDaemon(true);
        return thread;
    });

    public WalletService(CardRepository cardRepository,
                         CardGroupRepository cardGroupRepository,
                         WalletPassRepository walletPassRepository,
                         AppleWalletRegistrationRepository registrationRepository,
                         GoogleWalletService googleWalletService,
                         AppleWalletService appleWalletService) {
        this.cardRepository = cardRepository;
        this.cardGroupRepository = cardGroupRepository;
        this.walletPassRepository = walletPassRepository;
        this.registrationRepository = registrationRepository;
        this.googleWalletService = googleWalletService;
        this.appleWalletService = appleWalletService;
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
    }

    // ══════════════════════════════════════════════════════════════════════
    // Richieste del titolare
    // ══════════════════════════════════════════════════════════════════════

    /** Link "Aggiungi a Google Wallet". Empty se la card non esiste. */
    public Optional<String> googleSaveUrl(String cardId) throws Exception {
        Optional<CardWithGroup> data = load(cardId);
        if (data.isEmpty()) {
            return Optional.empty();
        }
        String url = googleWalletService.createSaveUrl(data.get().card(), data.get().group());
        WalletPass pass = walletPassRepository.findById(cardId).orElseGet(() -> new WalletPass(cardId));
        pass.setGoogleSaved(true);
        walletPassRepository.save(pass);
        return Optional.of(url);
    }

    /** Tessera Apple Wallet (.pkpass). Empty se la card non esiste. */
    public Optional<byte[]> applePass(String cardId) throws Exception {
        Optional<CardWithGroup> data = load(cardId);
        if (data.isEmpty()) {
            return Optional.empty();
        }
        if (!walletPassRepository.existsById(cardId)) {
            walletPassRepository.save(new WalletPass(cardId));
        }
        return Optional.of(appleWalletService.buildPass(data.get().card(), data.get().group()));
    }

    /** Ultimo aggiornamento della tessera (epoch millis), 0 se mai aggiunta a un wallet. */
    public long lastUpdated(String cardId) {
        return walletPassRepository.findById(cardId).map(WalletPass::getUpdatedAt).orElse(0L);
    }

    // ══════════════════════════════════════════════════════════════════════
    // Registrazioni dispositivi Apple
    // ══════════════════════════════════════════════════════════════════════

    /** Registra il dispositivo; true se è una nuova registrazione. */
    public boolean registerAppleDevice(String deviceLibraryIdentifier, String serialNumber, String pushToken) {
        Optional<AppleWalletRegistration> existing = registrationRepository
                .findFirstByDeviceLibraryIdentifierAndSerialNumber(deviceLibraryIdentifier, serialNumber);
        AppleWalletRegistration registration = existing.orElseGet(AppleWalletRegistration::new);
        registration.setDeviceLibraryIdentifier(deviceLibraryIdentifier);
        registration.setSerialNumber(serialNumber);
        registration.setPushToken(pushToken);
        if (registration.getCreatedAt() == null) {
            registration.setCreatedAt(new Date());
        }
        registrationRepository.save(registration);
        return existing.isEmpty();
    }

    public void unregisterAppleDevice(String deviceLibraryIdentifier, String serialNumber) {
        registrationRepository.deleteAllByDeviceLibraryIdentifierAndSerialNumber(deviceLibraryIdentifier, serialNumber);
    }

    /** Tessere registrate sul dispositivo e modificate dopo {@code updatedSince} (null = tutte). */
    public List<WalletPass> applePassesUpdatedSince(String deviceLibraryIdentifier, Long updatedSince) {
        List<String> serials = registrationRepository.findAllByDeviceLibraryIdentifier(deviceLibraryIdentifier).stream()
                .map(AppleWalletRegistration::getSerialNumber)
                .distinct()
                .toList();
        return walletPassRepository.findAllById(serials).stream()
                .filter(pass -> updatedSince == null || pass.getUpdatedAt() > updatedSince)
                .toList();
    }

    // ══════════════════════════════════════════════════════════════════════
    // Sincronizzazione (chiamata dal listener Mongo)
    // ══════════════════════════════════════════════════════════════════════

    public void cardChanged(String cardId) {
        executor.submit(() -> runSafely("card " + cardId, () -> syncCard(cardId)));
    }

    public void groupChanged(String groupId) {
        executor.submit(() -> runSafely("gruppo " + groupId, () -> syncGroup(groupId)));
    }

    private void syncCard(String cardId) throws Exception {
        WalletPass pass = walletPassRepository.findById(cardId).orElse(null);
        if (pass == null) {
            return; // card mai aggiunta a un wallet
        }
        Optional<CardWithGroup> data = load(cardId);
        if (data.isEmpty()) {
            return;
        }

        pass.setUpdatedAt(System.currentTimeMillis());
        walletPassRepository.save(pass);

        if (pass.isGoogleSaved() && googleWalletService.isConfigured()) {
            googleWalletService.updateObject(data.get().card(), data.get().group());
        }

        if (appleWalletService.isConfigured()) {
            List<String> tokens = registrationRepository.findAllBySerialNumber(cardId).stream()
                    .map(AppleWalletRegistration::getPushToken)
                    .distinct()
                    .toList();
            if (!tokens.isEmpty()) {
                appleWalletService.push(tokens).forEach(registrationRepository::deleteAllByPushToken);
            }
        }
    }

    private void syncGroup(String groupId) throws Exception {
        List<String> cardIds = cardRepository.findAllByCardGroupId(groupId).stream().map(Card::getId).toList();
        List<WalletPass> passes = walletPassRepository.findAllById(cardIds);
        if (passes.isEmpty()) {
            return;
        }

        CardGroup group = cardGroupRepository.findById(groupId).orElse(null);
        if (group != null && googleWalletService.isConfigured() && passes.stream().anyMatch(WalletPass::isGoogleSaved)) {
            googleWalletService.updateClass(group);
        }
        // Il traguardo e la scadenza del gruppo compaiono sulle tessere delle card
        for (WalletPass pass : passes) {
            runSafely("card " + pass.getCardId(), () -> syncCard(pass.getCardId()));
        }
    }

    // ══════════════════════════════════════════════════════════════════════

    private record CardWithGroup(Card card, CardGroup group) {
    }

    private Optional<CardWithGroup> load(String cardId) {
        return cardRepository.findById(cardId).flatMap(card ->
                cardGroupRepository.findById(card.getCardGroupId()).map(group -> new CardWithGroup(card, group)));
    }

    private interface Task {
        void run() throws Exception;
    }

    private static void runSafely(String description, Task task) {
        try {
            task.run();
        } catch (Exception e) {
            log.error("Aggiornamento wallet non riuscito per {}", description, e);
        }
    }
}
