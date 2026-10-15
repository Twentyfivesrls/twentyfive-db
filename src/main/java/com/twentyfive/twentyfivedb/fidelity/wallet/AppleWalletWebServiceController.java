package com.twentyfive.twentyfivedb.fidelity.wallet;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Web service di aggiornamento delle tessere, con i percorsi fissati da Apple
 * ("Wallet Passes Web Service Reference"). L'iPhone lo chiama tramite fidelity-be,
 * che lo espone su HTTPS all'indirizzo indicato in webServiceURL.
 */
@RestController
@RequestMapping("/wallet/apple/v1")
@Slf4j
public class AppleWalletWebServiceController {

    private final WalletService walletService;
    private final AppleWalletService appleWalletService;

    public AppleWalletWebServiceController(WalletService walletService, AppleWalletService appleWalletService) {
        this.walletService = walletService;
        this.appleWalletService = appleWalletService;
    }

    /** Il dispositivo si registra per ricevere gli aggiornamenti della tessera. */
    @PostMapping("/devices/{deviceId}/registrations/{passTypeId}/{serialNumber}")
    public ResponseEntity<Void> register(@PathVariable String deviceId,
                                         @PathVariable String passTypeId,
                                         @PathVariable String serialNumber,
                                         @RequestHeader(value = "Authorization", required = false) String authorization,
                                         @RequestBody Map<String, String> body) {
        if (!isKnownPass(passTypeId, serialNumber)) {
            return ResponseEntity.notFound().build();
        }
        if (!appleWalletService.isAuthorized(authorization, serialNumber)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        boolean created = walletService.registerAppleDevice(deviceId, serialNumber, body.get("pushToken"));
        return ResponseEntity.status(created ? HttpStatus.CREATED : HttpStatus.OK).build();
    }

    /** Elenco delle tessere del dispositivo modificate dopo l'ultimo aggiornamento. */
    @GetMapping("/devices/{deviceId}/registrations/{passTypeId}")
    public ResponseEntity<Map<String, Object>> updatedPasses(@PathVariable String deviceId,
                                                             @PathVariable String passTypeId,
                                                             @RequestParam(value = "passesUpdatedSince", required = false) String since) {
        if (!appleWalletService.getPassTypeId().equals(passTypeId)) {
            return ResponseEntity.notFound().build();
        }
        Long updatedSince = parseTag(since);
        List<WalletPass> passes = walletService.applePassesUpdatedSince(deviceId, updatedSince);
        if (passes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        long lastUpdated = passes.stream().mapToLong(WalletPass::getUpdatedAt).max().orElse(0);
        return ResponseEntity.ok(Map.of(
                "serialNumbers", passes.stream().map(WalletPass::getCardId).toList(),
                "lastUpdated", String.valueOf(lastUpdated)));
    }

    /** Il dispositivo non vuole più aggiornamenti (tessera rimossa dal Wallet). */
    @DeleteMapping("/devices/{deviceId}/registrations/{passTypeId}/{serialNumber}")
    public ResponseEntity<Void> unregister(@PathVariable String deviceId,
                                           @PathVariable String passTypeId,
                                           @PathVariable String serialNumber,
                                           @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (!appleWalletService.getPassTypeId().equals(passTypeId)) {
            return ResponseEntity.notFound().build();
        }
        if (!appleWalletService.isAuthorized(authorization, serialNumber)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        walletService.unregisterAppleDevice(deviceId, serialNumber);
        return ResponseEntity.ok().build();
    }

    /** Versione aggiornata della tessera. */
    @GetMapping("/passes/{passTypeId}/{serialNumber}")
    public ResponseEntity<byte[]> latestPass(@PathVariable String passTypeId,
                                             @PathVariable String serialNumber,
                                             @RequestHeader(value = "Authorization", required = false) String authorization,
                                             HttpServletRequest request) {
        if (!isKnownPass(passTypeId, serialNumber)) {
            return ResponseEntity.notFound().build();
        }
        if (!appleWalletService.isAuthorized(authorization, serialNumber)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        long lastUpdated = walletService.lastUpdated(serialNumber);
        long ifModifiedSince = request.getDateHeader("If-Modified-Since");
        // Le date HTTP hanno la precisione del secondo
        if (ifModifiedSince > 0 && lastUpdated / 1000 <= ifModifiedSince / 1000) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
        }
        try {
            return walletService.applePass(serialNumber)
                    .map(pass -> ResponseEntity.ok()
                            .contentType(WalletController.PKPASS)
                            .lastModified(lastUpdated)
                            .body(pass))
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Generazione tessera Apple aggiornata non riuscita per {}", serialNumber, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /** Errori segnalati dai dispositivi. */
    @PostMapping("/log")
    public ResponseEntity<Void> deviceLog(@RequestBody Map<String, List<String>> body) {
        body.getOrDefault("logs", List.of()).forEach(message -> log.warn("Apple Wallet (dispositivo): {}", message));
        return ResponseEntity.ok().build();
    }

    private boolean isKnownPass(String passTypeId, String serialNumber) {
        return appleWalletService.getPassTypeId().equals(passTypeId) && walletService.lastUpdated(serialNumber) > 0;
    }

    private static Long parseTag(String tag) {
        try {
            return tag == null || tag.isBlank() ? null : Long.parseLong(tag);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
