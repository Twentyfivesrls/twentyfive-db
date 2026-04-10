# Wallet Integration — Setup Guide

Guida completa per abilitare Apple Wallet e Google Wallet nell'applicazione Fidelity.

---

## Struttura dei file da creare

```
src/main/resources/
└── certs/
    ├── apple-wallet.p12        ← certificato Pass Type ID (da generare)
    └── AppleWWDRCAG3.cer       ← certificato intermedio Apple (download pubblico)
```

---

## Apple Wallet

### Prerequisiti
- Apple Developer account attivo (99$/anno)
- Mac con Keychain Access

### Passaggi

#### 1. Creare il Pass Type ID
1. Accedi a [developer.apple.com](https://developer.apple.com)
2. Vai in **Certificates, Identifiers & Profiles → Identifiers**
3. Clicca **+** e seleziona **Pass Type IDs**
4. Inserisci una descrizione e un identificatore nel formato `pass.com.nomeazienda.fidelity`
5. Clicca **Register**
6. Annota il valore dell'identificatore → sarà `wallet.apple.pass-type-id`

#### 2. Trovare il Team ID
1. In alto a destra clicca sul nome dell'account → **Membership**
2. Annota il **Team ID** (stringa di 10 caratteri) → sarà `wallet.apple.team-id`

#### 3. Generare il certificato Pass Type ID
1. Sempre in **Certificates, Identifiers & Profiles → Certificates**
2. Clicca **+** e seleziona **Pass Type ID Certificate**
3. Seleziona il Pass Type ID creato al passo 1
4. Genera una **Certificate Signing Request (CSR)** tramite Keychain Access:
   - Apri Keychain Access → Menu **Keychain Access → Certificate Assistant → Request a Certificate from a CA**
   - Inserisci email e nome, seleziona **Saved to disk**
5. Carica il file `.certSigningRequest` su Apple Developer e scarica il `.cer` generato
6. Fai doppio clic sul `.cer` per importarlo in Keychain Access

#### 4. Esportare il certificato come .p12
1. Apri **Keychain Access → I miei certificati**
2. Trova il certificato `Pass Type ID: pass.com.nomeazienda.fidelity`
3. Tasto destro → **Esporta**
4. Seleziona formato **Personal Information Exchange (.p12)**
5. Scegli una password sicura → sarà `wallet.apple.keystore-password`
6. Salva il file come `apple-wallet.p12`
7. Copia il file in `src/main/resources/certs/apple-wallet.p12`

#### 5. Scaricare il certificato intermedio Apple (WWDRCA)
1. Vai su [apple.com/certificateauthority](https://www.apple.com/certificateauthority/)
2. Nella sezione **Apple Intermediate Certificates** scarica **Worldwide Developer Relations — G3**
3. Il file si chiama `AppleWWDRCAG3.cer`
4. Copialo in `src/main/resources/certs/AppleWWDRCAG3.cer`

#### 6. Configurare le properties
Modifica `application.properties` (e `application-prod.properties`):
```properties
wallet.apple.pass-type-id=pass.com.nomeazienda.fidelity
wallet.apple.team-id=ABCDE12345
wallet.apple.keystore-path=classpath:certs/apple-wallet.p12
wallet.apple.keystore-password=la-password-scelta
wallet.apple.wwdrca-path=classpath:certs/AppleWWDRCAG3.cer
```

---

## Google Wallet

### Prerequisiti
- Account Google Cloud ([console.cloud.google.com](https://console.cloud.google.com))
- Account Google Pay & Wallet Console ([pay.google.com/business/console](https://pay.google.com/business/console))

### Passaggi

#### 1. Abilitare la Google Wallet API
1. Vai su [Google Cloud Console](https://console.cloud.google.com)
2. Seleziona (o crea) un progetto
3. Vai in **API e servizi → Libreria**
4. Cerca **Google Wallet API** e clicca **Abilita**

#### 2. Creare il Service Account
1. Vai in **IAM e amministrazione → Service Account**
2. Clicca **Crea service account**
3. Inserisci un nome (es. `fidelity-wallet`) e clicca **Crea e continua**
4. Non è necessario assegnare un ruolo IAM in questa fase
5. Clicca **Fine**
6. Annota l'email del service account (es. `fidelity-wallet@progetto.iam.gserviceaccount.com`) → sarà `wallet.google.service-account-email`

#### 3. Generare la chiave privata
1. Clicca sul service account appena creato
2. Vai nella scheda **Chiavi → Aggiungi chiave → Crea nuova chiave**
3. Seleziona formato **JSON** e scarica il file
4. Apri il file JSON e copia il valore del campo `private_key` (stringa PEM completa, incluso `-----BEGIN PRIVATE KEY-----` e `-----END PRIVATE KEY-----`) → sarà `wallet.google.private-key`

#### 4. Registrarsi su Google Pay & Wallet Console
1. Vai su [pay.google.com/business/console](https://pay.google.com/business/console)
2. Completa la registrazione come issuer
3. Nella dashboard trovi l'**Issuer ID** (numero lungo) → sarà `wallet.google.issuer-id`
4. Vai in **Utenti** e aggiungi l'email del service account con ruolo **Amministratore**

#### 5. Creare la LoyaltyClass (operazione una-tantum)

La **LoyaltyClass** è il template che Google Wallet usa per tutti i pass di questo tipo.
Va creata **una sola volta** prima di poter generare pass per gli utenti.

Esegui questa chiamata REST (sostituisci i valori):

```bash
# Prima ottieni un token di accesso
ACCESS_TOKEN=$(curl -s -X POST https://oauth2.googleapis.com/token \
  -d "grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer" \
  -d "assertion=<JWT_SERVICE_ACCOUNT>" \
  | jq -r '.access_token')

# Crea la LoyaltyClass per le card normali
curl -X POST \
  "https://walletobjects.googleapis.com/walletobjects/v1/loyaltyClass" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "<ISSUER_ID>.fidelity_loyalty",
    "issuerName": "Fidelity",
    "programName": "Carta Fedeltà",
    "programLogo": {
      "sourceUri": { "uri": "https://tuodominio.com/assets/logo.png" },
      "contentDescription": { "defaultValue": { "language": "it-IT", "value": "Logo" } }
    },
    "reviewStatus": "UNDER_REVIEW"
  }'

# Crea la LoyaltyClass per i voucher
curl -X POST \
  "https://walletobjects.googleapis.com/walletobjects/v1/loyaltyClass" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "<ISSUER_ID>.fidelity_loyalty_voucher",
    "issuerName": "Fidelity",
    "programName": "Voucher Punti",
    "programLogo": {
      "sourceUri": { "uri": "https://tuodominio.com/assets/logo.png" },
      "contentDescription": { "defaultValue": { "language": "it-IT", "value": "Logo" } }
    },
    "reviewStatus": "UNDER_REVIEW"
  }'
```

> **Nota:** lo stato `UNDER_REVIEW` è normale. La classe funziona immediatamente in ambiente di test.
> Per la produzione Google effettua una revisione manuale.

In alternativa alle chiamate curl, le classi si possono creare anche direttamente dalla
[Google Pay & Wallet Console](https://pay.google.com/business/console) nella sezione **Loyalty**.

#### 6. Configurare le properties
Modifica `application.properties` (e `application-prod.properties`):
```properties
wallet.google.issuer-id=3388000000012345678
wallet.google.class-id=fidelity_loyalty
wallet.google.service-account-email=fidelity-wallet@progetto.iam.gserviceaccount.com
wallet.google.private-key=-----BEGIN PRIVATE KEY-----\nMIIE...\n-----END PRIVATE KEY-----
```

> **Attenzione:** il valore di `private-key` in un file `.properties` deve avere i newline
> sostituiti con `\n` oppure il valore va inserito tutto su una riga.
> In alternativa usa variabili d'ambiente o un vault (consigliato in produzione).

---

## Verifica del funzionamento

### Apple Wallet
1. Avvia il backend con le credenziali configurate
2. Da un iPhone, apri la pagina `card-view/{customerId}` in Safari
3. Tocca **Aggiungi ad Apple Wallet** → deve apparire il dialog nativo di iOS
4. Oppure chiama direttamente: `GET http://host/card/wallet/apple/{cardId}`
   - Risposta attesa: `200 OK` con `Content-Type: application/vnd.apple.pkpass`
   - Risposta se credenziali mancanti: `501 Not Implemented`

### Google Wallet
1. Avvia il backend con le credenziali configurate
2. Chiama: `GET http://host/card/wallet/google/{cardId}`
   - Risposta attesa: `200 OK` con `{ "url": "https://pay.google.com/gp/v/save/..." }`
   - Risposta se credenziali mancanti: `501 Not Implemented`
3. Apri l'URL restituito su un dispositivo Android → deve apparire il dialog "Aggiungi a Google Wallet"

---

## Riepilogo properties

```properties
# ── Apple Wallet ──────────────────────────────────────────────────────────────
wallet.apple.pass-type-id=pass.com.nomeazienda.fidelity   # da Apple Developer
wallet.apple.team-id=ABCDE12345                            # da Apple Membership
wallet.apple.keystore-path=classpath:certs/apple-wallet.p12
wallet.apple.keystore-password=password-del-p12
wallet.apple.wwdrca-path=classpath:certs/AppleWWDRCAG3.cer

# ── Google Wallet ─────────────────────────────────────────────────────────────
wallet.google.issuer-id=3388000000012345678                # da Wallet Console
wallet.google.class-id=fidelity_loyalty                    # definito da te
wallet.google.service-account-email=xxx@yyy.iam.gserviceaccount.com
wallet.google.private-key=-----BEGIN PRIVATE KEY-----\n...
```
