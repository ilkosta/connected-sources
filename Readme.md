## Stato attuale

Si è scelto di concentrare lo sviluppo iniziale sul **processo di onboarding dei produttori** perché rappresenta un'area **critica** sia dal punto di vista **normativo** sia dal punto di vista **architetturale e di rischio operativo**.

In particolare:

### Rispondere ai requisiti normativi e di settore (NIS2, sicurezza, tracciabilità)

L’onboarding è il momento in cui nasce un nuovo tenant. È quindi cruciale garantire:

* isolamento dei dati e delle risorse,
* auditabilità completa,
* provisioning affidabile delle componenti (FS, DB, log),
* gestione strutturata di errori e incidenti.

Questi aspetti sono direttamente allineati ai requisiti di **NIS2**, che richiedono segregazione, logging accurato, resilienza e gestione degli incidenti.


### Sostenere il core business: erogare servizi ai produttori

I produttori sono il centro del sistema.
Se l’onboarding non funziona o non è affidabile:

* i produttori non possono iniziare a usare la piattaforma,
* nessuna altra funzionalità può essere esercitata,
* qualsiasi errore avrebbe un impatto immediato sulla continuità del servizio.

L’onboarding è quindi **un blocco indispensabile**, senza il quale il prodotto non può esistere.


### Affrontare per primi i casi d’uso più rischiosi e trasversali

L’onboarding coinvolge numerosi componenti:

* API pubbliche, sicurezza, token
* Postgres, SQLite per tenant
* Filesystem dedicato
* Logging multi-livello
* Notifiche e ticketing
* Esecutori asincroni, retry, timeout
* Normalizzazione e naming
* Contesto tenant (TenantContextHolder + MDC)

È il flusso con la **massima entropia funzionale**.
Implementarlo correttamente fin da subito evita debito tecnico in quasi ogni altro modulo.


### Validare e mettere alla prova la separazione dei tenant (punto fondamentale)

I casi d’uso dell’onboarding sono gli **unici** che:

* **creano** un tenant,
* **inizializzano** il suo filesystem dedicato,
* **creano e migrano** il suo database SQLite,
* **attivano** i logger isolati,
* **verificano** che il routing delle risorse (DB, FS, log) sia realmente per-tenant.

In altre parole:

> L’onboarding è il modo più naturale e completo per verificare la bontà dell’architettura multitenant.
> Se funziona qui, funzionerà anche in tutti i casi d’uso successivi.

È il modulo che obbliga l’intera architettura a dimostrare:

* che la separazione tra tenant esiste davvero,
* che i componenti lavorano in isolamento,
* che il contesto (tenantId) viene propagato correttamente,
* che le operazioni asincrone non “perdono” il tenant di riferimento,
* che ogni tenant mantiene il proprio ciclo di vita indipendente.

Tutto ciò *prima* di implementare funzionalità di contenuto, notifiche multi-tenant, trasformazioni, integrazioni, ecc.


### Definire pattern architetturali solidi per l’intera piattaforma

Implementando l’onboarding si sono consolidati:

* pattern per gli executor asincroni e la propagazione del contesto,
* meccanismi di logging isolato,
* routing dinamico dei datasource,
* naming e normalizzazione dei tenant,
* gestione automatica degli incidenti,
* migrazioni Flyway per tenant,
* API idempotenti.

Questi meccanismi ora sono **riutilizzabili e coerenti** per ogni altro modulo.



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

* NIS2
* GDPR

#### 🛡️ Integrazione dell’architettura con i requisiti NIS2

L’architettura implementata per la piattaforma multi-tenant è stata progettata in modo da **isolare i tenant**, garantire **tracciabilità completa**, supportare **processi di notifica degli incidenti**, e mantenere un elevato livello di **resilienza operativa**.
Questi elementi coincidono direttamente con molte delle prescrizioni della Direttiva NIS2.

Di seguito una sintesi dei punti principali.

---

##### 1. **Isolamento dei tenant = Security-by-Design**

La piattaforma applica un modello di **segregazione per-tenant**:

