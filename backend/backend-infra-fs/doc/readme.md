modulo contenente le integrazioni con servizi esterni... 
ad esempio:

* Git (inizializzazione, commit)
* YAML parser/validator
* QRCode generator 
* Static site generator
* Configurazione FileSystem

Rappresenta un modulo verticale... 

visto che al momento la scelta di gestione del tenant è tramite GIT
come conseguenza viene la necessità di integrare anche altri servizi
es. la serializzazione YAML, una certa logica di verifica dell'integrità/stato del tenant
