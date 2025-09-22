package org.connected_sources.api.notification;

import java.time.Duration;

import org.connected_sources.notification.events.EventType;
import org.connected_sources.notification.template.NotificationTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.connected_sources.notification.core.Channel;
import org.connected_sources.notification.service.*;



@RestController
@RequestMapping("/notifications")
public class NotificationController {
  private final NotificationDispatcher dispatcher;
  public NotificationController(NotificationDispatcher d) { this.dispatcher = d; }


  public record SendRequest(String templateId, String recipientKey, String subject, String bodyMd,
                            String ttl, String eventType, String channel, boolean hasPii) {}
  public record SendResponse(String status, String auditId, boolean duplicate) {}


  @PostMapping
  public ResponseEntity<SendResponse> post(@RequestBody SendRequest req) {
    Channel ch = Channel.valueOf(req.channel()); // or AUTO selection elsewhere
    Duration ttl = req.ttl() == null ? null : Duration.parse(req.ttl());
    var dispatchOutcome = dispatcher.enqueue(
            NotificationTemplate.fromId(req.templateId()),
            req.recipientKey(), ch,
            req.subject(), req.bodyMd(), ttl, EventType.fromString(req.eventType()), req.hasPii());
    if (dispatchOutcome.isDuplicate())
      return ResponseEntity.ok(new SendResponse("duplicate", null, true));
    return ResponseEntity.accepted().body(new SendResponse("accepted", dispatchOutcome.auditId(), false));
  }
}