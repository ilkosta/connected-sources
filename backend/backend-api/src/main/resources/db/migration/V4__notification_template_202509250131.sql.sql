INSERT INTO public.notification_template ("name","version",body_md,"scope",tenant_id) VALUES
	 ('ONBOARDING_APPROVED',1,'## Completa la tua registrazione a Connected Sources

Ricevi questa email perché l''utente {{requesterUserId}} ({{requesterEmail}})ha chiesto la tua registrazione in Connected Sources.
Puoi completare la [procedura di regisrazione]({{link}}) così potrai entrare a far parte di Connected Sources e fare rete con le altre realtà produttive del territorio.
','GLOBAL',NULL),
	 ('ONBOARDING_REQUESTED',1,'# Nuova richiesta di inscrizione

L''utente **{{requesterUserId}}**  ha chiesto l''invio del modulo di inscrizione per **{{producerName}}**.
Se approverai la richiesta, il modulo verrà inviato all''email **{{email}}**.

##### dati della richiesta

- **richiedente**                : {{requesterUserId}}
-  **per il produttore**       : {{producerName}}    
-  **email produttore**       : {{email}}         
- **sito web**                   : {{website}}        
- **partita iva/codice fiscale** : {{vatOrFiscalCode}}

---
informazioni di dettaglio:

* **id di correlazione**: {{correlationId}}
* **data della richiesta**: {{submittedAt}}


## gestiscila

Ti invito a gestire la richiesta **[approvandola]({{approveUrl}})** oppure [rifiutandola]({{rejectUrl}}).
','GLOBAL',NULL);
