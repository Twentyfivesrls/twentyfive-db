package com.twentyfive.twentyfivedb.fidelity.wallet;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * Registrazione di un dispositivo Apple agli aggiornamenti di una tessera:
 * l'iPhone la crea quando la tessera viene aggiunta al Wallet e la rimuove quando viene eliminata.
 */
@Data
@NoArgsConstructor
@Document("fidelity_wallet_apple_registration")
public class AppleWalletRegistration {

    @Id
    private String id;

    private String deviceLibraryIdentifier;

    /** Token APNs del dispositivo a cui inviare la notifica di aggiornamento. */
    private String pushToken;

    /** serialNumber della tessera = id della card. */
    private String serialNumber;

    private Date createdAt;
}