* **Filesystem separato** per ogni soggetto (con directory dedicate)
* **Database SQLite dedicato** per tenant, isolato da altri tenant
* **Log FS separati e non condivisi**
* **Context propagation (TenantContext)** che impedisce l’accesso incrociato ai dati

Questo approccio risponde ai principi NIS2 di:

* minimizzazione del rischio di accesso non autorizzato,
* riduzione degli impatti in caso di incidente su un singolo tenant,
* contenimento del blast radius.

---

##### 2. **Tracciabilità e logging avanzato**

La piattaforma implementa un sistema di log multilivello:

* Log **per-tenant** (SQLite e filesystem)
* Log **globali** su Postgres
* Contesto obbligatorio: `tenantId`, `userId`, `correlationId`, IP, UA, path, latenza
* Eventi classificati come: `SECURITY`, `AUDIT`, `ACCESS`, ecc.

Questo fornisce:

* **Audit trail completo**, requisito centrale in NIS2
* capacità di ricostruire eventi e catene causali in caso di incidente
* tracciabilità delle attività degli utenti privilegiati (curatore, amministratore)

---

##### 3. **Gestione degli incidenti con apertura automatica ticket**

In caso di errore permanente, il sistema:

* rileva l’incidente
* cerca ticket già aperti (evita duplicazioni)
* crea automaticamente un **incident ticket** nel sistema esterno (es. Redmine valutato come prima implementazione)
* collega l’ID del ticket nella tabella audit (`notification_audit.ticket_id`)

Questo soddisfa i requisiti NIS2 di:

* **incident reporting**,
* **processo strutturato di gestione degli incidenti**,
* **notifica tempestiva** verso gli stakeholder (curatori, produttori).

---

##### 4. **Resilienza tecnica tramite provisioning asincrono**

La separazione tra:

* API sincrone (“veloci”)
* provisioning asincrono (“lento”, con retry/backoff)

permette di:

* evitare blocchi o degradazioni del servizio
* gestire picchi di carico con **bounded queue + CallerRunsPolicy**
* mitigare errori temporanei (retry con backoff)
* applicare timeout automatico (deadline di 2 giorni → stato `EXPIRED`)

Questo si allinea con le richieste NIS2 di:

* **resilienza dei servizi essenziali**,
* continuità operativa,
* strategie di mitigazione per fallimenti non permanenti.

---

##### 5. **Notifiche a stakeholder differenti**

Il processo di onboarding e provisioning notifica in modo differenziato:

* Curatore
* Richiedente
* Amministratore del produttore
* (in caso di incidente) sistema di ticketing

Questa differenziazione supporta i requisiti NIS2 di:

* **comunicazione efficace degli incidenti**,
* informazione proporzionata al ruolo,
* verificabilità del canale.

---

##### 6. **Configurazione centralizzata e controllata**

Il sistema centralizza configurazioni sensibili:

* SMTP/Telegram
* Redmine incident tracker
* Retry policy
* Mappature categoria/errore per ticketing

e consente override per ambienti di test → garantendo:

* governance coerente
* facilità di audit
* prevenzione di configurazioni inconsistenti

---

##### 7. **Principio del minimo privilegio**

Ruoli distinti:

* **Richiedente**: può solo aprire una richiesta
* **Curatore**: può approvare o respingere
* **Admin del produttore**: configurato nella fase di registrazione

L’intero processo rispetta NIS2 in termini di:

* controllo degli accessi basato su ruolo
* separazione dei compiti
* riduzione del rischio di abusi

---

##### 8. **Metriche e osservabilità → capacità di risposta rapida**

Il sistema espone metriche:

* latenza delle notifiche
* errori e retry
* stati onboarding (REQUESTED/APPROVED/PREPARATION/ENABLED/FAILED/EXPIRED)
* spazio su disco, scrivibilità SQLite
* dimensione e saturazione degli executor asincroni

Utile per:

* identificare anomalie operative
* prevenire incidenti
* facilitare reportistica NIS2


## integrazioni future

* OntoReMa : https://github.com/regione-marche/OntoReMa - l'analisi dei casi d'uso è stata svolta considerando le ontologie definite in OntiReMa e le possibilità di integrazione offerte

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



