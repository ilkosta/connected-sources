package org.connected_sources.notification.model;

import org.connected_sources.notification.core.Channel;

public record CuratorContact(
        Long userId,
        Channel channel,
        String address){


}
