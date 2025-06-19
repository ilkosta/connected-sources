package org.connected_sources.user;


import org.connected_sources.shared.Producer;
import org.connected_sources.shared.User;
import org.connected_sources.shared.UserRole;
import org.connected_sources.shared.TenantLifecycleManager;

import java.util.*;

public class ProducerService {

    private final Map<String, Producer> producers = new HashMap<>();
    private final Map<String, User> managers = new HashMap<>();
    private final TenantLifecycleManager tenantManager;

    public ProducerService(TenantLifecycleManager tenantManager) {
        this.tenantManager = tenantManager;
    }

    public void registerProducer(String id, String name, String email) {
        Producer producer = new Producer(id, name, email);
        producers.put(id, producer);
        tenantManager.createTenant(id);
    }

    public void completeRegistration(String producerId, User manager) {
        if (!producers.containsKey(producerId)) throw new IllegalArgumentException("Producer not found");
        managers.put(manager.getId(), manager);
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

    public boolean exists(String producerId) {
        return producers.containsKey(producerId);
    }
}
