
## integrazioni

* OntoReMa : https://github.com/regione-marche/OntoReMa

## gestione dei tenant

### 🔐 Requisiti Non Funzionali legati alle scelte di gestione dei tenant

| Requisito   | Dettaglio                                                                            |
| ----------- |--------------------------------------------------------------------------------------|
| Isolamento  | Ogni tenant opera su file system e/o database dedicati                               |
| Scalabilità | I moduli sono indipendenti e gestiti per tenant                                      |
| Sicurezza   | Validazione e contesto tenant obbligatorio                                           |
| Logging     | Ogni operazione è tracciata nel singolo tenant (sia nella versione db che filesystem)|

## perché struttura multi modulo

* divisione delle responsabilità tra moduli
```
  ├── backend-api/                     # Modulo gateway REST: espone le API
  ├── backend-content/                 # Modulo funzionalità gestione contenuti
  ├── backend-tenant/                  # Modulo gestione tenant e provisioning
  ├── backend-user/                    # Modulo gestione utenti e registrazione
  ├── backend-notification/            # Modulo gestione notifiche e report
  ├── backend-infra/                   # Integrazioni tecniche: Git, YAML, QR, DB
  ├── backend-shared/                  # Entità, enum, utilità comuni ...
  ...
```  
* cercare di diminuire le dipendenze di ciascun modulo
* testing mirato e più rapido
* possibilità di sostituibilità delle componenti implementative sfruttando il classpath


Ogni modulo/plugin implementativo condividerà una stessa struttura del dominio definita tramite pacchetti, es.

```
org.connected_sources.user.controller
org.connected_sources.user.service
org.connected_sources.content.entity
org.connected_sources.api.controller
```
**TODO**: attenzione a far dipendere i moduli solo da interfacce ben definite... **non cercare scorciatoie per i test**...


## perché viene usato gradle

* cache nativo... quindi più rapido in compilazione di progetti multi-modulo
* familiarità

Ogni modulo deve avere un proprio build.gradle.kts con dipendenze minime e possibilmente
riferimenti espliciti solo ai moduli comuni (backend-shared, backend-tenant-api, ecc.).