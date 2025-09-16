package org.connected_sources.notification.core;

public interface ChannelAdapter {
  Channel type();
  SendResult send(RenderedMessage msg) throws Exception;
}
