package org.connected_sources.notification.service;

import org.connected_sources.notification.core.Channel;
import org.connected_sources.notification.core.ChannelAdapter;
import org.connected_sources.notification.core.RenderedMessage;
import org.connected_sources.notification.core.SendResult;

/** Simple per-channel adapter that can be toggled to success / transient fail / permanent fail. */
public class StubChannelAdapter implements ChannelAdapter {

  public enum Mode { SUCCESS, TRANSIENT_FAIL, PERMANENT_FAIL }

  private final Channel channel;
  private volatile Mode mode = Mode.SUCCESS;
  private volatile String errorCode = "SIMULATED";

  public StubChannelAdapter(Channel channel) {
    this.channel = channel;
  }

  public void setMode(Mode mode) { this.mode = mode; }
  public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

  @Override
  public Channel type() { return channel; }

  @Override
  public SendResult send(RenderedMessage msg) {
    return switch (mode) {
      case SUCCESS -> new SendResult(true, "stub-" + channel.name(), null, false);
      case TRANSIENT_FAIL -> new SendResult(false, null, errorCode, false);
      case PERMANENT_FAIL -> new SendResult(false, null, errorCode, true);
    };
  }
}
