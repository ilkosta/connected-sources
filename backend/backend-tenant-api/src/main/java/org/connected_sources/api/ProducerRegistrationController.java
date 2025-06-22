package org.connected_sources.api;

import org.connected_sources.shared.*;
import org.connected_sources.user.ProducerRegistration;
import org.connected_sources.user.ProducerService;
import org.connected_sources.user.RegistrationExpiredException;
import org.connected_sources.user.RegistrationNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/producer")
public class ProducerRegistrationController {

  private final ProducerService producerService;

  public ProducerRegistrationController(ProducerService producerService) {
    this.producerService = producerService;
  }

  @PostMapping("/register")
  public ResponseEntity<Map<String, String>> registerProducer(@Valid @RequestBody ProducerRegistrationRequest request) {
    ProducerRegistration reg = producerService.register(request.getName(), request.getInstitutionalEmail(), request.getLegalHeadquarters());
    return ResponseEntity.ok(Map.of(
            "producerId", reg.getProducerId(),
            "registrationId", reg.getRegistrationId()
                                   ));
  }

  @PostMapping("/{producerId}/complete-registration/{registrationId}")
  public ResponseEntity<Void> completeRegistration(
          @PathVariable String producerId,
          @PathVariable String registrationId,
          @RequestHeader("X-User-Id") String userId,
          @RequestHeader("X-User-Email") String email
                                                  ) {
    User user = new User(userId, email);
    producerService.completeRegistration(producerId, registrationId, user);
    return ResponseEntity.ok().build();
  }

  @ExceptionHandler(RegistrationNotFoundException.class)
  public ResponseEntity<String> handleNotFound() {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Registration not found");
  }

  @ExceptionHandler(RegistrationExpiredException.class)
  public ResponseEntity<String> registrationExpired() {
    return ResponseEntity.status(HttpStatus.GONE).body("Registration request expired");
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
  }
}
