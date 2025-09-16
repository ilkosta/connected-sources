package org.connected_sources.notification.core;

public abstract class BaseChannelAdapter implements ChannelAdapter {

  // final to implement a Template Method
  @Override
  public final SendResult send(RenderedMessage msg) throws Exception {
    return pipeline(msg);
  }

  protected SendResult pipeline(RenderedMessage msg) {
    try {
      beforeSend(msg);
      return sendInternal(msg);
    }
    catch (Exception e) { return onFailure(msg, e); }
  }

  protected void beforeSend(RenderedMessage msg) {}

  // protocol specific
  protected abstract SendResult sendInternal(RenderedMessage msg) throws Exception;

  protected SendResult onFailure(RenderedMessage msg, Exception e) {
    return new SendResult(
            false,
            null,
            e.getClass().getSimpleName(),
            isPermanent(e));
  }

  protected boolean isPermanent(Exception e) { return false; }
}