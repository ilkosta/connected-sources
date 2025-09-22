# Karate E2E — Onboarding

Questi test coprono:
- Happy path (REQUESTED→APPROVED→PREPARATION→ENABLED)
- Idempotenza (stesso payload nella stessa giornata → stessa entity)
- Errore permanente (→ FAILED)
- Scadenza timeout (→ EXPIRED)

## Requisiti test-profile
- `karate-config.js` definisce `baseUrl` e token fittizi.
- Possibile riduzione deadline provisioning (`onboarding.provisioning.deadline=PT10S`).
- Hook di test per forzare failure (tenantIdHint contenente `fail-perm`) e per simulare provisioning infinito (`never-ready`).

## Avvio
- Eseguire `OnboardingKarateTest` (JUnit5) o `mvn test -Dtest=OnboardingKarateTest`.
