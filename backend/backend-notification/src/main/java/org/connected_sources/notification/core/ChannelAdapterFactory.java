package org.connected_sources.notification.core;

import java.util.EnumMap;
import java.util.List;
import org.springframework.stereotype.Component;


@Component
public class ChannelAdapterFactory {
  private final EnumMap<Channel, ChannelAdapter> adapters = new EnumMap<>(Channel.class);

  public ChannelAdapterFactory(List<ChannelAdapter> channels) {
    channels.forEach(c -> adapters.put(c.type(), c));
  }
  public ChannelAdapter resolve(Channel c) { return adapters.get(c); }
}