package com.twentyfive.twentyfivedb.fidelity.wallet;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import twentyfive.twentyfiveadapter.models.fidelityModels.Card;
import twentyfive.twentyfiveadapter.models.fidelityModels.CardGroup;
import twentyfive.twentyfiveadapter.models.fidelityModels.ProfileFidelity;

/**
 * Ogni salvataggio di una card o di un gruppo (scansioni, premi, saldo voucher,
 * stato, modifica dati, immagine del gruppo) aggiorna le tessere wallet collegate.
 */
@Configuration
public class WalletMongoListeners {

    @Bean
    public AbstractMongoEventListener<Card> walletCardListener(WalletService walletService) {
        // Tipo esplicito: Spring lo usa per filtrare gli eventi per entità
        return new AbstractMongoEventListener<Card>() {
            @Override
            public void onAfterSave(AfterSaveEvent<Card> event) {
                walletService.cardChanged(event.getSource().getId());
            }
        };
    }

    @Bean
    public AbstractMongoEventListener<CardGroup> walletCardGroupListener(WalletService walletService) {
        return new AbstractMongoEventListener<CardGroup>() {
            @Override
            public void onAfterSave(AfterSaveEvent<CardGroup> event) {
                walletService.groupChanged(event.getSource().getId());
            }
        };
    }

    /** Nuova immagine del gruppo (ownerId = id del gruppo; per i profili utente non trova card). */
    @Bean
    public AbstractMongoEventListener<ProfileFidelity> walletGroupImageListener(WalletService walletService) {
        return new AbstractMongoEventListener<ProfileFidelity>() {
            @Override
            public void onAfterSave(AfterSaveEvent<ProfileFidelity> event) {
                if (event.getSource().getOwnerId() != null) {
                    walletService.groupChanged(event.getSource().getOwnerId());
                }
            }
        };
    }
}
