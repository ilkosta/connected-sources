//package org.connected_sources.core.user;
//
//import com.github.slugify.Slugify;
//import org.connected_sources.shared.*;
//import org.connected_sources.tenant.spi.TenantLifecycleManager;
//import org.springframework.stereotype.Component;
//
//import java.time.Instant;
//import java.util.*;
//
//@Component
//public class ProducerService {
//
//    private final Map<String, Producer> producers = new HashMap<>();
//    private final Map<String, ProducerRegistration> pending = new HashMap<>();
//    private final Map<Long, User> managers = new HashMap<>();
//
//    private final TenantLifecycleManager tenantManager;
//    private final NotificationService notificationService;
//
//    public ProducerService(TenantLifecycleManager tenantManager, NotificationService notificationService) {
//        this.tenantManager = tenantManager;
//        this.notificationService = notificationService;
//    }
//
//    public ProducerRegistration register(
//            final String username, final String userEmail, // the requesting user
//            final String name, final String email, final String legalHQ) {    // the producer data
//        String producerId = UUID.randomUUID().toString();
//        String registrationId = UUID.randomUUID().toString();
//        Instant now = Instant.now();
//
//        ProducerRegistration reg = new ProducerRegistration(
//                registrationId, producerId, name, email, legalHQ, now);
//        pending.put(registrationId, reg);
//
//      final Slugify slg = Slugify.builder().lowerCase(true).build();
//        String link = "https://frontend/register/" + slg.slugify(name) + "/" + registrationId;
//        notificationService.sendRegistrationEmail(email, username, userEmail, link); // TODO: ci va il nome utente
//
//        return reg;
//    }
//
//    public void completeRegistration(String producerId, String registrationId, User caller) {
//        ProducerRegistration reg = pending.get(registrationId);
//        if (reg == null) throw new RegistrationNotFoundException();
//        if (reg.isExpired()) throw new RegistrationExpiredException();
//        if (!reg.getProducerId().equals(producerId)) throw new IllegalArgumentException("Mismatched producer Id");
//
//        Producer producer = new Producer(producerId, reg.getName(), reg.getInstitutionalEmail(), reg.getLegalHeadquarters());
//        producers.put(producerId, producer);
//        tenantManager.provisionTenant(producerId);
//        managers.put(caller.getId(), caller);
//    }
//
//    public boolean isRegistered(String id) {
//        return producers.containsKey(id);
//    }
//
//    public Producer getProducer(String id) {
//        return producers.get(id);
//    }
//
//    public User getManager(Long userId) {
//        return managers.get(userId);
//    }
//}
