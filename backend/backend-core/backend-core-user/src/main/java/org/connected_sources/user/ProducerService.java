package org.connected_sources.user;

import java.util.*;

public class ProducerService {
    private final Map<String, String> registeredProducers = new HashMap<>();

    public void registerProducer(String producerId, String name) {
        registeredProducers.put(producerId, name);
    }

    public String getProducerName(String producerId) {
        return registeredProducers.get(producerId);
    }

    public boolean exists(String producerId) {
        return registeredProducers.containsKey(producerId);
    }
}
