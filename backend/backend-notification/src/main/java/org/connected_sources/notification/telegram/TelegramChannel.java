package org.connected_sources.notification.telegram;

import org.connected_sources.notification.core.*;
import org.springframework.stereotype.Component;


@Component
public class TelegramChannel extends BaseChannelAdapter {
  @Override public Channel type() { return Channel.TELEGRAM; }
  @Override protected SendResult sendInternal(RenderedMessage msg) throws Exception {
// integrate Telegram Bot API
    String id = java.util.UUID.randomUUID().toString();
    return new SendResult(true, id, null, false);
  }
}