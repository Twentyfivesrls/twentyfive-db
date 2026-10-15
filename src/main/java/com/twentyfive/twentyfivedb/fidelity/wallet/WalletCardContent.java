package com.twentyfive.twentyfivedb.fidelity.wallet;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import twentyfive.twentyfiveadapter.models.fidelityModels.Card;
import twentyfive.twentyfiveadapter.models.fidelityModels.CardGroup;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.Locale;

/**
 * Contenuti mostrati sulla tessera wallet, comuni a Google e Apple:
 * titolare, codice, raccolta scansioni (o saldo voucher) e QR code.
 */
@Component
public class WalletCardContent {

    /** Dati della tessera già pronti per essere mostrati. */
    public record Data(String cardId,
                       String holder,
                       String code,
                       String groupName,
                       boolean voucher,
                       String balanceLabel,
                       String balanceValue,
                       String qrValue,
                       boolean active,
                       Date expirationDate) {
    }

    private final String fidelityBaseUrl;
    private final String issuerName;

    public WalletCardContent(@Value("${fidelity.base.url}") String fidelityBaseUrl,
                             @Value("${wallet.issuer-name:Fidelity}") String issuerName) {
        this.fidelityBaseUrl = fidelityBaseUrl;
        this.issuerName = issuerName;
    }

    public String getIssuerName() {
        return issuerName;
    }

    public Data build(Card card, CardGroup group) {
        boolean voucher = "voucher".equals(card.getType());
        String holder = ((card.getName() == null ? "" : card.getName()) + " "
                + (card.getSurname() == null ? "" : card.getSurname())).trim();
        String code = card.getCardCode() != null ? card.getCardCode() : card.getId();

        String balanceLabel = voucher ? "Saldo" : "Scansioni";
        String balanceValue = voucher
                ? formatPoints(card.getVoucherAmount()) + " pt"
                : card.getScanNumberExecuted() + " / " + group.getScanNumber();

        return new Data(
                card.getId(),
                holder,
                code,
                group.getName(),
                voucher,
                balanceLabel,
                balanceValue,
                // Stesso contenuto del QR generato per la card: lo scanner esistente lo riconosce
                fidelityBaseUrl + "card-view/" + card.getId(),
                Boolean.TRUE.equals(card.getIsActive()),
                voucher ? null : group.getExpirationDate()
        );
    }

    private static String formatPoints(Double amount) {
        DecimalFormat format = new DecimalFormat("#,##0.##", DecimalFormatSymbols.getInstance(Locale.ITALY));
        return format.format(amount == null ? 0 : amount);
    }
}
