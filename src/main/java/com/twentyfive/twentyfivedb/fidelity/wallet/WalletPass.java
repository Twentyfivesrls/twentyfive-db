package com.twentyfive.twentyfivedb.fidelity.wallet;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Stato della tessera wallet di una card: esiste solo per le card che il titolare
 * ha aggiunto (o provato ad aggiungere) a Google Wallet o Apple Wallet, così gli
 * aggiornamenti vengono inviati solo dove servono.
 */
@Data
@NoArgsConstructor
@Document("fidelity_wallet_pass")
public class WalletPass {

    /** Id della card (è anche il serialNumber della tessera Apple). */
    @Id
    private String cardId;

    /** True se l'oggetto Google Wallet è stato creato. */
    private boolean googleSaved;

    /** Ultima modifica della card (epoch millis): usata da Apple per sapere quali tessere scaricare. */
    private long updatedAt;

    public WalletPass(String cardId) {
        this.cardId = cardId;
        this.updatedAt = System.currentTimeMillis();
    }
}
