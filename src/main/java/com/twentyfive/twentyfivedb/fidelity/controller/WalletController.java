package com.twentyfive.twentyfivedb.fidelity.controller;

import com.twentyfive.twentyfivedb.fidelity.exceptions.WalletNotConfiguredException;
import com.twentyfive.twentyfivedb.fidelity.service.AppleWalletService;
import com.twentyfive.twentyfivedb.fidelity.service.GoogleWalletService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/card")
public class WalletController {

    private final AppleWalletService appleWalletService;
    private final GoogleWalletService googleWalletService;

    public WalletController(AppleWalletService appleWalletService, GoogleWalletService googleWalletService) {
        this.appleWalletService = appleWalletService;
        this.googleWalletService = googleWalletService;
    }

    @GetMapping("/wallet/apple/{id}")
    public ResponseEntity<byte[]> appleWalletPass(@PathVariable String id) {
        try {
            byte[] pass = appleWalletService.generatePass(id);
            String filename = id + ".pkpass";
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.apple.pkpass"))
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .body(pass);
        } catch (WalletNotConfiguredException e) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
        } catch (Exception e) {
            System.err.println("Apple Wallet error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/wallet/google/{id}")
    public ResponseEntity<Map<String, String>> googleWalletUrl(@PathVariable String id) {
        try {
            String url = googleWalletService.generateSaveUrl(id);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (WalletNotConfiguredException e) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
        } catch (Exception e) {
            System.err.println("Google Wallet error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
