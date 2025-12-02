## baseline architetturale

* **Stack**: Java 21, Spring Boot, Gradle (Kotlin DSL), SQLite/PostgreSQL
* sistema diviso in due applicativi:
  - **pubblico** : generato dal processo di pubblicazione nel backend (rigenerabile, replicabile, ...)
    + è composto per la quasi totalità da contenuto statico generato dal processo di pubblicazione dell'applicativo privato
  - **privato** : sistema di gestione dei contenuti
    + ogni produttore di contenuti ha un proprio tenant
    * Isolamento del tenant: dati, log e runtime devono essere completamente separati e facilmente esportabili
    * Confini modulari: la base di codice deve essere manutenibile, scalabile e testabile in modo indipendente
    * ogni tenant deve essere fornito dinamicamente
    * API stateless: tutte le API devono essere stateless e risolte in base al contesto/tenant
    
    
### 🛡️ rischi tecnici

| rischio | strategia di mitigazione | 
| --------| ------------------------ | 
| Perdita di dati tra tenant | forte isolamento runtime  e a livello storage: tutto l'I/O diviso per tenant |
| compromissione dati | gestione dei tenant in repository storicizzati e replicati in remoto |
| Problemi di concorrenza tra tenant | nessuna condivisione dei pool di connessioni | 
| Base di codice non scalabile | moduli separati testabili indipendentemente | 
| difficoltà nel testare il comportamento dei tenant | ogni flusso tenant testabile in modo indipendente |
| avvio fragile / problemi di IO | provisioning è su richiesta e recuperabile | 
| notifiche inviate ad attori errati | gestione dell'informazione del tenant di riferimento, coda con riconoscimento del tenant, tracciatura del tenant nel logging | 
### vincoli normativi

* GDPR
* NIS2

## 🔐 requisiti non funzionali

### legati alla divisione del sistema tra parte privata e pubblica

| Requisito                   | Dettaglio                                                                                                                 |
|-----------------------------|---------------------------------------------------------------------------------------------------------------------------|
| **efficienza**                  | i contenuti pubblicati sono di origine statica, pre-elaborati, affidati all'accesso diretto di un webserver specializzato |
| testabilità/**controllabilità** | gli elementi pubblici sono il risultato ripetibile di elaborazioni che insieme al contenuto pubblicato memorizzano il relativo hash per verifiche di integrità |
| **affidabilità** | la parte pubblica, controllabile e statica sarà soggetta a pochissimi errori riscontrabili dagli utenti, i contenuti potranno essere rigenerati se compromessi. Il sistema può essere replicato e bilanciato |


### legati alle scelte di gestione dei tenant

| Requisito                                   | Dettaglio                                                                                                                                                                                            |
|---------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Portabilità e interoperabilità**             | Ogni tenant può essere consegnato allo stakeholder richiedente. Il payload sarà comprensivo di tutti i dati raccolti dal sistema. Il formato è basato su database open (es. sqlite) e repository git |
| **Isolamento**                                  | Ogni tenant opera su file system e/o database dedicati, con un proprio pool di connessioni                                                                                                           |
| **Scalabilità**                                 | I tenant possono essere distribuiti orizzontalmente su più server applicativi                                                                                                                        |
| Flessibilità nelle performance (**efficienza**) | Ai tenant più utilizzati può essere concesso l'hardware più performante etc.                                                                                                                         |
| Migliore gestione dello spazio (**efficienza**) | I tenant rappresentano le unità minime di dati che possono essere gestite e riallocate tra i server permettendo una ottimizzazione nella distribuzione                                               |
| **Logging per tenant**                                     | Ogni operazione è tracciata nel singolo tenant (sia nella versione db che filesystem)                                                                                                                
| **Disponibilità** | i tenant nella configurazione repository+db locale possono essere replicati automaticamente, facilmente spostati e ricostruiti in caso di incidente |

## struttura multi modulo

### motivazioni

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
* tenere sotto controllo l'accoppiamento di ciascun modulo (gestione delle dipendenze)
  * **manutenibilità**: porta ad un impatto ai cambiamenti meglio circoscritto e limitato
  * **riusabilità**: maggiore possibilità di riuso
* testing mirato
* **sostituibilità** delle componenti implementative sfruttando il classpath

### modalità

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


## integrazioni future

* OntoReMa : https://github.com/regione-marche/OntoReMa
