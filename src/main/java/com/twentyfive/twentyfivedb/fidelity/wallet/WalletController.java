package com.twentyfive.twentyfivedb.fidelity.wallet;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** Tessere wallet richieste dal titolare della card (pagina card-view) o dalla dashboard. */
@RestController
@RequestMapping("/card/wallet")
@Slf4j
public class WalletController {

    public static final MediaType PKPASS = MediaType.parseMediaType("application/vnd.apple.pkpass");

    private final WalletService walletService;
    private final GoogleWalletService googleWalletService;
    private final AppleWalletService appleWalletService;

    public WalletController(WalletService walletService,
                            GoogleWalletService googleWalletService,
                            AppleWalletService appleWalletService) {
        this.walletService = walletService;
        this.googleWalletService = googleWalletService;
        this.appleWalletService = appleWalletService;
    }

    @GetMapping("/google/{id}")
    public ResponseEntity<Map<String, String>> googleSaveUrl(@PathVariable String id) {
        if (!googleWalletService.isConfigured()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        try {
            return walletService.googleSaveUrl(id)
                    .map(url -> ResponseEntity.ok(Map.of("url", url)))
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Creazione tessera Google Wallet non riuscita per la card {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/apple/{id}")
    public ResponseEntity<byte[]> applePass(@PathVariable String id) {
        if (!appleWalletService.isConfigured()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        try {
            return walletService.applePass(id)
                    .map(pass -> ResponseEntity.ok().contentType(PKPASS).body(pass))
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Creazione tessera Apple Wallet non riuscita per la card {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
