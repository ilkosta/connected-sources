
## integrazioni

* OntoReMa : https://github.com/regione-marche/OntoReMa

## perché struttura multi modulo

* divisione delle responsabilità tra moduli
```
  ├── backend-api/                      # Modulo gateway REST: espone le API
  ├── backend-content/                 # Modulo funzionalità gestione contenuti
  ├── backend-tenant/                  # Modulo gestione tenant e provisioning
  ├── backend-user/                    # Modulo gestione utenti e registrazione
  ├── backend-notification/           # Modulo gestione notifiche e report
  ├── backend-infra/                   # Integrazioni tecniche: Git, YAML, QR, DB
  ├── backend-shared/                  # Entità, enum, utilità comuni ...
  ...
```  
* cercare di diminuire le dipendenze di ciascun modulo
* testing mirato e più rapido

## perché viene usato gradle

* cache nativo... quindi più rapido in compilazione di progetti multi-modulo
* familiarità