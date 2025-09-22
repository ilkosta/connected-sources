//package org.connected_sources.core.user;
//
//import org.connected_sources.shared.*;
//import org.connected_sources.tenant.spi.TenantLifecycleManager;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.time.Instant;
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
////import org.connected_sources.shared.*;
//
//public class ProducerOnboardingTest {
//
//  private ProducerService producerService;
//  private NotificationService notificationService;
//  private TenantLifecycleManager tenantManager;
//
//  @BeforeEach
//  void setup() {
//    notificationService = mock(NotificationService.class);
//    tenantManager = mock(TenantLifecycleManager.class);
//    producerService = new ProducerService(tenantManager, notificationService);
//  }
//
//  @Test
//  void testRegisterProducerSendsEmail() {
//    String name = "Consorzio Marche";
//    String email = "info@consorzio.it";
//    String hq = "Via Roma 1, Ancona";
//
//    String username = "foo";
//    String userEmail = "foo@gmail.com";
//
//    ProducerRegistration reg = producerService.register(
//            username, userEmail,
//            name, email, hq);
//
////    assertEquals("consorzio-marche", reg.getProducerId());
//    assertEquals(name, reg.getName());
//    assertEquals(email, reg.getInstitutionalEmail());
//    assertFalse(reg.isExpired());
//
//    verify(notificationService, times(1))
//            .sendRegistrationEmail(eq(email), eq(username),eq(userEmail), contains("/register/consorzio-marche/"));
//  }
//
//  @Test
//  void testCompleteRegistrationCreatesProducerAndManager() {
//    ProducerRegistration reg = producerService.register(
//            "user1", "user1@gmail.com",
//              "Consorzio Marche", "info@consorzio.it", "Via Pluto, 10 Paperinopoli");
//    User user = new User();
//    user.setId(1L);
//    user.setEmail("info@user.it");
//    producerService.completeRegistration(reg.getProducerId(), reg.getRegistrationId(), user);
//
//    Producer p = producerService.getProducer(reg.getProducerId());
//    assertNotNull(p);
//    assertEquals(reg.getName(), p.getName());
//
//    User manager = producerService.getManager(user.getId());
//    assertEquals(user.getEmail(), manager.getEmail());
//
//    verify(tenantManager, times(1)).provisionTenant(reg.getProducerId());
//  }
//
//  @Test
//  void testExpiredRegistrationIsRejected() {
//    String producerId = "expired-prod";
//    String registrationId = "reg-expired";
//    ProducerRegistration expired = new ProducerRegistration(
//            registrationId, producerId, "Expired", "expired@xx.it", "HQ", Instant.now().minusSeconds(31L * 24 * 60 * 60)
//    );
//
//    // simulate manual insertion (bypass register)
//    Map<String, ProducerRegistration> pending = TestAccess.getPendingMap(producerService);
//    pending.put(registrationId, expired);
//
//    User user = new User();
//    user.setId(2L);
//    user.setEmail("user2@xx.it");
//    assertThrows(RegistrationExpiredException.class, () ->
//            producerService.completeRegistration(producerId, registrationId, user));
//  }
//
//  // barbatrucco per accedere a pendingRegistrations privata dentro Producerservice
//  // la userò ad esempio per inserire una registrazione scaduta
//  static class TestAccess {
//    static Map<String, ProducerRegistration> getPendingMap(ProducerService service) {
//      try {
//        var field = ProducerService.class.getDeclaredField("pending");
//        field.setAccessible(true);
//        return (Map<String, ProducerRegistration>) field.get(service);
//      } catch (Exception e) {
//        throw new RuntimeException("Reflection failed", e);
//      }
//    }
//  }
//}
