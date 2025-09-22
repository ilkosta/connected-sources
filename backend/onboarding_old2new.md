| Old item | New / Replacement |
| --- | --- |
| `api/OnboardingController` | `api/web/onboarding/OnboardingController` (rewired to core repos) |
| `api/dto/OnboardingRequest` | `api/dto/onboarding/OnboardingRequestCreate` (+ `OnboardingView`) |
| `ProducerRegistrationController` | Removed (functionality covered by new flow + registration step) |
| `ProducerRegistrationRequest`, `UserRegistrationRequest` | Removed; replaced by `RegistrationPayload` |
| `core-user/CreateUserAndProducerCommand`, `OnboardingService`, `ProducerRegistration`, `ProducerService` | Removed; split into `OnboardingRepo`/`TenantRepo` + `OnboardingProvisioner` |
| `RegistrationExpiredException`, `RegistrationNotFoundException` | Removed; handled via state transitions (`EXPIRED`) + 404/410 in controller |
| `TeamService` | Removed from onboarding hot path; team bootstrap moves to per-tenant SQLite seeds |
| `shared/NotificationService` (monolithic) | Use `backend-notification/NotificationDispatcher` |
| `tenant-fs-impl/TenantContextFilter` | Remove; context belongs to API/security; use `TenantContextHolder` + `ContextAwareTaskDecorator` |
