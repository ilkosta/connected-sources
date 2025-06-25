package org.connected_sources.user;

import com.github.slugify.Slugify;
import org.connected_sources.shared.*;

import java.time.Instant;
import java.util.*;

public class ProducerService {

    private final Map<String, Producer> producers = new HashMap<>();
    private final Map<String, ProducerRegistration> pending = new HashMap<>();
    private final Map<String, User> managers = new HashMap<>();

    private final TenantLifecycleManager tenantManager;
    private final NotificationService notificationService;

    public ProducerService(TenantLifecycleManager tenantManager, NotificationService notificationService) {
        this.tenantManager = tenantManager;
        this.notificationService = notificationService;
    }

    public ProducerRegistration register(String name, String email, String legalHQ) {
        final Slugify slg = Slugify.builder().lowerCase(true).build();
        String producerId = slg.slugify(name);
        String registrationId = UUID.randomUUID().toString();
        Instant now = Instant.now();

        ProducerRegistration reg = new ProducerRegistration(registrationId, producerId, name, email, legalHQ, now);
        pending.put(registrationId, reg);

        String link = "https://frontend/register/" + producerId + "/" + registrationId;
        notificationService.sendRegistrationEmail(email, name, link);

        return reg;
    }

    public void completeRegistration(String producerId, String registrationId, User caller) {
        ProducerRegistration reg = pending.get(registrationId);
        if (reg == null) throw new RegistrationNotFoundException();
        if (reg.isExpired()) throw new RegistrationExpiredException();
        if (!reg.getProducerId().equals(producerId)) throw new IllegalArgumentException("Mismatched producer Id");

        Producer producer = new Producer(producerId, reg.getName(), reg.getInstitutionalEmail(), reg.getLegalHeadquarters());
        producers.put(producerId, producer);
        tenantManager.provisionTenant(producerId);
        managers.put(caller.getId(), caller);
    }

    public boolean isRegistered(String id) {
        return producers.containsKey(id);
    }

    public Producer getProducer(String id) {
        return producers.get(id);
    }

    public User getManager(String userId) {
        return managers.get(userId);
    }
}
