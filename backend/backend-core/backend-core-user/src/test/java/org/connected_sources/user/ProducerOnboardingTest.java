package org.connected_sources.user;

import org.connected_sources.shared.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.connected_sources.shared.*;

public class ProducerOnboardingTest {

  private ProducerService producerService;
  private NotificationService notificationService;
  private TenantLifecycleManager tenantManager;

  @BeforeEach
  public void setup() {
    notificationService = mock(NotificationService.class);
    tenantManager = mock(TenantLifecycleManager.class);
    producerService = new ProducerService(tenantManager, notificationService);
  }

  @Test
  public void testRegisterProducerSendsEmail() {
    String name = "Consorzio Marche";
    String email = "info@consorzio.it";
    String hq = "Via Roma 1, Ancona";

    ProducerRegistration reg = producerService.register(name, email, hq);

    assertEquals("consorzio-marche", reg.getProducerId());
    assertEquals(name, reg.getName());
    assertEquals(email, reg.getInstitutionalEmail());
    assertFalse(reg.isExpired());

    verify(notificationService, times(1)).sendRegistrationEmail(eq(email), eq(name),
                                                                contains("/register/consorzio-marche/"));
  }

  @Test
  public void testCompleteRegistrationCreatesProducerAndManager() {
    ProducerRegistration reg = producerService.register("Consorzio Marche", "info@consorzio.it", "Via Roma");
    User user = new User("u1", "info@user.it");

    producerService.completeRegistration(reg.getProducerId(), reg.getRegistrationId(), user);

    Producer p = producerService.getProducer(reg.getProducerId());
    assertNotNull(p);
    assertEquals(reg.getName(), p.getName());

    User manager = producerService.getManager("u1");
    assertEquals(user.getEmail(), manager.getEmail());

    verify(tenantManager, times(1)).provisionTenant(reg.getProducerId());
  }

  @Test
  public void testExpiredRegistrationIsRejected() {
    String producerId = "expired-prod";
    String registrationId = "reg-expired";
    ProducerRegistration expired = new ProducerRegistration(
            registrationId, producerId, "Expired", "expired@xx.it", "HQ", Instant.now().minusSeconds(31L * 24 * 60 * 60)
    );

    // simulate manual insertion (bypass register)
    Map<String, ProducerRegistration> pending = TestAccess.getPendingMap(producerService);
    pending.put(registrationId, expired);

    User user = new User("u2", "user2@xx.it");

    assertThrows(RegistrationExpiredException.class, () ->
            producerService.completeRegistration(producerId, registrationId, user));
  }

  // barbatrucco per accedere a pendingRegistrations privata dentro Producerservice
  // la userò ad esempio per inserire una registrazione scaduta
  static class TestAccess {
    static Map<String, ProducerRegistration> getPendingMap(ProducerService service) {
      try {
        var field = ProducerService.class.getDeclaredField("pending");
        field.setAccessible(true);
        return (Map<String, ProducerRegistration>) field.get(service);
      } catch (Exception e) {
        throw new RuntimeException("Reflection failed", e);
      }
    }
  }
}
